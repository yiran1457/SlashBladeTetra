package net.yiran.sbtetra.core.mixins;

import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.gui.HoloAddonRootGui;
import net.yiran.sbtetra.item.SlashBladeModularItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.HoloPage;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloRootBaseGui;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

@Mixin(HoloGui.class)
public abstract class HoloGuiMixin {
    @Shadow @Final private HoloRootBaseGui[] pages;

    @Shadow @Final private GuiElement defaultGui;

    @Shadow private Runnable closeCallback;

    @Shadow protected abstract void changePage(HoloPage page);

    @Inject(method = "<init>",at = @At(value = "TAIL"))
    private void onInit(CallbackInfo ci){
        this.pages[3] =new HoloAddonRootGui(0, 18);
        this.pages[3].setVisible(false);
        this.defaultGui.addChild(this.pages[3]);
    }
    @Inject(method = "openSchematic",at = @At(value = "HEAD"), cancellable = true)
    private void sb$openSchematic(IModularItem item, ItemStack itemStack, String slot, UpgradeSchematic schematic, Runnable closeCallback, CallbackInfo ci){
        if(item instanceof SlashBladeModularItem){
            this.changePage(HoloPage.valueOf("ADDON"));
            ((HoloAddonRootGui)this.pages[3]).updateState(item, itemStack, slot, schematic);
            this.closeCallback = closeCallback;
            ci.cancel();
        }
    }
}
