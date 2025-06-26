package net.yiran.sbtetra;

import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.yiran.sbtetra.gui.SBGuiStats;
import net.yiran.sbtetra.item.SlashBladeModularItem;
import se.mickelus.tetra.items.modular.IModularItem;

import static mods.flammpfeil.slashblade.client.ClientHandler.bakeBlade;
import static net.yiran.sbtetra.SlashBladeTetra.MODLUAR;

public class SlashBladeClientHandler {
    public static void doClientStuff(FMLClientSetupEvent event) {
        ItemProperties.register(MODLUAR.get(),
                new ResourceLocation("slashblade:user"),
                (stack, clientLevel, livingEntity, i) -> {
                    BladeModel.user = livingEntity;
                    return 0;
                });
        SBGuiStats.clientInit();
    }

    public static void Baked(ModelEvent.ModifyBakingResult event) {
        bakeBlade(MODLUAR.get(), event);
    }

    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ResourceKey.create(Registries.CREATIVE_MODE_TAB,new ResourceLocation("tetra","default"))) {
            SlashBladeModularItem item = (SlashBladeModularItem) MODLUAR.get();
            ItemStack itemStack = new ItemStack(item);
            IModularItem.putModuleInSlot(itemStack,"slashblade/handle","slashblade/handle/handle","handle/netherite");
            IModularItem.putModuleInSlot(itemStack,"slashblade/blade","slashblade/blade/blade","blade/netherite");
            IModularItem.putModuleInSlot(itemStack,"slashblade/tsuba","slashblade/tsuba/tsuba","tsuba/netherite");
            IModularItem.putModuleInSlot(itemStack,"slashblade/scabbard","slashblade/scabbard/scabbard","scabbard/netherite");
            event.accept(itemStack);
        }
    }

}
