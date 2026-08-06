package net.yiran.sbtetra.module.schematic;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE;

public class TintingSchematic implements UpgradeSchematic {
    public static Map<Item, Integer> ColorMap = new Object2ObjectOpenHashMap<>(16);

    static {
        for (DyeColor value : DyeColor.values()) {
            addColor(value);
        }
    }

    public GlyphData glyph = new GlyphData(64, 224);
    public String key = "tinting";
    public SchematicType schematicType = SchematicType.improvement;

    public static void addColor(DyeColor c) {
        ColorMap.put(DyeItem.byColor(c), c.getTextColor());
    }

    public static int mixRgb(List<Integer> colors) {
        int sumR = 0, sumG = 0, sumB = 0;
        for (int c : colors) {
            sumR += (c >> 16) & 0xFF;
            sumG += (c >> 8) & 0xFF;
            sumB += c & 0xFF;
        }

        int count = colors.size();
        int r = (sumR + count / 2) / count;
        int g = (sumG + count / 2) / count;
        int b = (sumB + count / 2) / count;

        return (r << 16) | (g << 8) | b;
    }

    @Override
    public ItemStack[] getSlotPlaceholders(ItemStack itemStack, int index) {
        return ColorMap.keySet().stream().map(Item::getDefaultInstance).toArray(ItemStack[]::new);
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getName() {
        return I18n.get("tetra/schematic/" + key + ".name");
    }

    @Override
    public String[] getSources() {
        return new String[]{"Tetra?"};
    }

    @Override
    public String getDescription(@Nullable ItemStack itemStack) {
        return I18n.get("tetra/schematic/" + key + ".description");
    }

    @Override
    public int getNumMaterialSlots() {
        return 3;
    }

    @Override
    public String getSlotName(ItemStack itemStack, int i) {
        return I18n.get("tetra/schematic/" + key + ".slot");
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int i, ItemStack itemStack1) {
        return 1;
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, String s, int i, ItemStack itemStack1) {
        return ColorMap.containsKey(itemStack1.getItem());
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String s, ItemStack[] itemStacks) {
        return Arrays.stream(itemStacks).filter(((Predicate<ItemStack>) ItemStack::isEmpty).negate()).map(ItemStack::getItem).allMatch(ColorMap::containsKey);
    }

    @Override
    public boolean isRelevant(ItemStack itemStack) {
        return itemStack.getItem() instanceof ISlashBladeTetra;
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        if (slot == null || !slot.equals("slashblade/tsuba")) return false;
        var state = targetStack.getCapability(BLADESTATE);
        return state.isPresent();
    }

    @Override
    public boolean canApplyUpgrade(Player player, ItemStack itemStack, ItemStack[] itemStacks, String s, Map<ToolAction, Integer> map) {
        return isMaterialsValid(itemStack, s, itemStacks);
    }

    @Override
    public boolean isIntegrityViolation(Player player, ItemStack itemStack, ItemStack[] itemStacks, String s) {
        return true;
    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] itemStacks, boolean b, String s, Player player) {
        //原理图应用逻辑，返回结果物品
        ItemStack newStack = itemStack.copy();
        if (b) {
            newStack.getCapability(BLADESTATE).ifPresent((bladeState) -> {
                List<Integer> color = new ArrayList<>();
                for (ItemStack stack : itemStacks) {
                    if (stack.isEmpty())
                        continue;
                    color.add(ColorMap.get(stack.getItem()));
                    stack.shrink(1);
                }
                bladeState.setEffectColor(new Color(mixRgb(color)));
            });
        }
        return newStack;
    }

    @Override
    public boolean checkTools(ItemStack itemStack, ItemStack[] itemStacks, Map<ToolAction, Integer> map) {
        return false;
    }

    @Override
    public Map<ToolAction, Integer> getRequiredToolLevels(ItemStack itemStack, ItemStack[] itemStacks) {
        return Map.of();
    }

    @Override
    public SchematicType getType() {
        return this.schematicType;
    }

    @Override
    public GlyphData getGlyph() {
        return this.glyph;
    }

    @Override
    public OutcomePreview[] getPreviews(ItemStack itemStack, String s) {
        return new OutcomePreview[0];
    }
}
