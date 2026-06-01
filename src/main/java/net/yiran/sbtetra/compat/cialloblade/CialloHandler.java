package net.yiran.sbtetra.compat.cialloblade;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.compat.cialloblade.schematic.CialloSchematic;
import net.yiran.sbtetra.compat.cialloblade.schematic.UnCialloSchematic;
import se.mickelus.tetra.module.SchematicRegistry;

public class CialloHandler {
    public static ResourceLocation CIALLO_SE = new ResourceLocation("cialloblade","ciallo");
    public static void setup() {
        if (!ModList.get().isLoaded("cialloblade")) return;
        if(Config.Server.EnableCiallo.get()) {
            SchematicRegistry.instance.registerSchematic(new CialloSchematic());
            SchematicRegistry.instance.registerSchematic(new UnCialloSchematic());
        }
    }
}
