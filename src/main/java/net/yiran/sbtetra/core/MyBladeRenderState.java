package net.yiran.sbtetra.core;

public final class MyBladeRenderState {

    private MyBladeRenderState() {
    }

    // 计算感知亮度 (0~1)
    public static float getBrightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * 0.2126f + g * 0.7152f + b * 0.0722f) / 255f;
    }
}
