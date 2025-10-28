package net.yiran.sbtetra.craft;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.SlashBladeTetra;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SBTIngredientManager {
    public static List<Item> ITEMS = new ArrayList<>();
    public static List<Function<Item, Item>> REPLACE_HANDLER = new ArrayList<>();

    public static void register(Item item) {
        ITEMS.add(item);
    }

    public static void registerReplaceHandler(Function<Item, Item> replacement) {
        REPLACE_HANDLER.add(replacement);
    }

    @Nullable
    public static Item getReplacement(ItemStack item) {
        Item target = item.getItem();
        for (Function<Item, Item> handler : REPLACE_HANDLER) {
            Item blade = handler.apply(target);
            if (blade != null)
                return blade;
        }
        if (item.is(SlashBladeTetra.REPLACEMENT))
            return SlashBladeTetra.MODLUAR.get();
        return null;
    }
}
