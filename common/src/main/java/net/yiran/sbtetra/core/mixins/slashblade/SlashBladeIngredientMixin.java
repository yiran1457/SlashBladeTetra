package net.yiran.sbtetra.core.mixins.slashblade;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.craft.SBTIngredientManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(SlashBladeIngredient.class)
public class SlashBladeIngredientMixin extends Ingredient {
    @Shadow(remap = false)
    @Final
    private RequestDefinition request;

    @Mutable
    @Shadow(remap = false)
    @Final
    private Set<Item> items;

    protected SlashBladeIngredientMixin(Stream<? extends Value> p_43907_) {
        super(p_43907_);
    }

    @WrapOperation(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean ttt(Set<Item> instance, Object o, Operation<Boolean> original) {

        if (Config.Server.CantWrapperItems.get().contains(ForgeRegistries.ITEMS.getKey((Item) o).toString())) {
            return original.call(instance,o);
        }
        return original.call(instance,o)||SBTIngredientManager.getItems().contains(o);
    }

}
