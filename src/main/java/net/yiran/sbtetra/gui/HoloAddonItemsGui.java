package net.yiran.sbtetra.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.core.IHoloItemGui;
import net.yiran.sbtetra.item.SlashBladeModularItem;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloItemGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloMaterialsButtonGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloSeparatorsGui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.yiran.sbtetra.SlashBladeTetra.MODLUAR;

public class HoloAddonItemsGui extends GuiElement {

    private final HoloSeparatorsGui separators;
    private final HoloMaterialsButtonGui materialsButton;
    private final KeyframeAnimation openAnimation;
    private final KeyframeAnimation backAnimation;

    public HoloAddonItemsGui(int x, int y, int width, int height, BiConsumer<IModularItem, ItemStack> onItemSelect, Consumer<String> onSlotSelect, Runnable onMaterialsClick) {
        super(x, y, width, height);
        this.separators = new HoloSeparatorsGui(1, -71, width, height);
        this.addChild(this.separators);

        SlashBladeModularItem item = (SlashBladeModularItem) MODLUAR.get();
        HoloItemGui holoItemGui = (HoloItemGui) new HoloItemGui(1, -40, item, 0, () -> onItemSelect.accept(item, item.getDefaultStack()), onSlotSelect).setAttachment(GuiAttachment.topCenter);

        IHoloItemGui iHoloItemGui = IHoloItemGui.cast(holoItemGui);

        iHoloItemGui.setIcon(
                new GuiTexture(0, 0, 50, 50, 0, 16,new ResourceLocation("slashbladetetra:textures/gui/texture.png"))
        );

        this.addChild(holoItemGui);


        this.materialsButton = new HoloMaterialsButtonGui(0, 60, onMaterialsClick);
        this.materialsButton.setAttachment(GuiAttachment.topCenter);
        this.addChild(this.materialsButton);
        this.openAnimation = new KeyframeAnimation(200, this).applyTo(new Applier.TranslateY((float)(y - 4), (float)y), new Applier.Opacity(0.0F, 1.0F)).withDelay(800);
        this.backAnimation = new KeyframeAnimation(100, this).applyTo(new Applier.TranslateY((float)(y - 4), (float)y), new Applier.Opacity(0.0F, 1.0F));
    }

    public void animateOpen() {
        this.openAnimation.start();
    }

    public void animateOpenAll() {
        this.openAnimation.start();
        this.separators.animateOpen();
    }

    public void animateBack() {
        this.backAnimation.start();
    }

    public void changeItem(IModularItem item) {
        this.getChildren(HoloItemGui.class).forEach((child) -> child.onItemSelected(item));
        this.materialsButton.setVisible(item == null);
        if (item == null) {
            this.separators.animateReopen();
        } else {
            this.separators.setVisible(false);
        }

    }
}
