package net.yiran.sbtetra.core.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatGetterDurability;

@Mixin(value = StatGetterDurability.class,remap = false)
public abstract class StatGetterDurabilityMixin implements IStatGetter {
    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        if(currentStack.getItem() instanceof ISlashBladeTetra||previewStack.getItem() instanceof ISlashBladeTetra) {
            return false;
        }
        return IStatGetter.super.shouldShow(player, currentStack, previewStack);
    }
}
