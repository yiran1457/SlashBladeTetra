package net.yiran.sbtetra.api;

import net.minecraft.world.item.ItemStack;

/**
 * Tetra 各版本的 improvement 名称查询方式不同，把差异抽到这里。
 * <p>
 * 6.9.0 是 {@code getImprovementName(String, int)}；6.10.0 起多一个 {@code ItemStack} 参数。
 * common 只依赖这个接口，具体实现由 {@code tetra-6.9.0} / {@code tetra-6.17.0} 模块提供，
 * 通过 {@link SlashBladeTetraServices} 注册。
 */
public interface IImprovementNameResolver {
    /**
     * @return Tetra 提供的 improvement 显示名，查不到时返回 {@code null}。
     */
    String getImprovementName(String improvementKey, int level, ItemStack stack);
}
