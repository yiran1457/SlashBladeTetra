package net.yiran.sbtetra.core.mixins.minecraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow
    private int count;

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void sbt$init(CompoundTag compoundTag, CallbackInfo ci) {
        this.count = compoundTag.getInt("Count");
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void sbt$save(CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        compoundTag.putInt("Count", this.count);
    }
}
