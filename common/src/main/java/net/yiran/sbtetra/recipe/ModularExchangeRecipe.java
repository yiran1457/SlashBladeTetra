package net.yiran.sbtetra.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.yiran.sbtetra.SlashBladeTetra;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import net.yiran.sbtetra.item.ModularExchangeItem;

public class ModularExchangeRecipe extends CustomRecipe {
    public ModularExchangeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private Pair findPair(CraftingContainer container) {
        ItemStack blade = ItemStack.EMPTY;
        ItemStack exchange = ItemStack.EMPTY;
        int exchangeSlot = -1;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof ISlashBladeTetra) {
                if (!blade.isEmpty()) {
                    return null;
                }
                blade = stack;
            } else if (stack.getItem() instanceof ModularExchangeItem) {
                if (!exchange.isEmpty()) {
                    return null;
                }
                exchange = stack;
                exchangeSlot = i;
            } else {
                return null;
            }
        }

        if (blade.isEmpty() || exchange.isEmpty()) {
            return null;
        }
        return new Pair(blade, exchange, exchangeSlot);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return findPair(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        Pair pair = findPair(container);
        if (pair == null) {
            return ItemStack.EMPTY;
        }

        ItemStack resultBlade = pair.blade.copy();
        ItemStack resultExchange = pair.exchange.copy();
        resultExchange.setCount(1);
        ModularExchangeItem.swapWithBlade(resultExchange, resultBlade);
        return resultBlade;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remainders = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        Pair pair = findPair(container);
        if (pair == null) {
            return remainders;
        }

        ItemStack resultBlade = pair.blade.copy();
        ItemStack resultExchange = pair.exchange.copy();
        resultExchange.setCount(1);
        ModularExchangeItem.swapWithBlade(resultExchange, resultBlade);
        remainders.set(pair.exchangeSlot, resultExchange);
        return remainders;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(SlashBladeTetra.MODLUAR.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SBTRecipeSerializers.MODULAR_EXCHANGE.get();
    }

    private static class Pair {
        final ItemStack blade;
        final ItemStack exchange;
        final int exchangeSlot;

        Pair(ItemStack blade, ItemStack exchange, int exchangeSlot) {
            this.blade = blade;
            this.exchange = exchange;
            this.exchangeSlot = exchangeSlot;
        }
    }
}
