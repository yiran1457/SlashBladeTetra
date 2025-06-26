package net.yiran.sbtetra.core.mixins;

import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetra.items.modular.impl.holo.HoloPage;



@Mixin(HoloPage.class)
public class HoloPageMixin implements IExtensibleEnum {
    private static HoloPage create(String name, String label){
        throw new IllegalStateException("Enum not extended");
    }
    static {
        create("ADDON", "ADDON");
    }
}
