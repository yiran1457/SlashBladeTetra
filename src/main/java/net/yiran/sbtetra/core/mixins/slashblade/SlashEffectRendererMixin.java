package net.yiran.sbtetra.core.mixins.slashblade;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import mods.flammpfeil.slashblade.client.renderer.entity.SlashEffectRenderer;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.core.MyBladeRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SlashEffectRenderer.class, remap = false)
public class SlashEffectRendererMixin<T extends EntitySlashEffect> {

    @WrapOperation(method = "render(Lmods/flammpfeil/slashblade/entity/EntitySlashEffect;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lmods/flammpfeil/slashblade/client/renderer/util/BladeRenderState;renderOverridedLuminous(Lnet/minecraft/world/item/ItemStack;Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal = 1))
    private void r(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Operation<Void> original, @Local(argsOnly = true) T entity, @Local(name = "baseAlpha") double baseAlpha) {
        if (Config.Client.EnableRenderWrapper.get()) {
            int color = entity.getColor() & 16777215;

            // 原版幂曲线渐隐：alphaScale = 1 - (1 - baseAlpha)^4
            double alphaScale = 1.0 - Math.pow(Math.max(0.0, 1.0 - baseAlpha), 4.0);
            int alpha = (255 & (int) (255.0 * alphaScale * Config.Client.SlashEffectAlphaMultiplier.get())) << 24;

            if (MyBladeRenderState.getBrightness(color) >= Config.Client.SlashEffectMinLuminance.get().floatValue()) {
                // 亮色：加法辉光
                BladeRenderState.setCol(color | alpha);
                BladeRenderState.renderOverridedLuminous(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn);
            } else {
                // 暗色/黑色：标准混合渲染原色，保证任意 RGB 都可见
                BladeRenderState.setCol(color | alpha);
                BladeRenderState.renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn);
            }
        } else
            original.call(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn);

    }
}
