package net.yiran.sbtetra.compat;

import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.api.IImprovementNameResolver;
import se.mickelus.tetra.items.modular.IModularItem;

/**
 * Tetra 6.10.0+：{@code getImprovementName(String, int, ItemStack)}。
 * 新版会先查客户端的 ImprovementOverride，因此必须把物品一并传入。
 */
public class ImprovementNameResolverImpl implements IImprovementNameResolver {
    @Override
    public String getImprovementName(String improvementKey, int level, ItemStack stack) {
        return IModularItem.getImprovementName(improvementKey, level, stack);
    }
}
