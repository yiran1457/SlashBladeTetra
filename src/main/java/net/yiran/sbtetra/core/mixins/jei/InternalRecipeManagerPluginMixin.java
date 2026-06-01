package net.yiran.sbtetra.core.mixins.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import mezz.jei.library.recipes.InternalRecipeManagerPlugin;
import net.yiran.sbtetra.compat.jei.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = InternalRecipeManagerPlugin.class, remap = false)
public class InternalRecipeManagerPluginMixin {
    @WrapOperation(method = "getRecipeTypes", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/IFocus;getTypedValue()Lmezz/jei/api/ingredients/ITypedIngredient;"))
    private <V> ITypedIngredient<V> getItem(IFocus<V> instance, Operation<ITypedIngredient<V>> original) {
        var type = original.call(instance);
        if (type instanceof TypedItemStack stack) {
            return Util.wrapperITypedIngredient(stack);
        }
        return type;
    }

    @WrapOperation(method = "getRecipes(Lmezz/jei/api/recipe/category/IRecipeCategory;Lmezz/jei/api/recipe/IFocus;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/IFocus;getTypedValue()Lmezz/jei/api/ingredients/ITypedIngredient;"))
    private <V> ITypedIngredient<V> getItems(IFocus<V> instance, Operation<ITypedIngredient<V>> original) {
        var type = original.call(instance);
        if (type instanceof TypedItemStack stack) {
            return Util.wrapperITypedIngredient(stack);
        }
        return type;
    }
}
