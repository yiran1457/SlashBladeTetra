package net.yiran.sbtetra.api;

import org.jetbrains.annotations.Nullable;

import java.util.ServiceLoader;

/**
 * common 与各 Tetra 版本模块之间的唯一接缝。
 * <p>
 * 版本模块通过 {@code META-INF/services} 声明 {@link IImprovementNameResolver} 的实现，
 * common 用 {@link ServiceLoader} 取用。这样 common 里不含任何与 Tetra 版本相关的假设，
 * 也不需要反射。
 */
public final class SlashBladeTetraServices {
    private static IImprovementNameResolver improvementNameResolver;

    private SlashBladeTetraServices() {
    }

    @Nullable
    public static synchronized IImprovementNameResolver improvementNameResolver() {
        if (improvementNameResolver == null) {
            for (IImprovementNameResolver resolver : ServiceLoader.load(IImprovementNameResolver.class)) {
                improvementNameResolver = resolver;
                break;
            }
        }
        return improvementNameResolver;
    }
}
