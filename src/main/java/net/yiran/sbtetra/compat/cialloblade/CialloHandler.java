package net.yiran.sbtetra.compat.cialloblade;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.api.SchematicRegisterManager;
import net.yiran.sbtetra.compat.cialloblade.schematic.CialloSchematic;
import net.yiran.sbtetra.compat.cialloblade.schematic.UnCialloSchematic;

public class CialloHandler {
    public static ResourceLocation CIALLO_SE = new ResourceLocation("cialloblade","ciallo");
    public static void setup() {
        if (!ModList.get().isLoaded("cialloblade")) return;
        SchematicRegisterManager.registerProvider(Config.Server.EnableCiallo,CialloSchematic::new);
        SchematicRegisterManager.registerProvider(Config.Server.EnableCiallo,UnCialloSchematic::new);
    }
}
