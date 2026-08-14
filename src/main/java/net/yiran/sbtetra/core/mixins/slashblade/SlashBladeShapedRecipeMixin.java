package net.yiran.sbtetra.core.mixins.slashblade;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.recipe.SlashBladeShapedRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.craft.SBTIngredientManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlashBladeShapedRecipe.class)
public abstract class SlashBladeShapedRecipeMixin {
    @Shadow(remap = false)
    protected abstract void updateEnchantment(ItemStack result, ItemStack ingredient);

    @Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void sbt$assemble(CraftingContainer container, RegistryAccess access, CallbackInfoReturnable<ItemStack> cir) {
        Item item = SBTIngredientManager.getReplacement(cir.getReturnValue());
        if (item != null) {
            container.getItems()
                    .stream()
                    .filter(stack -> SBTIngredientManager.getItems().contains(stack.getItem()))
                    .findFirst()
                    .map(itemStack -> {
                        var result = new ItemStack(item);
                        result.setTag(itemStack.getOrCreateTag().copy());
                        ISlashBladeState resultState = result.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);

                        var stack = cir.getReturnValue();

                        ISlashBladeState ingredientState = stack.getCapability(ItemSlashBlade.BLADESTATE).orElseThrow(NullPointerException::new);
                        resultState.deserializeNBT(ingredientState.serializeNBT());
                        result.getOrCreateTag().put("bladeState", resultState.serializeNBT());
                        this.updateEnchantment(result, stack);
                        return result;
                    })
                    .ifPresent(cir::setReturnValue);
        }
    }
}
