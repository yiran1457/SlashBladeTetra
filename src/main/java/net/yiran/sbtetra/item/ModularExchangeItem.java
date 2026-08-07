package net.yiran.sbtetra.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.TweakData;

import java.util.*;
import java.util.stream.Stream;

public class ModularExchangeItem extends Item {
    public static final String MODULE_DATA_KEY = "ModuleData";
    public static final String HAS_DATA_KEY = "HasModuleData";
    public static final String HONE_LIMIT_KEY = "honing_limit";

    private static final String[] KNOWN_SLOTS = {
            "slashblade/handle",
            "slashblade/blade",
            "slashblade/tsuba",
            "slashblade/scabbard",
            "slashblade/soul"
    };

    private static final Set<String> GLOBAL_MODULE_KEYS = new LinkedHashSet<>(Arrays.asList(
            "EnchantmentMapping",
            "Enchantments",
            "soul_slot",
            IModularItem.honeProgressKey,
            IModularItem.honeAvailableKey,
            IModularItem.honeCountKey,
            IModularItem.repairCountKey,
            HONE_LIMIT_KEY
    ));
    private static final String PREFIX_MAJOR = "\u00bb ";
    private static final String PREFIX_MINOR = " * ";
    private static final String PREFIX_DETAIL = "  - ";

    public ModularExchangeItem() {
        super(new Properties().stacksTo(1));
    }

    public static boolean hasModuleData(ItemStack stack) {
        return stack.hasTag()
                && stack.getTag().getBoolean(HAS_DATA_KEY)
                && stack.getTag().contains(MODULE_DATA_KEY, Tag.TAG_COMPOUND);
    }

    public static CompoundTag getModuleData(ItemStack stack) {
        return hasModuleData(stack) ? stack.getTag().getCompound(MODULE_DATA_KEY).copy() : new CompoundTag();
    }

    public static void storeFromBlade(ItemStack exchange, ItemStack blade) {
        if (!(blade.getItem() instanceof ISlashBladeTetra modularBlade) || !blade.hasTag()) {
            return;
        }

        CompoundTag source = blade.getTag();
        CompoundTag moduleData = new CompoundTag();
        Set<String> copiedKeys = new HashSet<>();

        Stream.concat(
                Arrays.stream(modularBlade.getMajorModuleKeys(blade)),
                Arrays.stream(modularBlade.getMinorModuleKeys(blade))
        ).distinct().forEach(slot -> copySlotData(source, moduleData, modularBlade, blade, slot, copiedKeys));

        for (String slot : KNOWN_SLOTS) {
            if (!copiedKeys.contains(slot) && source.contains(slot, Tag.TAG_STRING)) {
                copySlotData(source, moduleData, modularBlade, blade, slot, copiedKeys);
            }
        }

        for (String key : GLOBAL_MODULE_KEYS) {
            if (source.contains(key)) {
                copyTag(source, moduleData, key);
            }
        }

        // Prefer live stack enchantment tags (vanilla format) for reliable transfer/tooltip.
        ListTag enchantmentTags = blade.getEnchantmentTags();
        if (!enchantmentTags.isEmpty()) {
            moduleData.put("Enchantments", enchantmentTags.copy());
        }

        // Snapshot hone limit for tetra-style progress display (not a runtime tetra key).
        moduleData.putInt(HONE_LIMIT_KEY, modularBlade.getHoningLimit(blade));
        if (source.contains(IModularItem.honeProgressKey)) {
            moduleData.putInt(IModularItem.honeProgressKey, source.getInt(IModularItem.honeProgressKey));
        } else {
            moduleData.putInt(IModularItem.honeProgressKey, modularBlade.getHoningProgress(blade));
        }

        CompoundTag tag = exchange.getOrCreateTag();
        tag.put(MODULE_DATA_KEY, moduleData);
        tag.putBoolean(HAS_DATA_KEY, true);
    }

