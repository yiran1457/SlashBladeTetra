package net.yiran.sbtetra.compat.cialloblade;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import se.mickelus.tetra.module.SchematicRegistry;

public class CialloHandler {
    public static ResourceLocation CIALLO_SE = new ResourceLocation("cialloblade","ciallo");
    public static void setup() {
        if (!ModList.get().isLoaded("cialloblade")) return;
        SchematicRegistry.instance.registerSchematic(new CialloSchematic());
        SchematicRegistry.instance.registerSchematic(new UnCialloSchematic());
    }
}
