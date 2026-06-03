package net.yiran.sbtetra.gui.statgetter;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;


public class StatGetterRefineFactor implements IStatGetter {
    public static StatGetterRefineFactor instance = new StatGetterRefineFactor();

    public static double refineEffect(ItemStack stack) {
        var state = stack.getCapability(ItemSlashBlade.BLADESTATE);
        if (!state.isPresent()) return 0;
        var refine = state.orElseThrow(RuntimeException::new).getRefine();
        return Config.Server.SOUL_BLADE_MAPPING_RULE.get().apply(refine);
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

    public enum Rule {
        COMMON(
                refine -> (18 * refine + 100) / (9 * refine + 1000),
                "refine -> (18 * refine + 100) / (9 * refine + 1000)"
        ),
        NONE(
                refine -> 1,
                "refine -> 1"
        ),
        LOGARITHM(
                refine -> Math.log((refine + 50) / 50),
                "refine -> Math.log((refine + 50) / 50)"
        ),
        BT(
                refine -> 0.1 + refine / 25,
                "refine -> 0.1 + refine / 25"
        );

        public final Double2DoubleFunction factor;
        public final String desc;
        public final Int2DoubleArrayMap cache = new Int2DoubleArrayMap();


        Rule(Double2DoubleFunction factor, String desc) {
            this.factor = factor;
            this.desc = desc;
        }

        public double apply(int refine) {
            if (cache.containsKey(refine)) {
                return cache.get(refine);
            } else {
                var value = factor.get(refine);
                cache.put(refine, value);
                return value;
            }
        }
    }
}
