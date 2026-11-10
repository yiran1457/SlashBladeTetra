package net.yiran.sbtetra.gui.statgetter;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.api.Expressions;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;


public class StatGetterRefineFactor implements IStatGetter {
    public static StatGetterRefineFactor instance = new StatGetterRefineFactor();

    public static double refineEffect(ItemStack stack) {
        var state = stack.getCapability(ItemSlashBlade.BLADESTATE);
        if (!state.isPresent()) return 0;
        var refine = state.orElseThrow(RuntimeException::new).getRefine();
        return Expressions.soulBladeMapping.getExpression().evaluate(refine);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return refineEffect(itemStack);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String s) {
        return 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String s, String s1) {
        return 0;
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        if (currentStack.getItem() instanceof ISlashBladeTetra || previewStack.getItem() instanceof ISlashBladeTetra)
            return IStatGetter.super.shouldShow(player, currentStack, previewStack);
        return false;
    }

}
