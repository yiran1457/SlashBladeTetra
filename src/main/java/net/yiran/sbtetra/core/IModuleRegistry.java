package net.yiran.sbtetra.core;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.module.ItemModule;

public interface IModuleRegistry {
    void registerModule(ResourceLocation key, ItemModule module);
}
