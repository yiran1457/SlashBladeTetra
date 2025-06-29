package net.yiran.sbtetra.module;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.init.SBItems;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.yiran.sbtetra.item.SlashBladeModularItem;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Collections;
import java.util.Map;

import static mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE;
import static net.yiran.sbtetra.compat.cialloblade.CialloHandler.CIALLO_SE;

@SuppressWarnings("all")
public class SoulExtractionSchematic implements UpgradeSchematic {
    public GlyphData glyph;
    public String key;
    public SchematicType schematicType = SchematicType.other;

    public SoulExtractionSchematic() {
        this.glyph = new GlyphData(new ResourceLocation("slashbladetetra:textures/gui/texture.png"), 64, 0);
        this.key = "soulextraction";
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
        return 0;
    }

    @Override
    public String getSlotName(ItemStack itemStack, int index) {
        return "";
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        return 0;
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, String itemSlot, int index, ItemStack materialStack) {
        return false;
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        return false;
    }

    @Override
    public boolean isRelevant(ItemStack itemStack) {
        return itemStack.getItem() instanceof SlashBladeModularItem;
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        if (slot == null || !slot.equals("slashblade/soul")) return false;
        ISlashBladeState state = targetStack.getCapability(BLADESTATE).orElse(null);
        if (state == null) return false;
        if (state.getProudSoulCount()<500) return false;
        return true;
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
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] itemStacks, boolean b, String soul, Player player) {
        //原理图应用逻辑，返回结果物品
        ItemStack newStack = itemStack.copy();
        if(b)
        newStack.getCapability(BLADESTATE).ifPresent((bladeState) -> {
            int soulCount = bladeState.getProudSoulCount();
            int count =Math.min(16, soulCount /500);
            ItemStack soulStack = new ItemStack(SBItems.proudsoul_tiny.asItem());
            soulStack.setCount(count);
            player.addItem(soulStack);
            bladeState.setProudSoulCount(soulCount -500* count);
        });
        return newStack;
    }

    @Override
    public boolean checkTools(ItemStack targetStack, ItemStack[] materials, Map<ToolAction, Integer> availableTools) {
        return this.getRequiredToolLevels(targetStack, materials).entrySet().stream().allMatch((entry) -> (Integer) availableTools.getOrDefault(entry.getKey(), 0) >= (Integer) entry.getValue());
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
