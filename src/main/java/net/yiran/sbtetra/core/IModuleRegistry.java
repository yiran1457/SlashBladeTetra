package net.yiran.sbtetra.core;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ModuleRegistry;

public interface IModuleRegistry {
    IModuleRegistry instance = (IModuleRegistry) ModuleRegistry.instance;

    void registerModule(ResourceLocation key, ItemModule module);
}
