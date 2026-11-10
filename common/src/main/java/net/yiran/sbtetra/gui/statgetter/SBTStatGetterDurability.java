package net.yiran.sbtetra.gui.statgetter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.Optional;

public class SBTStatGetterDurability implements IStatGetter {
    public static SBTStatGetterDurability instance = new SBTStatGetterDurability();

    public double getSBTDurability(ItemStack stack) {
        return CastOptional.cast(stack.getItem(), ISlashBladeTetra.class)
                .map(item ->Optional.of(item.getPropertiesCached(stack)).map((properties) -> properties.durability * properties.durabilityMultiplier).map(Math::round).orElse(0))
                .orElseGet(stack::getMaxDamage);
    }

    public double getValue(Player player, ItemStack itemStack) {
        return getSBTDurability(itemStack);
    }

    public double getValue(Player player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map((item) -> item.getModuleFromSlot(itemStack, slot))
                .map((module) -> module.getProperties(itemStack))
                .map((data) -> data.durability + (data.durabilityMultiplier != 0.0F ? (int) ((data.durabilityMultiplier - 1.0F) * getSBTDurability(itemStack)) : 0))
                .orElse(0);
    }

    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap((item) -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map((module) -> module.getImprovement(itemStack, improvement)).map((data) -> data.durability + (data.durabilityMultiplier != 0.0F ? (int) ((data.durabilityMultiplier - 1.0F) * getSBTDurability(itemStack)) : 0))
                .orElse(0);
    }

    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        if (currentStack.getItem() instanceof ISlashBladeTetra || previewStack.getItem() instanceof ISlashBladeTetra) {
            return true;
        }
        return false;
    }
}
