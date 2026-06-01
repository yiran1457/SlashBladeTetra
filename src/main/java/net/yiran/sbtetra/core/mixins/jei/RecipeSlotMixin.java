package net.yiran.sbtetra.core.mixins.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import net.yiran.sbtetra.compat.jei.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Pseudo
@Mixin(value = RecipeSlot.class,remap = false)
public class RecipeSlotMixin {
    @WrapOperation(method = "getAllIngredients", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;flatMap(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private Stream<ITypedIngredient<?>> injectSBTIngredient(Stream<Optional<ITypedIngredient<?>>> instance, Function<Optional<ITypedIngredient<?>>, Stream<ITypedIngredient<?>>> function, Operation<Stream<ITypedIngredient<?>>> original) {
        return original.call(instance, function).flatMap(Util::injectITypedIngredient);
    }
}
