package net.yiran.sbtetra.core.mixins;

import com.google.common.collect.Lists;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.core.IOutcomeMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import se.mickelus.tetra.module.schematic.OutcomeMaterial;

import java.util.Collection;

@Mixin(OutcomeMaterial.class)
public class OutcomeMaterialMixin implements IOutcomeMaterial {
    @Shadow protected Collection<ItemStack> itemStacks;

    @Shadow private ItemPredicate predicate;

    @Shadow public int count;

    @Override
    public IOutcomeMaterial addItemStack(ItemStack itemStack) {
        this.itemStacks.add(itemStack);
        return this;
    }

    @Override
    public IOutcomeMaterial setCount(int count) {
        this.count = count;
        return this;
    }

    public IOutcomeMaterial resetItemStacks() {
        this.itemStacks = Lists.newArrayList();
        return this;
    }

    @Override
    public IOutcomeMaterial setItemPredicate(ItemPredicate itemPredicate) {
        this.predicate = itemPredicate;
        return this;
    }
}
