package net.yiran.sbtetra.handler;

import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.minecraft.world.entity.LivingEntity;
import net.yiran.sbtetra.gui.statgetter.StatGetterRefineFactor;
import net.yiran.sbtetra.item.ISlashBladeTetra;

import static net.yiran.sbtetra.itemeffect.SBItemEffects.MAPPING;
import static net.yiran.sbtetra.itemeffect.SBItemEffects.REFINE;

public class SummonSwordHandler {
    /*等哪天能用了
    @SubscribeEvent
    public static void onSummonSwordHit(SlashBladeEvent.SummonedSwordOnHitEntityEvent event) {
        var summoned = event.getSummonedSword();
        summoned.setDamage(getResultDamage(summoned,summoned.getDamage()));
    }*/

    public static double getResultDamage(EntityAbstractSummonedSword summonedSword, double amount) {
        if (!(summonedSword.getOwner() instanceof LivingEntity livingEntity)) return amount;
        var stack = livingEntity.getMainHandItem();
        if (!(stack.getItem() instanceof ISlashBladeTetra modular)) return amount;
        double arg = 0;
        var lvl = modular.getEffectLevel(stack, MAPPING);
        if (lvl > 0) {
            arg += lvl;
        }
        var eff = modular.getEffectEfficiency(stack, MAPPING);
        if (eff > 0) {
            arg += summonedSword.getDamage() + modular.getEffectLevel(stack, REFINE) * eff;
        }
        if (arg > 0) {
            arg *= StatGetterRefineFactor.instance.getValue(null, stack);
        }
        return amount + arg;
    }

}
