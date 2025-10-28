package net.yiran.sbtetra.craft;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import net.yiran.sbtetra.SlashBladeTetra;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SBTIngredientManager {
    private static List<Item> ITEMS ;
    public static List<RegistryObject<Item>> REGISTER_OBJECT = new ArrayList<>();
    public static List<Function<Item, Item>> REPLACE_HANDLER = new ArrayList<>();

    public static void register(RegistryObject<Item> item) {
        REGISTER_OBJECT.add(item);
    }

    public static List<Item> getItems() {
        if (ITEMS == null) {
            ITEMS = REGISTER_OBJECT.stream().map(RegistryObject::get).collect(Collectors.toList());
        }
        return ITEMS;
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
