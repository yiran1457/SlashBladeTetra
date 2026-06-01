package net.yiran.sbtetra.core.mixins.slashblade;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.yiran.sbtetra.handler.SummonSwordHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityAbstractSummonedSword.class)
public class EntityAbstractSummonedSwordMixin {
    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lmods/flammpfeil/slashblade/entity/EntityAbstractSummonedSword;getDamage()D", remap = false))
    private double sbt$damage(EntityAbstractSummonedSword instance, Operation<Double> original) {
        return SummonSwordHandler.getResultDamage(instance, original.call(instance));
    }
}
