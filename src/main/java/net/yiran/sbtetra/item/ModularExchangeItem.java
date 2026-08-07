package net.yiran.sbtetra.item;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
import net.yiran.sbtetra.Config;
import org.jetbrains.annotations.NotNull;
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

    /** Per-crystal override compound. Example: {TransferSettings:{enchantments:true,proudSoul:true}}. */
    public static final String TRANSFER_SETTINGS_KEY = "TransferSettings";
    /** Snapshot of the policy used while the payload was stored. */
    public static final String TRANSFER_POLICY_KEY = "TransferPolicy";
    /** Selected SlashBlade capability values stored alongside the Tetra payload. */
    public static final String BLADE_STATE_DATA_KEY = "BladeStateData";

    public static final String TRANSFER_TETRA_KEY = "tetra";
    public static final String TRANSFER_ENCHANTMENTS_KEY = "enchantments";
    public static final String TRANSFER_PROUD_SOUL_KEY = "proudSoul";
    public static final String TRANSFER_KILL_COUNT_KEY = "killCount";
    public static final String TRANSFER_REFINE_KEY = "refine";

    private static final String BLADE_STATE_TAG_KEY = "bladeState";
    private static final String PROUD_SOUL_NBT_KEY = "proudSoul";
    private static final String KILL_COUNT_NBT_KEY = "killCount";
    private static final String REFINE_NBT_KEY = "RepairCounter";
    private static final String REFINE_DATA_KEY = "refine";

    private static final String[] KNOWN_SLOTS = {
            "slashblade/handle",
            "slashblade/blade",
            "slashblade/tsuba",
            "slashblade/scabbard",
            "slashblade/soul"
    };
    private static final Set<String> KNOWN_SLOT_SET = new HashSet<>(Arrays.asList(KNOWN_SLOTS));

    /* Tetra data that is not tied to one module slot. Enchantment data is handled separately. */
    private static final Set<String> TETRA_GLOBAL_KEYS = new LinkedHashSet<>(Arrays.asList(
            "soul_slot",
            IModularItem.repairCountKey,
            IModularItem.cooledStrengthKey,
            IModularItem.honeProgressKey,
            IModularItem.honeAvailableKey,
            IModularItem.honeCountKey,
            HONE_LIMIT_KEY
    ));
    private static final Set<String> ENCHANTMENT_KEYS = new LinkedHashSet<>(Arrays.asList(
            "Enchantments",
            "StoredEnchantments",
            "EnchantmentMapping"
    ));
    private static final Set<String> RESERVED_DATA_KEYS = new HashSet<>(Arrays.asList(
            TRANSFER_POLICY_KEY,
            BLADE_STATE_DATA_KEY
    ));

    private static final String PREFIX_MAJOR = "\u00bb ";
    private static final String PREFIX_MINOR = " * ";
    private static final String PREFIX_DETAIL = "  - ";
    private static final String PREFIX_TREE_BRANCH = "\u251c ";
    private static final String PREFIX_TREE_LAST = "\u2514 ";

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

    public static CompoundTag getTransferSettingsTag(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TRANSFER_SETTINGS_KEY, Tag.TAG_COMPOUND)) {
            return stack.getTag().getCompound(TRANSFER_SETTINGS_KEY).copy();
        }
        return new CompoundTag();
    }

    public static void setTransferSettings(ItemStack stack, CompoundTag settings) {
        if (settings.getAllKeys().isEmpty()) {
            stack.removeTagKey(TRANSFER_SETTINGS_KEY);
            return;
        }
        stack.getOrCreateTag().put(TRANSFER_SETTINGS_KEY, settings.copy());
    }

    public static void storeFromBlade(ItemStack exchange, ItemStack blade) {
        if (!(blade.getItem() instanceof ISlashBladeTetra modularBlade)) {
            return;
        }

        TransferSettings settings = TransferSettings.fromExchange(exchange);
        CompoundTag source = blade.getTag();
        CompoundTag moduleData = new CompoundTag();

        if (settings.tetraData) {
            captureTetraData(source, moduleData, modularBlade, blade);
        }
        if (settings.enchantments) {
            captureEnchantmentData(source, blade, moduleData);
        }
        if (settings.transfersBladeState()) {
            captureBladeStateData(source, blade, moduleData, settings);
        }
        captureAdditionalData(source, moduleData, settings);

        // The policy is part of the snapshot so later config changes do not alter an already stored crystal.
        moduleData.put(TRANSFER_POLICY_KEY, settings.toTag());

        CompoundTag tag = exchange.getOrCreateTag();
        tag.put(MODULE_DATA_KEY, moduleData);
        tag.putBoolean(HAS_DATA_KEY, true);
    }

    private static void captureTetraData(@Nullable CompoundTag source, CompoundTag moduleData,
                                         ISlashBladeTetra modularBlade, ItemStack blade) {
        Set<String> copiedKeys = new HashSet<>();
        if (source != null) {
            Stream.concat(
                    Arrays.stream(safeKeys(modularBlade.getMajorModuleKeys(blade))),
                    Arrays.stream(safeKeys(modularBlade.getMinorModuleKeys(blade)))
            ).filter(key -> key != null && !key.isEmpty())
                    .distinct()
                    .forEach(slot -> copySlotData(source, moduleData, modularBlade, blade, slot, copiedKeys));

            for (String slot : KNOWN_SLOTS) {
                if (!copiedKeys.contains(slot) && source.contains(slot, Tag.TAG_STRING)) {
                    copySlotData(source, moduleData, modularBlade, blade, slot, copiedKeys);
                }
            }

            for (String key : TETRA_GLOBAL_KEYS) {
                if (source.contains(key)) {
                    copyTag(source, moduleData, key);
                }
            }
        }

        // This is display-only; Tetra recalculates the actual limit from item properties on application.
        moduleData.putInt(HONE_LIMIT_KEY, modularBlade.getHoningLimit(blade));
        if (source != null && source.contains(IModularItem.honeProgressKey, Tag.TAG_ANY_NUMERIC)) {
            moduleData.putInt(IModularItem.honeProgressKey, source.getInt(IModularItem.honeProgressKey));
        } else {
            moduleData.putInt(IModularItem.honeProgressKey, modularBlade.getHoningProgress(blade));
        }
    }

    private static String[] safeKeys(@Nullable String[] keys) {
        return keys == null ? new String[0] : keys;
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

    private static void captureEnchantmentData(@Nullable CompoundTag source, ItemStack blade, CompoundTag moduleData) {
        if (source != null) {
            for (String key : ENCHANTMENT_KEYS) {
                copyTag(source, moduleData, key);
            }
        }

        // ItemStack exposes the live vanilla enchantment list, which is more reliable than a stale root tag.
        ListTag enchantments = blade.getEnchantmentTags();
        if (!enchantments.isEmpty()) {
            moduleData.put("Enchantments", enchantments.copy());
        }
    }

    private static void captureBladeStateData(@Nullable CompoundTag source, ItemStack blade, CompoundTag moduleData,
                                              TransferSettings settings) {
        CompoundTag sourceState = source != null && source.contains(BLADE_STATE_TAG_KEY, Tag.TAG_COMPOUND)
                ? source.getCompound(BLADE_STATE_TAG_KEY)
                : new CompoundTag();
        ISlashBladeState state = blade.getCapability(ItemSlashBlade.BLADESTATE).orElse(null);
        CompoundTag stateData = new CompoundTag();

        if (settings.proudSoul) {
            int value = state.getProudSoulCount();
            stateData.putInt(PROUD_SOUL_NBT_KEY, value);
        }
        if (settings.killCount) {
            int value = state.getKillCount();
            stateData.putInt(KILL_COUNT_NBT_KEY, value);
        }
        if (settings.refine) {
            int value = state.getRefine();
            putRefineValue(stateData, value);
        }

        if (!stateData.getAllKeys().isEmpty()) {
            moduleData.put(BLADE_STATE_DATA_KEY, stateData);
        }
    }

    private static void captureAdditionalData(@Nullable CompoundTag source, CompoundTag moduleData,
                                              TransferSettings settings) {
        if (source == null) {
            return;
        }
        for (String key : settings.additionalNbtKeys) {
            if (isSafeAdditionalKey(key)) {
                copyTag(source, moduleData, key);
            }
        }
    }

    public static void applyToBlade(ItemStack exchange, ItemStack blade) {
        if (!(blade.getItem() instanceof ISlashBladeTetra modularBlade) || !hasModuleData(exchange)) {
            return;
        }

        CompoundTag moduleData = getModuleData(exchange);
        TransferSettings settings = TransferSettings.fromStoredData(moduleData);
        boolean changedTetraData = false;

        if (settings.tetraData && hasTetraPayload(moduleData)) {
            CompoundTag preservedEnchantments = settings.enchantments ? new CompoundTag() : getEnchantmentData(blade.getTag());
            clearTetraData(blade, modularBlade);
            copyStoredTetraData(moduleData, blade.getOrCreateTag());
            if (!settings.enchantments) {
                restoreEnchantmentData(blade, preservedEnchantments);
            }
            changedTetraData = true;
        }

        if (settings.enchantments) {
            clearEnchantmentData(blade);
            copyStoredEnchantmentData(moduleData, blade.getOrCreateTag());
        }

        CompoundTag stateData = getStoredBladeStateData(moduleData);
        if (settings.transfersBladeState() && !stateData.getAllKeys().isEmpty()) {
            applyBladeStateData(blade, stateData, settings);
        }
        applyAdditionalData(moduleData, blade, settings);

        if (changedTetraData) {
            IModularItem.updateIdentifier(blade);
            modularBlade.clearCaches();
            modularBlade.assemble(blade, null, 0f);
        }
    }

    public static void swapWithBlade(ItemStack exchange, ItemStack blade) {
        if (!(blade.getItem() instanceof ISlashBladeTetra modularBlade)) {
            return;
        }

        CompoundTag previous = hasModuleData(exchange) ? getModuleData(exchange) : null;
        storeFromBlade(exchange, blade);

        if (previous == null) {
            CompoundTag stored = getModuleData(exchange);
            TransferSettings settings = TransferSettings.fromStoredData(stored);
            resetTransferredData(blade, modularBlade, settings);
            return;
        }

        ItemStack temp = exchange.copy();
        temp.getOrCreateTag().put(MODULE_DATA_KEY, previous);
        temp.getOrCreateTag().putBoolean(HAS_DATA_KEY, true);
        applyToBlade(temp, blade);
    }

    private static void resetTransferredData(ItemStack blade, ISlashBladeTetra modularBlade, TransferSettings settings) {
        if (settings.tetraData) {
            CompoundTag preservedEnchantments = settings.enchantments ? new CompoundTag() : getEnchantmentData(blade.getTag());
            clearTetraData(blade, modularBlade);
            ISlashBladeTetra.putDefaultModule(blade);
            if (!settings.enchantments) {
                restoreEnchantmentData(blade, preservedEnchantments);
            }
            IModularItem.updateIdentifier(blade);
            modularBlade.clearCaches();
            modularBlade.assemble(blade, null, 0f);
        }

        if (settings.enchantments) {
            clearEnchantmentData(blade);
        }
        if (settings.transfersBladeState()) {
            clearBladeStateData(blade, settings);
        }
        clearAdditionalData(blade, settings);
    }

    private static void copyStoredTetraData(CompoundTag source, CompoundTag target) {
        for (String key : source.getAllKeys()) {
            // The hone limit is only used by the crystal tooltip; Tetra calculates it from modules.
            if (!HONE_LIMIT_KEY.equals(key) && isTetraPayloadKey(source, key)) {
                copyTag(source, target, key);
            }
        }
    }

    private static void copyStoredEnchantmentData(CompoundTag source, CompoundTag target) {
        for (String key : ENCHANTMENT_KEYS) {
            copyTag(source, target, key);
        }
    }

    private static void applyAdditionalData(CompoundTag source, ItemStack blade, TransferSettings settings) {
        CompoundTag target = blade.getOrCreateTag();
        for (String key : settings.additionalNbtKeys) {
            if (!isSafeAdditionalKey(key)) {
                continue;
            }
            target.remove(key);
            copyTag(source, target, key);
            if (BLADE_STATE_TAG_KEY.equals(key) && target.contains(BLADE_STATE_TAG_KEY, Tag.TAG_COMPOUND)) {
                blade.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(
                        state -> state.deserializeNBT(target.getCompound(BLADE_STATE_TAG_KEY))
                );
            }
        }
    }

    private static void clearAdditionalData(ItemStack blade, TransferSettings settings) {
        CompoundTag target = blade.getOrCreateTag();
        for (String key : settings.additionalNbtKeys) {
            if (!isSafeAdditionalKey(key)) {
                continue;
            }
            target.remove(key);
            if (BLADE_STATE_TAG_KEY.equals(key)) {
                blade.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> state.deserializeNBT(new CompoundTag()));
            }
        }
    }

    private static void clearTetraData(ItemStack blade, ISlashBladeTetra modularBlade) {
        CompoundTag tag = blade.getOrCreateTag();

        Set<String> slots = new HashSet<>();
        slots.addAll(Arrays.asList(safeKeys(modularBlade.getMajorModuleKeys(blade))));
        slots.addAll(Arrays.asList(safeKeys(modularBlade.getMinorModuleKeys(blade))));
        slots.addAll(KNOWN_SLOT_SET);

        for (String slot : slots) {
            if (slot == null || slot.isEmpty()) {
                continue;
            }
            ItemModule module = modularBlade.getModuleFromSlot(blade, slot);
            String materialKey = tag.contains(slot, Tag.TAG_STRING) ? tag.getString(slot) + "_material" : null;

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

        for (String key : TETRA_GLOBAL_KEYS) {
            tag.remove(key);
        }
    }

    private static CompoundTag getEnchantmentData(@Nullable CompoundTag source) {
        CompoundTag result = new CompoundTag();
        if (source != null) {
            for (String key : ENCHANTMENT_KEYS) {
                copyTag(source, result, key);
            }
        }
        return result;
    }

    private static void restoreEnchantmentData(ItemStack blade, CompoundTag data) {
        clearEnchantmentData(blade);
        copyStoredEnchantmentData(data, blade.getOrCreateTag());
    }

    private static void clearEnchantmentData(ItemStack blade) {
        for (String key : ENCHANTMENT_KEYS) {
            blade.removeTagKey(key);
        }
    }

    private static CompoundTag getStoredBladeStateData(CompoundTag data) {
        if (data.contains(BLADE_STATE_DATA_KEY, Tag.TAG_COMPOUND)) {
            return data.getCompound(BLADE_STATE_DATA_KEY);
        }
        if (data.contains(BLADE_STATE_TAG_KEY, Tag.TAG_COMPOUND)) {
            return data.getCompound(BLADE_STATE_TAG_KEY);
        }

        // Accept simple legacy/custom snapshots that stored these values at the payload root.
        CompoundTag result = new CompoundTag();
        copyTag(data, result, PROUD_SOUL_NBT_KEY);
        copyTag(data, result, KILL_COUNT_NBT_KEY);
        if (data.contains(REFINE_DATA_KEY, Tag.TAG_ANY_NUMERIC)) {
            result.putInt(REFINE_DATA_KEY, data.getInt(REFINE_DATA_KEY));
        }
        if (data.contains(REFINE_NBT_KEY, Tag.TAG_ANY_NUMERIC)) {
            result.putInt(REFINE_NBT_KEY, data.getInt(REFINE_NBT_KEY));
        }
        return result;
    }

    private static void applyBladeStateData(ItemStack blade, CompoundTag data, TransferSettings settings) {
        CompoundTag targetTag = blade.getOrCreateTag();
        CompoundTag targetState = targetTag.contains(BLADE_STATE_TAG_KEY, Tag.TAG_COMPOUND)
                ? targetTag.getCompound(BLADE_STATE_TAG_KEY).copy()
                : new CompoundTag();
        ISlashBladeState state = blade.getCapability(ItemSlashBlade.BLADESTATE).orElse(null);
        boolean changed = false;

        if (settings.proudSoul && data.contains(PROUD_SOUL_NBT_KEY, Tag.TAG_ANY_NUMERIC)) {
            int value = data.getInt(PROUD_SOUL_NBT_KEY);
            targetState.putInt(PROUD_SOUL_NBT_KEY, value);
            if (state != null) {
                state.setProudSoulCount(value);
            }
            changed = true;
        }
        if (settings.killCount && data.contains(KILL_COUNT_NBT_KEY, Tag.TAG_ANY_NUMERIC)) {
            int value = data.getInt(KILL_COUNT_NBT_KEY);
            targetState.putInt(KILL_COUNT_NBT_KEY, value);
            if (state != null) {
                state.setKillCount(value);
            }
            changed = true;
        }
        if (settings.refine && hasRefineValue(data)) {
            int value = getRefineValue(data);
            targetState.putInt(REFINE_NBT_KEY, value);
            if (state != null) {
                state.setRefine(value);
            }
            changed = true;
        }

        if (changed) {
            targetTag.put(BLADE_STATE_TAG_KEY, targetState);
        }
    }

    private static void clearBladeStateData(ItemStack blade, TransferSettings settings) {
        CompoundTag targetTag = blade.getOrCreateTag();
        CompoundTag targetState = targetTag.contains(BLADE_STATE_TAG_KEY, Tag.TAG_COMPOUND)
                ? targetTag.getCompound(BLADE_STATE_TAG_KEY).copy()
                : new CompoundTag();
        ISlashBladeState state = blade.getCapability(ItemSlashBlade.BLADESTATE).orElse(null);
        boolean changed = false;

        if (settings.proudSoul) {
            targetState.putInt(PROUD_SOUL_NBT_KEY, 0);
            state.setProudSoulCount(0);
            changed = true;
        }
        if (settings.killCount) {
            targetState.putInt(KILL_COUNT_NBT_KEY, 0);
            state.setKillCount(0);
            changed = true;
        }
        if (settings.refine) {
            targetState.putInt(REFINE_NBT_KEY, 0);
            state.setRefine(0);
            changed = true;
        }

        if (changed) {
            targetTag.put(BLADE_STATE_TAG_KEY, targetState);
        }
    }

    private static boolean hasRefineValue(CompoundTag data) {
        return data.contains(REFINE_DATA_KEY, Tag.TAG_ANY_NUMERIC)
                || data.contains(REFINE_NBT_KEY, Tag.TAG_ANY_NUMERIC);
    }

    @Nullable
    private static Integer getRefineValue(CompoundTag data) {
        if (data.contains(REFINE_DATA_KEY, Tag.TAG_ANY_NUMERIC)) {
            return data.getInt(REFINE_DATA_KEY);
        }
        return getNumericValue(data, REFINE_NBT_KEY);
    }

    private static void putRefineValue(CompoundTag data, int value) {
        // Keep the human-readable key and SlashBlade's serialized key for compatibility with external NBT tools.
        data.putInt(REFINE_DATA_KEY, value);
        data.putInt(REFINE_NBT_KEY, value);
    }

    @Nullable
    private static Integer getNumericValue(CompoundTag data, String key) {
        return data.contains(key, Tag.TAG_ANY_NUMERIC) ? data.getInt(key) : null;
    }

    private static void copyTag(@Nullable CompoundTag source, CompoundTag target, String key) {
        if (source == null) {
            return;
        }
        Tag value = source.get(key);
        if (value != null) {
            target.put(key, value.copy());
        }
    }

    private static boolean isSafeAdditionalKey(String key) {
        return key != null
                && !key.isBlank()
                && !MODULE_DATA_KEY.equals(key)
                && !HAS_DATA_KEY.equals(key)
                && !TRANSFER_SETTINGS_KEY.equals(key)
                && !TRANSFER_POLICY_KEY.equals(key)
                && !BLADE_STATE_DATA_KEY.equals(key);
    }

    private static boolean isTetraPayloadKey(CompoundTag data, String key) {
        if (RESERVED_DATA_KEYS.contains(key) || ENCHANTMENT_KEYS.contains(key)) {
            return false;
        }
        return TETRA_GLOBAL_KEYS.contains(key) || isModuleRelatedKey(data, key);
    }

    private static boolean hasTetraPayload(CompoundTag data) {
        for (String key : data.getAllKeys()) {
            if (isTetraPayloadKey(data, key)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isModuleRelatedKey(CompoundTag data, String key) {
        return key.contains(":")
                || key.endsWith("_material")
                || key.endsWith("/settle_progress")
                || isModuleSlotKey(data, key);
    }

    private static boolean isModuleSlotKey(CompoundTag data, String key) {
        if (!data.contains(key, Tag.TAG_STRING) || RESERVED_DATA_KEYS.contains(key)
                || TETRA_GLOBAL_KEYS.contains(key) || ENCHANTMENT_KEYS.contains(key)
                || key.contains(":") || key.endsWith("_material") || key.endsWith("/settle_progress")) {
            return false;
        }
        String moduleKey = data.getString(key);
        return KNOWN_SLOT_SET.contains(key) || key.contains("/") || moduleKey.contains("/");
    }

    private static boolean isShiftDown() {
        // Same source as Tetra modular tooltips; only called from client-side appendHoverText.
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

    private static String shortId(String id) {
        int slash = id.lastIndexOf('/');
        return slash >= 0 ? id.substring(slash + 1) : id;
    }

    private static String improvementName(String improvementKey, int level) {
        try {
            String name = IModularItem.getImprovementName(improvementKey, level);
            if (name != null && !name.isEmpty()) {
                String stripped = ChatFormatting.stripFormatting(name);
                return stripped == null || stripped.isEmpty() ? name : stripped;
            }
        } catch (Throwable ignored) {
            // A missing optional module should not prevent the crystal tooltip from rendering.
        }
        return improvementKey + (level > 0 ? " " + level : "");
    }

    private static Component enchantmentName(CompoundTag enchantmentTag) {
        String id = enchantmentTag.contains("id", Tag.TAG_STRING) ? enchantmentTag.getString("id") : "";
        int level = enchantmentTag.contains("lvl", Tag.TAG_ANY_NUMERIC) ? enchantmentTag.getInt("lvl") : 0;

        if (!id.isEmpty()) {
            try {
                Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(id));
                if (enchantment != null) {
                    return enchantment.getFullname(Math.max(level, 1));
                }
            } catch (RuntimeException ignored) {
                // Fall through to a literal id for malformed or unloaded enchantment ids.
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

    private static int getEnchantmentCount(CompoundTag data) {
        int count = 0;
        if (data.contains("Enchantments", Tag.TAG_LIST)) {
            count += data.getList("Enchantments", Tag.TAG_COMPOUND).size();
        }
        if (data.contains("StoredEnchantments", Tag.TAG_LIST)) {
            count += data.getList("StoredEnchantments", Tag.TAG_COMPOUND).size();
        }
        return count;
    }

    private static void appendTransferTooltip(TransferSettings settings, @Nullable CompoundTag data,
                                              List<Component> tooltip) {
        List<Component> entries = new ArrayList<>();
        CompoundTag stateData = data == null ? null : getStoredBladeStateData(data);

        if (settings.tetraData) {
            entries.add(Component.translatable("item.slashbladetetra.modular_exchange.transfer_tetra"));
        }
        if (settings.enchantments) {
            entries.add(data == null
                    ? Component.translatable("item.slashbladetetra.modular_exchange.transfer_enchantments")
                    : Component.translatable(
                            "item.slashbladetetra.modular_exchange.enchantment_count",
                            getEnchantmentCount(data)
                    ));
        }
        if (settings.proudSoul) {
            entries.add(stateData != null && stateData.contains(PROUD_SOUL_NBT_KEY, Tag.TAG_ANY_NUMERIC)
                    ? Component.translatable(
                            "item.slashbladetetra.modular_exchange.proud_soul",
                            stateData.getInt(PROUD_SOUL_NBT_KEY)
                    )
                    : Component.translatable("item.slashbladetetra.modular_exchange.transfer_proud_soul"));
        }
        if (settings.killCount) {
            entries.add(stateData != null && stateData.contains(KILL_COUNT_NBT_KEY, Tag.TAG_ANY_NUMERIC)
                    ? Component.translatable(
                            "item.slashbladetetra.modular_exchange.kill_count",
                            stateData.getInt(KILL_COUNT_NBT_KEY)
                    )
                    : Component.translatable("item.slashbladetetra.modular_exchange.transfer_kill_count"));
        }
        if (settings.refine) {
            entries.add(stateData != null && hasRefineValue(stateData)
                    ? Component.translatable(
                            "item.slashbladetetra.modular_exchange.refine_count",
                            getRefineValue(stateData)
                    )
                    : Component.translatable("item.slashbladetetra.modular_exchange.transfer_refine"));
        }

        if (!settings.additionalNbtKeys.isEmpty()) {
            entries.add(Component.translatable(
                    "item.slashbladetetra.modular_exchange.transfer_additional",
                    String.join(", ", settings.additionalNbtKeys)
            ));
        }

        if (entries.isEmpty()) {
            entries.add(Component.translatable("item.slashbladetetra.modular_exchange.transfer_none"));
        }

        tooltip.add(Component.translatable("item.slashbladetetra.modular_exchange.transfer_data")
                .withStyle(ChatFormatting.GRAY));
        for (int index = 0; index < entries.size(); index++) {
            String prefix = index == entries.size() - 1 ? PREFIX_TREE_LAST : PREFIX_TREE_BRANCH;
            tooltip.add(prefix(prefix, ChatFormatting.DARK_GRAY)
                    .append(entries.get(index).copy().withStyle(ChatFormatting.GRAY)));
        }
    }

    private static void appendStoredDataTooltip(CompoundTag data, List<Component> tooltip, boolean expanded) {
        TransferSettings settings = TransferSettings.fromStoredData(data);
        List<String> moduleSlots = new ArrayList<>();
        for (String slot : KNOWN_SLOTS) {
            if (isModuleSlotKey(data, slot)) {
                moduleSlots.add(slot);
            }
        }
        for (String key : data.getAllKeys()) {
            if (isModuleSlotKey(data, key) && !moduleSlots.contains(key)) {
                moduleSlots.add(key);
            }
        }

        int improvementTotal = 0;
        for (String key : data.getAllKeys()) {
            if (key.contains(":")) {
                improvementTotal++;
            }
        }
        int enchantmentTotal = getEnchantmentCount(data);

        appendTransferTooltip(settings, data, tooltip);

        if (expanded) {
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
                int level = data.contains(key, Tag.TAG_ANY_NUMERIC) ? data.getInt(key) : 0;
                tooltip.add(prefix(PREFIX_DETAIL, ChatFormatting.DARK_GRAY)
                        .append(Component.literal(improvementName(improvementId, level)).withStyle(ChatFormatting.DARK_GRAY)));
            }

            if (settings.enchantments) {
                appendEnchantmentLines(data, tooltip, slot, displayedEnchantments);
            }
        }

        if (data.contains("soul_slot") && data.getBoolean("soul_slot")) {
            tooltip.add(prefix(PREFIX_MINOR, ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("item.slashbladetetra.modular_exchange.soul_enabled")
                            .withStyle(ChatFormatting.GRAY)));
        }
        if (data.contains(IModularItem.repairCountKey, Tag.TAG_ANY_NUMERIC)) {
            tooltip.add(prefix(PREFIX_DETAIL, ChatFormatting.DARK_GRAY)
                    .append(Component.translatable(
                            "item.slashbladetetra.modular_exchange.repair_count",
                            data.getInt(IModularItem.repairCountKey)
                    ).withStyle(ChatFormatting.DARK_GRAY)));
        }

        // Preserve old snapshots without a valid Tetra enchantment mapping.
        if (settings.enchantments && data.contains("Enchantments", Tag.TAG_LIST)) {
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

    private static boolean hasEnchantmentPayload(CompoundTag data) {
        for (String key : ENCHANTMENT_KEYS) {
            if (data.contains(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.translatable("item.slashbladetetra.modular_exchange.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.slashbladetetra.modular_exchange.tooltip_usage")
                .withStyle(ChatFormatting.DARK_GRAY));

        if (!hasModuleData(stack)) {
            appendTransferTooltip(TransferSettings.fromExchange(stack), null, tooltip);
            tooltip.add(Component.translatable("item.tetra.modular.empty_slot")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("item.slashbladetetra.modular_exchange.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        appendStoredDataTooltip(getModuleData(stack), tooltip, isShiftDown());
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return hasModuleData(stack) || super.isFoil(stack);
    }

    private static final class TransferSettings {
        private final LinkedHashSet<String> additionalNbtKeys;
        private boolean tetraData;
        private boolean enchantments;
        private boolean proudSoul;
        private boolean killCount;
        private boolean refine;

        private TransferSettings(boolean tetraData, boolean enchantments, boolean proudSoul, boolean killCount,
                                 boolean refine, Collection<String> additionalNbtKeys) {
            this.tetraData = tetraData;
            this.enchantments = enchantments;
            this.proudSoul = proudSoul;
            this.killCount = killCount;
            this.refine = refine;
            this.additionalNbtKeys = new LinkedHashSet<>();
            for (String key : additionalNbtKeys) {
                if (isSafeAdditionalKey(key)) {
                    this.additionalNbtKeys.add(key);
                }
            }
        }

        private static TransferSettings fromConfig() {
            boolean tetraData = true;
            boolean enchantments = false;
            boolean proudSoul = false;
            boolean killCount = false;
            boolean refine = false;
            List<String> additional = new ArrayList<>();

            try {
                tetraData = Config.Server.ModularExchangeTransferTetraData.get();
                enchantments = Config.Server.ModularExchangeTransferEnchantments.get();
                proudSoul = Config.Server.ModularExchangeTransferProudSoul.get();
                killCount = Config.Server.ModularExchangeTransferKillCount.get();
                refine = Config.Server.ModularExchangeTransferRefine.get();
                additional.addAll(Config.Server.ModularExchangeAdditionalNbtKeys.get());
            } catch (RuntimeException ignored) {
                // Tooltips can be constructed before a remote server config has arrived; use documented defaults then.
            }
            return new TransferSettings(tetraData, enchantments, proudSoul, killCount, refine, additional);
        }

        private static TransferSettings fromExchange(ItemStack exchange) {
            TransferSettings result = fromConfig();
            CompoundTag root = exchange.getTag();
            if (root == null) {
                return result;
            }
            if (root.contains(TRANSFER_SETTINGS_KEY, Tag.TAG_COMPOUND)) {
                result.applyOverrides(root.getCompound(TRANSFER_SETTINGS_KEY), true);
            }
            if (root.contains("TransferData", Tag.TAG_COMPOUND)) {
                result.applyOverrides(root.getCompound("TransferData"), true);
            }
            // Direct root keys are convenient for commands and datapacks, while the compound avoids collisions.
            result.applyOverrides(root, false);
            return result;
        }

        private static TransferSettings fromStoredData(CompoundTag data) {
            if (data.contains(TRANSFER_POLICY_KEY, Tag.TAG_COMPOUND)) {
                return fromPolicy(data.getCompound(TRANSFER_POLICY_KEY));
            }

            // Older crystals did not carry a policy. Infer categories from their payload for backward compatibility.
            boolean tetraData = hasTetraPayload(data);
            CompoundTag stateData = getStoredBladeStateData(data);
            return new TransferSettings(
                    tetraData,
                    hasEnchantmentPayload(data),
                    stateData.contains(PROUD_SOUL_NBT_KEY, Tag.TAG_ANY_NUMERIC),
                    stateData.contains(KILL_COUNT_NBT_KEY, Tag.TAG_ANY_NUMERIC),
                    hasRefineValue(stateData),
                    List.of()
            );
        }

        private static TransferSettings fromPolicy(CompoundTag policy) {
            TransferSettings result = new TransferSettings(false, false, false, false, false, List.of());
            result.tetraData = getBooleanOrDefault(policy, TRANSFER_TETRA_KEY, false);
            result.enchantments = getBooleanOrDefault(policy, TRANSFER_ENCHANTMENTS_KEY, false);
            result.proudSoul = getBooleanOrDefault(policy, TRANSFER_PROUD_SOUL_KEY, false);
            result.killCount = getBooleanOrDefault(policy, TRANSFER_KILL_COUNT_KEY, false);
            result.refine = getBooleanOrDefault(policy, TRANSFER_REFINE_KEY, false);
            List<String> additional = readStringList(policy, "additionalNbtKeys");
            if (additional != null) {
                for (String key : additional) {
                    if (isSafeAdditionalKey(key)) {
                        result.additionalNbtKeys.add(key);
                    }
                }
            }
            return result;
        }

        @Nullable
        private static Boolean readBoolean(CompoundTag tag, String... keys) {
            for (String key : keys) {
                if (tag.contains(key, Tag.TAG_ANY_NUMERIC)) {
                    return tag.getBoolean(key);
                }
            }
            return null;
        }

        private static boolean getBooleanOrDefault(CompoundTag tag, String key, boolean defaultValue) {
            Boolean value = readBoolean(tag, key);
            return value == null ? defaultValue : value;
        }

        @Nullable
        private static List<String> readStringList(CompoundTag tag, String... keys) {
            for (String key : keys) {
                if (!tag.contains(key, Tag.TAG_LIST)) {
                    continue;
                }
                ListTag list = tag.getList(key, Tag.TAG_STRING);
                List<String> result = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    result.add(list.getString(i));
                }
                return result;
            }
            return null;
        }

        private void applyOverrides(CompoundTag tag, boolean allowShortNames) {
            List<String> categories = readStringList(tag,
                    "transferData", "TransferData", "data", "Data", "categories", "Categories");
            if (categories != null) {
                tetraData = false;
                enchantments = false;
                proudSoul = false;
                killCount = false;
                refine = false;
                for (String category : categories) {
                    applyCategory(category);
                }
            }

            Boolean value = readBoolean(tag, "transferTetraData", "TransferTetraData");
            if (value == null && allowShortNames) {
                value = readBoolean(tag, TRANSFER_TETRA_KEY);
            }
            if (value != null) {
                tetraData = value;
            }

            value = readBoolean(tag, "transferEnchantments", "TransferEnchantments");
            if (value == null && allowShortNames) {
                value = readBoolean(tag, TRANSFER_ENCHANTMENTS_KEY, "enchantments");
            }
            if (value != null) {
                enchantments = value;
            }

            value = readBoolean(tag, "transferProudSoul", "TransferProudSoul");
            if (value == null && allowShortNames) {
                value = readBoolean(tag, TRANSFER_PROUD_SOUL_KEY, "proud_soul");
            }
            if (value != null) {
                proudSoul = value;
            }

            value = readBoolean(tag, "transferKillCount", "TransferKillCount");
            if (value == null && allowShortNames) {
                value = readBoolean(tag, TRANSFER_KILL_COUNT_KEY, "kill_count");
            }
            if (value != null) {
                killCount = value;
            }

            value = readBoolean(tag, "transferRefine", "TransferRefine", "transferForge", "TransferForge");
            if (value == null && allowShortNames) {
                value = readBoolean(tag, TRANSFER_REFINE_KEY, "forge", "forging");
            }
            if (value != null) {
                refine = value;
            }

            List<String> additional = readStringList(tag,
                    "additionalNbtKeys", "AdditionalNbtKeys", "additionalKeys", "AdditionalKeys");
            if (additional != null) {
                for (String key : additional) {
                    if (isSafeAdditionalKey(key)) {
                        additionalNbtKeys.add(key);
                    }
                }
            }
        }

        private void applyCategory(String category) {
            if (category == null || category.isBlank()) {
                return;
            }
            String normalized = category.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
            switch (normalized) {
                case "all" -> {
                    tetraData = true;
                    enchantments = true;
                    proudSoul = true;
                    killCount = true;
                    refine = true;
                }
                case "none" -> {
                    tetraData = false;
                    enchantments = false;
                    proudSoul = false;
                    killCount = false;
                    refine = false;
                }
                case "tetra", "tetradata", "module", "modules" -> tetraData = true;
                case "enchantment", "enchantments", "ench" -> enchantments = true;
                case "proudsoul", "soul", "souls" -> proudSoul = true;
                case "killcount", "kill", "kills" -> killCount = true;
                case "refine", "refinement", "forge", "forging", "refinecount" -> refine = true;
                default -> {
                    if (isSafeAdditionalKey(category)) {
                        additionalNbtKeys.add(category);
                    }
                }
            }
        }

        private boolean transfersBladeState() {
            return proudSoul || killCount || refine;
        }

        private CompoundTag toTag() {
            CompoundTag result = new CompoundTag();
            result.putBoolean(TRANSFER_TETRA_KEY, tetraData);
            result.putBoolean(TRANSFER_ENCHANTMENTS_KEY, enchantments);
            result.putBoolean(TRANSFER_PROUD_SOUL_KEY, proudSoul);
            result.putBoolean(TRANSFER_KILL_COUNT_KEY, killCount);
            result.putBoolean(TRANSFER_REFINE_KEY, refine);
            if (!additionalNbtKeys.isEmpty()) {
                ListTag additional = new ListTag();
                for (String key : additionalNbtKeys) {
                    additional.add(StringTag.valueOf(key));
                }
                result.put("additionalNbtKeys", additional);
            }
            return result;
        }
    }
}
