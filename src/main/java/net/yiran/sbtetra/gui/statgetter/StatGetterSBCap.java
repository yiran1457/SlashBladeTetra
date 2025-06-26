package net.yiran.sbtetra.gui.statgetter;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;

import java.util.function.Function;

import static mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE;

public class StatGetterSBCap implements IStatGetter {
    public Function<ISlashBladeState,Double> getter;
    public StatGetterSBCap(Function<ISlashBladeState,Double> getter) {
        this.getter = getter;
    }
    @Override
    public double getValue(Player player, ItemStack itemStack) {
        LazyOptional<ISlashBladeState> state = itemStack.getCapability(BLADESTATE);
        if(state.isPresent()) {
            ISlashBladeState state2 = state.orElseThrow(NullPointerException::new);
            return getter.apply(state2);
        }
        return 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String s) {
        return 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String s, String s1) {
        return 0;
    }
}
