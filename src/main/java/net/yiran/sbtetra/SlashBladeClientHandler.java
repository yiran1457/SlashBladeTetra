package net.yiran.sbtetra;

import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.yiran.sbtetra.craft.SBTIngredientManager;
import net.yiran.sbtetra.gui.SBGuiStats;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.client.ToolActionIconStore;

import static mods.flammpfeil.slashblade.client.ClientHandler.bakeBlade;

public class SlashBladeClientHandler {
    public static void init(IEventBus bus) {
        bus.addListener(SlashBladeClientHandler::doClientStuff);
        bus.addListener(SlashBladeClientHandler::Baked);
        bus.addListener(SlashBladeClientHandler::buildContents);
        bus.addListener(SlashBladeClientHandler::registerItemDecoration);
    }

    public static void registerItemDecoration(RegisterItemDecorationsEvent event) {
        for (Item item : SBTIngredientManager.getItems()) {
            event.register(item, SlashBladeClientHandler::sbtDecoration);
        }
    }

    public static boolean sbtDecoration(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        var pose = guiGraphics.pose();
        pose.pushPose();


        pose.translate(xOffset + 11, yOffset + 11, 153);
        pose.scale(0.4f, 0.4f, 1);

        var glyphData = ToolActionIconStore.instance.getIcon(TetraToolActions.hammer);
        guiGraphics.blit(glyphData.textureLocation, 0, 0, glyphData.textureX, glyphData.textureY, 16, 16);
        pose.popPose();
        return false;
    }

    public static void doClientStuff(FMLClientSetupEvent event) {
        for (Item item : SBTIngredientManager.getItems()) {
            ItemProperties.register(item,
                    new ResourceLocation("slashblade:user"),
                    (stack, clientLevel, livingEntity, i) -> {
                        BladeModel.user = livingEntity;
                        return 0;
                    });
        }
        SBGuiStats.clientInit();
    }

    public static void Baked(ModelEvent.ModifyBakingResult event) {
        for (Item item : SBTIngredientManager.getItems()) {
            bakeBlade(item, event);
        }
    }

    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation("tetra", "default"))) {
            for (Item item : SBTIngredientManager.getItems()) {
                ItemStack itemStack = new ItemStack(item);
                ISlashBladeTetra.putDefaultModule(itemStack);
                event.accept(itemStack);
            }
        }
    }

}
