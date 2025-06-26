package net.yiran.sbtetra.core;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.schematic.OutcomeMaterial;

public interface IOutcomeMaterial {
    static IOutcomeMaterial create() {
        return ((IOutcomeMaterial) new OutcomeMaterial()).resetItemStacks();
    }

    IOutcomeMaterial addItemStack(ItemStack itemStack);
    IOutcomeMaterial setCount(int count);

    IOutcomeMaterial setItemPredicate(ItemPredicate itemPredicate);

    IOutcomeMaterial resetItemStacks();
}