    private static void copySlotData(CompoundTag source, CompoundTag moduleData, ISlashBladeTetra modularBlade,
                                     ItemStack blade, String slot, Set<String> copiedKeys) {
        if (!source.contains(slot, Tag.TAG_STRING)) {
            return;
        }

        String moduleKey = source.getString(slot);
        moduleData.putString(slot, moduleKey);
        copiedKeys.add(slot);

        String variantKey = moduleKey + "_material";
        if (source.contains(variantKey)) {
            copyTag(source, moduleData, variantKey);
            copiedKeys.add(variantKey);
        }

        String settleProgressKey = slot + "/settle_progress";
        if (source.contains(settleProgressKey)) {
            copyTag(source, moduleData, settleProgressKey);
            copiedKeys.add(settleProgressKey);
        }

        ItemModule module = modularBlade.getModuleFromSlot(blade, slot);
        if (module != null) {
            for (TweakData tweak : module.getTweaks(blade)) {
                String tweakKey = slot + ":" + tweak.key;
                if (source.contains(tweakKey)) {
                    copyTag(source, moduleData, tweakKey);
                    copiedKeys.add(tweakKey);
                }
            }
        }

        if (module instanceof ItemModuleMajor majorModule) {
            for (ImprovementData improvement : majorModule.getImprovements(blade)) {
                String improvementKey = slot + ":" + improvement.key;
                if (source.contains(improvementKey)) {
                    copyTag(source, moduleData, improvementKey);
                    copiedKeys.add(improvementKey);
                }
            }
        }

        String prefix = slot + ":";
        for (String key : source.getAllKeys()) {
            if ((key.startsWith(prefix) || key.equals(settleProgressKey)) && !copiedKeys.contains(key)) {
                copyTag(source, moduleData, key);
                copiedKeys.add(key);
            }
        }
    }

    public static void applyToBlade(ItemStack exchange, ItemStack blade) {
        if (!(blade.getItem() instanceof ISlashBladeTetra modularBlade) || !hasModuleData(exchange)) {
            return;
        }

        CompoundTag moduleData = getModuleData(exchange);
        CompoundTag target = blade.getOrCreateTag();

        clearModularData(blade, modularBlade);

        for (String key : moduleData.getAllKeys()) {
            // Display-only snapshot; tetra recalculates hone limit from item properties.
            if (HONE_LIMIT_KEY.equals(key)) {
                continue;
            }
            copyTag(moduleData, target, key);
        }

        IModularItem.updateIdentifier(blade);
        modularBlade.clearCaches();
        modularBlade.assemble(blade, null, 0f);
    }

    public static void swapWithBlade(ItemStack exchange, ItemStack blade) {
        if (!(blade.getItem() instanceof ISlashBladeTetra modularBlade)) {
            return;
        }

        CompoundTag previous = hasModuleData(exchange) ? getModuleData(exchange) : null;
        storeFromBlade(exchange, blade);

        if (previous == null) {
            clearModularData(blade, modularBlade);
            ISlashBladeTetra.putDefaultModule(blade);
            IModularItem.updateIdentifier(blade);
            modularBlade.clearCaches();
            modularBlade.assemble(blade, null, 0f);
            return;
        }

        ItemStack temp = exchange.copy();
        temp.getOrCreateTag().put(MODULE_DATA_KEY, previous);
        temp.getOrCreateTag().putBoolean(HAS_DATA_KEY, true);
        applyToBlade(temp, blade);
    }

    private static void clearModularData(ItemStack blade, ISlashBladeTetra modularBlade) {
        CompoundTag tag = blade.getOrCreateTag();

        Set<String> slots = new HashSet<>();
        slots.addAll(Arrays.asList(modularBlade.getMajorModuleKeys(blade)));
        slots.addAll(Arrays.asList(modularBlade.getMinorModuleKeys(blade)));
        slots.addAll(Arrays.asList(KNOWN_SLOTS));

        for (String slot : slots) {
            ItemModule module = modularBlade.getModuleFromSlot(blade, slot);
            String materialKey = null;
            if (tag.contains(slot, Tag.TAG_STRING)) {
                materialKey = tag.getString(slot) + "_material";
            }

            if (module != null) {
                module.removeModule(blade, false);
            } else {
                tag.remove(slot);
            }

            String prefix = slot + ":";
            Set<String> removable = new HashSet<>();
            for (String key : tag.getAllKeys()) {
                if (key.startsWith(prefix) || key.equals(slot + "/settle_progress")) {
                    removable.add(key);
                }
            }
            if (materialKey != null) {
                removable.add(materialKey);
            }
            removable.forEach(tag::remove);
            tag.remove(slot);
        }

        for (String key : GLOBAL_MODULE_KEYS) {
            tag.remove(key);
        }
    }

