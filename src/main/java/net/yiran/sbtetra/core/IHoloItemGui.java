package net.yiran.sbtetra.core;

import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloItemGui;

public interface IHoloItemGui {
    static IHoloItemGui cast(HoloItemGui holoItemGui){
        return (IHoloItemGui)holoItemGui;
    }
    void setIcon(GuiTexture icon);
}
