package net.yiran.sbtetra.core.mixins;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleList;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleMajor;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mixin(GuiModuleList.class)
public class GuiModuleListMixin extends GuiElement {

    @Shadow private GuiModuleMajor[] majorModuleElements;

    @Shadow @Final private BiConsumer<String, String> hoverHandler;

    @Shadow @Final private Consumer<String> slotClickHandler;

    public GuiModuleListMixin(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    @Inject(method = "updateMajorModules",at = @At(
            value = "HEAD"
    ), cancellable = true)
    private void sb$updateMajorModules(IModularItem item, ItemStack itemStack, ItemStack previewStack, CallbackInfo ci){

        String[] majorModuleNames = item.getMajorModuleNames(itemStack);
        String[] majorModuleKeys = item.getMajorModuleKeys(itemStack);
        ItemModuleMajor[] majorModules = item.getMajorModules(itemStack);
        GuiModuleOffsets offsets = item.getMajorGuiOffsets(itemStack);
        this.majorModuleElements = new GuiModuleMajor[majorModules.length];
        if (!previewStack.isEmpty()) {
            ItemModuleMajor[] majorModulesPreview = item.getMajorModules(previewStack);
            if(majorModulesPreview.length < majorModules.length){
                ArrayList<ItemModuleMajor> ModulesBuffer= new ArrayList<>();
                List<String> majorModuleKeysPreviewBuffer = List.of(item.getMajorModuleKeys(previewStack));
                for(int i = 0; i < majorModules.length; i++){
                    int index = majorModuleKeysPreviewBuffer.indexOf(majorModuleNames[i]);
                    if(index>-1){
                        ModulesBuffer.add(majorModulesPreview[index]);
                    }else {
                        ModulesBuffer.add(majorModules[i]);
                    }
                }
                majorModulesPreview = ModulesBuffer.toArray(new ItemModuleMajor[0]);
            }
            for(int i = 0; i < majorModuleNames.length; ++i) {
                int x = offsets.getX(i);
                this.majorModuleElements[i] = new GuiModuleMajor(x, offsets.getY(i), x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight, itemStack, previewStack, majorModuleKeys[i], majorModuleNames[i], majorModules[i], majorModulesPreview[i], this.slotClickHandler, this.hoverHandler);
                this.addChild(this.majorModuleElements[i]);
            }
        } else {
            for(int i = 0; i < majorModuleNames.length; ++i) {
                int x = offsets.getX(i);
                this.majorModuleElements[i] = new GuiModuleMajor(x, offsets.getY(i), x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight, itemStack, itemStack, majorModuleKeys[i], majorModuleNames[i], majorModules[i], majorModules[i], this.slotClickHandler, this.hoverHandler);
                this.addChild(this.majorModuleElements[i]);
            }
        }


        ci.cancel();
    }
}
