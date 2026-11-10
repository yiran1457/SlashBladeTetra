package net.yiran.sbtetra.compat;

import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.api.IImprovementNameResolver;
import se.mickelus.tetra.items.modular.IModularItem;

/**
 * Tetra 6.9.0：{@code getImprovementName(String, int)}，没有 ItemStack 参数。
 */
public class ImprovementNameResolverImpl implements IImprovementNameResolver {
    @Override
    public String getImprovementName(String improvementKey, int level, ItemStack stack) {
        return IModularItem.getImprovementName(improvementKey, level);
    }
}
