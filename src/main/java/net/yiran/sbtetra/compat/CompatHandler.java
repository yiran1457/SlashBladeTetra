package net.yiran.sbtetra.compat;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.yiran.sbtetra.compat.cialloblade.CialloHandler;

public class CompatHandler {
    public static void init(){
    }
    public static void setup(FMLCommonSetupEvent event) {
        CialloHandler.setup();
    }
    public static void clientSetup(FMLClientSetupEvent event) {
    }
}
