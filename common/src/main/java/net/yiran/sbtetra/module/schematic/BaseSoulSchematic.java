package net.yiran.sbtetra.module.schematic;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.ForgeRegistries;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Collections;
import java.util.Map;

public abstract class BaseSoulSchematic implements UpgradeSchematic {
    public GlyphData glyph;
    public String key;
    public SchematicType schematicType = SchematicType.other;
    public TagKey<Item> unLimitMaterial;

    public BaseSoulSchematic(GlyphData glyph, String key, TagKey<Item> unLimitMaterial) {
        this.glyph = glyph;
        this.key = key;
        this.unLimitMaterial = unLimitMaterial;
    }

    @Override
    public ItemStack[] getSlotPlaceholders(ItemStack itemStack, int index) {
        return ForgeRegistries.ITEMS.tags().getTag(unLimitMaterial).stream().map(Item::getDefaultInstance).toArray(ItemStack[]::new);
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

    public abstract Object[] getDescriptionExtraValues(@Nullable ItemStack itemStack);

    @Override
    public String getDescription(@Nullable ItemStack itemStack) {
        return I18n.get("tetra/schematic/" + key + ".description", getDescriptionExtraValues(itemStack));
    }

    @Override
    public int getNumMaterialSlots() {
        return 1;
    }

    @Override
    public String getSlotName(ItemStack itemStack, int index) {
        return I18n.get("tetra/schematic/" + key + ".slot");
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int i, ItemStack materialStack) {
        return 1;
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, String s, int i, ItemStack materialStack) {
        return materialStack.is(unLimitMaterial);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String s, ItemStack[] materials) {
        return false;
    }

    @Override
    public boolean isRelevant(ItemStack itemStack) {
        return itemStack.getItem() instanceof ISlashBladeTetra;
    }

    @Override
    public boolean matchesRequirements(CraftingContext context) {
        return IModularItem.isHoneable(context.targetStack);
    }

    @Override
    public boolean canApplyUpgrade(Player player, ItemStack itemStack, ItemStack[] itemStacks, String s, Map<ToolAction, Integer> map) {
        return true;
    }

    @Override
    public boolean isIntegrityViolation(Player player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return true;
    }

    @Override
    public boolean isHoning() {
        return true;
    }

    @Override
    public boolean checkTools(ItemStack targetStack, ItemStack[] materials, Map<ToolAction, Integer> availableTools) {
        return this.getRequiredToolLevels(targetStack, materials).entrySet().stream()
                .allMatch((entry) -> availableTools.getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    @Override
    public Map<ToolAction, Integer> getRequiredToolLevels(ItemStack itemStack, ItemStack[] itemStacks) {
        return Collections.singletonMap(TetraToolActions.hammer, 1);
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
