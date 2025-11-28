package net.yiran.sbtetra.core.mixins;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.recipe.SlashBladeSmithingRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.yiran.sbtetra.craft.SBTIngredientManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("all")
@Mixin(SlashBladeSmithingRecipe.class)
public abstract class SlashBladeSmithingRecipeMixin implements SmithingRecipe {

    @Shadow(remap = false)
    protected abstract void updateEnchantment(ItemStack result, ItemStack ingredient);

    @Inject(
            method = "m_5874_(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            //method = "assemble",
            remap = false,
            at = @At("RETURN"),
            cancellable = true
    )
    private void sbt$assemble(@Nullable Container container,@Nullable RegistryAccess access, CallbackInfoReturnable<ItemStack> cir) {
        Item item = SBTIngredientManager.getReplacement(cir.getReturnValue());
        if (item != null) {
            ItemStack itemStack = container.getItem(1);
            if (SBTIngredientManager.getItems().contains(itemStack.getItem())) {

                var result = new ItemStack(item);
                result.setTag(itemStack.getOrCreateTag());
                ISlashBladeState resultState = result.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);

                var stack = cir.getReturnValue();

                ISlashBladeState ingredientState = stack.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
                resultState.deserializeNBT(ingredientState.serializeNBT());
                result.getOrCreateTag().put("bladeState", resultState.serializeNBT());
                this.updateEnchantment(result, stack);
                cir.setReturnValue(result);
            }
        }
    }
}
