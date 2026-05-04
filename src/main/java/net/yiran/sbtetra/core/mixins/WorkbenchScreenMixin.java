package net.yiran.sbtetra.core.mixins;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;

import java.util.stream.Collectors;

@Mixin(WorkbenchScreen.class)
public class WorkbenchScreenMixin {
    @Redirect(method = "onTileEntityChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 0))
    private boolean onTileEntityChange(ItemStack stack, ItemStack other) {
        if (!(stack.getItem() instanceof IModularItem item)) return false;
        if (!(other.getItem() instanceof IModularItem)) return false;
        if (stack == other) return true;
        if (stack.getItem() != other.getItem()) return false;

        return item.getAllModules(stack).stream().map(ItemModule::getVariantData).collect(Collectors.toSet())
                .containsAll(item.getAllModules(stack).stream().map(ItemModule::getVariantData).toList());
        //return false;
    }
}
