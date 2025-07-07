package net.yiran.sbtetra.compat.extraholopage;

import net.minecraftforge.fml.ModList;

public class ExtraHoloPageHandler {
    public static void clientSetup() {
        if (!ModList.get().isLoaded("extraholopage")) return;
        AddPageHandler.add();
    }
}
