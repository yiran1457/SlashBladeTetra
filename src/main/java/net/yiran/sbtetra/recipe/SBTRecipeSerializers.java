package net.yiran.sbtetra.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yiran.sbtetra.SlashBladeTetra;

public class SBTRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SlashBladeTetra.MODID);

    public static final RegistryObject<RecipeSerializer<ModularExchangeRecipe>> MODULAR_EXCHANGE =
            RECIPE_SERIALIZERS.register("modular_exchange",
                    () -> new SimpleCraftingRecipeSerializer<>(ModularExchangeRecipe::new));

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
}
