package net.yiran.sbtetra.compat.extraholopage;

import net.minecraft.resources.ResourceLocation;
import net.yiran.extraholopage.api.ExtraHoloRegister;
import net.yiran.sbtetra.SlashBladeTetra;

public class AddPageHandler {
    public static void add() {
        ExtraHoloRegister.register(SlashBladeTetra.MODLUAR.get())
                .setTexture(new ResourceLocation("slashbladetetra:textures/gui/texture.png"), 50, 50, 0, 16);
    }
}
