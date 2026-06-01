package net.yiran.sbtetra.core;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState.renderOverrided;

public class MyBladeRenderState extends RenderStateShard {

    protected static final TransparencyStateShard MY_TRANSLUCENT_TRANSPARENCY = new TransparencyStateShard("my_translucent_transparency", () -> {
        RenderSystem.enableBlend();
        blend();
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    // 1. 添加缓存 Map
    private static final Map<ResourceLocation, RenderType> slashBladeBlendNoDepthCache = new HashMap<>();

    public MyBladeRenderState(String name, Runnable setupState, Runnable clearState) {
        super(name, setupState, clearState);
    }

    // 计算颜色亮度 (0~1)
    public static float getBrightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * 0.2126f + g * 0.7152f + b * 0.0722f) / 255f; // 感知亮度
    }

    public static void t(PoseStack poseStack) {
        poseStack.translate(0, 0, 1f);

    }

    public static int getAlpha() {
        return 180 << 24;
    }

    // 提亮暗色：线性提亮，保留色相
    public static int brightenColor(int rgb, float factor) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = Math.min(255, (int) (r * factor));
        g = Math.min(255, (int) (g * factor));
        b = Math.min(255, (int) (b * factor));
        return (r << 16) | (g << 8) | b;
    }

    public static void blend() {
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
    }

    // 2. 创建不写深度的 RenderType
    public static RenderType getSlashBladeBlendNoDepthWrite(ResourceLocation texture) {
        return slashBladeBlendNoDepthCache.computeIfAbsent(texture, (t) -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setTextureState(new TextureStateShard(t, false, true))
                    .setTransparencyState(MY_TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)   // 关键：只写颜色，不写深度
                    .createCompositeState(false);
            return RenderType.create("slashblade_blend_no_depth_" + t,
                    DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, false, true, state);
        });
    }


    // 3. 添加公开渲染方法
    public static void renderOverridedNoDepthWrite(ItemStack stack, WavefrontObject model,
                                                   String target, ResourceLocation texture, PoseStack matrixStackIn,
                                                   MultiBufferSource bufferIn, int packedLightIn) {
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn,
                packedLightIn, MyBladeRenderState::getSlashBladeBlendNoDepthWrite, true);
    }
}