    private static void copyTag(CompoundTag source, CompoundTag target, String key) {
        Tag value = source.get(key);
        if (value != null) {
            target.put(key, value.copy());
        }
    }

    private static boolean isModuleSlotKey(String key) {
        return !key.contains(":")
                && !GLOBAL_MODULE_KEYS.contains(key)
                && !key.endsWith("_material")
                && !key.endsWith("/settle_progress");
    }

    private static String shortId(String id) {
        int slash = id.lastIndexOf('/');
        return slash >= 0 ? id.substring(slash + 1) : id;
    }

    private static boolean isShiftDown() {
        // Same source as tetra modular tooltips; only called from client-side appendHoverText.
        return Screen.hasShiftDown();
    }

    private static MutableComponent prefix(String value, ChatFormatting formatting) {
        return Component.literal(value).withStyle(formatting);
    }

    private static MutableComponent moduleName(String moduleKey, @Nullable String material) {
        if (material == null || material.isEmpty()) {
            return Component.translatable("tetra.module." + moduleKey).withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable(
                "tetra.module." + moduleKey + ".material_name",
                Component.translatable("tetra.material." + shortId(material) + ".prefix")
        ).withStyle(ChatFormatting.GRAY);
    }

    private static String improvementName(String improvementKey, int level) {
        try {
            String name = IModularItem.getImprovementName(improvementKey, level);
            if (name != null && !name.isEmpty()) {
                String stripped = ChatFormatting.stripFormatting(name);
                return stripped == null || stripped.isEmpty() ? name : stripped;
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return improvementKey + (level > 0 ? " " + level : "");
    }

    private static Component enchantmentName(CompoundTag enchantmentTag) {
        String id = enchantmentTag.contains("id", Tag.TAG_STRING) ? enchantmentTag.getString("id") : "";
        int level = enchantmentTag.contains("lvl", Tag.TAG_ANY_NUMERIC)
                ? enchantmentTag.getInt("lvl")
                : 0;
        if (level <= 0 && enchantmentTag.contains("lvl", Tag.TAG_SHORT)) {
            level = enchantmentTag.getShort("lvl");
        }

        if (!id.isEmpty()) {
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(id));
            if (enchantment != null) {
                return enchantment.getFullname(Math.max(level, 1));
            }
        }
        return Component.literal(id.isEmpty() ? "?" : id + " " + level).withStyle(ChatFormatting.GRAY);
    }

    private static void appendEnchantmentLines(CompoundTag data, List<Component> tooltip, String slot,
                                               Set<String> displayedEnchantments) {
        if (!data.contains("Enchantments", Tag.TAG_LIST)) {
            return;
        }
        ListTag enchantments = data.getList("Enchantments", Tag.TAG_COMPOUND);
        CompoundTag mapping = data.contains("EnchantmentMapping", Tag.TAG_COMPOUND)
                ? data.getCompound("EnchantmentMapping")
                : null;
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantment = enchantments.getCompound(i);
            String id = enchantment.contains("id", Tag.TAG_STRING) ? enchantment.getString("id") : "";
            boolean belongsToSlot = mapping != null
                    && mapping.contains(id)
                    && slot.equals(mapping.get(id).getAsString());
            if (belongsToSlot && displayedEnchantments.add(id)) {
                tooltip.add(prefix(PREFIX_DETAIL, ChatFormatting.DARK_GRAY)
                        .append(enchantmentName(enchantment).copy().withStyle(ChatFormatting.DARK_GRAY)));
            }
        }
    }

    private static void appendStoredDataTooltip(CompoundTag data, List<Component> tooltip, boolean expanded) {
        List<String> moduleSlots = new ArrayList<>();
        for (String slot : KNOWN_SLOTS) {
            if (data.contains(slot, Tag.TAG_STRING)) {
                moduleSlots.add(slot);
            }
        }
        for (String key : data.getAllKeys()) {
            if (isModuleSlotKey(key) && !moduleSlots.contains(key)) {
                moduleSlots.add(key);
            }
        }

        int improvementTotal = 0;
        for (String key : data.getAllKeys()) {
            if (key.contains(":")) {
                improvementTotal++;
            }
        }
        int enchantmentTotal = data.contains("Enchantments", Tag.TAG_LIST)
                ? data.getList("Enchantments", Tag.TAG_COMPOUND).size()
                : 0;

        if (expanded) {
            // Detailed module and enchantment information stays under the shift marker.
            tooltip.add(Tooltips.expanded);
            tooltip.add(Component.translatable(
                    "item.slashbladetetra.modular_exchange.stats",
                    moduleSlots.size(),
                    improvementTotal,
                    enchantmentTotal
            ).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Tooltips.expand);

        Set<String> displayedEnchantments = new HashSet<>();

        for (String slot : moduleSlots) {
            String moduleKey = data.getString(slot);
            String materialKey = moduleKey + "_material";
            String material = data.contains(materialKey, Tag.TAG_STRING) ? data.getString(materialKey) : null;

            // Tetra major module line: "? " + module name
            tooltip.add(prefix(PREFIX_MAJOR, ChatFormatting.DARK_GRAY).append(moduleName(moduleKey, material)));

            List<String> improvementKeys = new ArrayList<>();
            String slotPrefix = slot + ":";
            for (String key : data.getAllKeys()) {
                if (key.startsWith(slotPrefix)) {
                    improvementKeys.add(key);
                }
            }
            improvementKeys.sort(Comparator.naturalOrder());

            for (String key : improvementKeys) {
                String improvementId = key.substring(slotPrefix.length());
                int level = data.contains(key, Tag.TAG_INT) ? data.getInt(key) : 0;
                // Tetra improvement line: "  - " + name
                tooltip.add(prefix(PREFIX_DETAIL, ChatFormatting.DARK_GRAY)
                        .append(Component.literal(improvementName(improvementId, level)).withStyle(ChatFormatting.DARK_GRAY)));
            }

            appendEnchantmentLines(data, tooltip, slot, displayedEnchantments);

        }

        if (data.contains("soul_slot") && data.getBoolean("soul_slot")) {
            // Tetra minor-module style: " * "
            tooltip.add(prefix(PREFIX_MINOR, ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("item.slashbladetetra.modular_exchange.soul_enabled")
                            .withStyle(ChatFormatting.GRAY)));
        }

        if (data.contains(IModularItem.repairCountKey)) {
            tooltip.add(prefix(PREFIX_DETAIL, ChatFormatting.DARK_GRAY)
                    .append(Component.translatable(
                            "item.slashbladetetra.modular_exchange.repair_count",
                            data.getInt(IModularItem.repairCountKey)
                    ).withStyle(ChatFormatting.DARK_GRAY)));
        }

        // Preserve enchantments from older data without a valid module mapping.
        if (data.contains("Enchantments", Tag.TAG_LIST)) {
            ListTag enchantments = data.getList("Enchantments", Tag.TAG_COMPOUND);
            for (int i = 0; i < enchantments.size(); i++) {
                CompoundTag enchantment = enchantments.getCompound(i);
                String id = enchantment.contains("id", Tag.TAG_STRING) ? enchantment.getString("id") : "";
                if (displayedEnchantments.add(id)) {
                    tooltip.add(prefix(PREFIX_DETAIL, ChatFormatting.DARK_GRAY)
                            .append(enchantmentName(enchantment).copy().withStyle(ChatFormatting.DARK_GRAY)));
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // Tetra-like short description lines
        tooltip.add(Component.translatable("item.slashbladetetra.modular_exchange.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.slashbladetetra.modular_exchange.tooltip_usage")
                .withStyle(ChatFormatting.DARK_GRAY));

        if (!hasModuleData(stack)) {
            tooltip.add(Component.translatable("item.tetra.modular.empty_slot")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("item.slashbladetetra.modular_exchange.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        boolean expanded = isShiftDown();
        appendStoredDataTooltip(getModuleData(stack), tooltip, expanded);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasModuleData(stack) || super.isFoil(stack);
    }
}
