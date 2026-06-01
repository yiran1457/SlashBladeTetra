package net.yiran.sbtetra.core.mixins.test;

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
            int alpha = (255 & (int) ((double) 255.0F * baseAlpha)) << 24;

            int brightened = MyBladeRenderState.brightenColor(color, 1.8f);

            BladeRenderState.setCol(brightened | alpha);
            MyBladeRenderState.renderOverridedNoDepthWrite(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn);

        } else
            original.call(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn);

    }
}
