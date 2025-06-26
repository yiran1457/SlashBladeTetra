package net.yiran.sbtetra.core.mixins;

import net.minecraft.resources.ResourceLocation;
import net.yiran.sbtetra.core.IModuleRegistry;
import net.yiran.sbtetra.module.SlashBladeSoulModule;
import net.yiran.sbtetra.module.SlashBladeSoulRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ModuleRegistry;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetra.module.schematic.InvalidSchematicException;

import java.util.*;

@Mixin(ModuleRegistry.class)
public class ModuleRegistryMixin implements IModuleRegistry {
    @Shadow
    private Map<ResourceLocation, ItemModule> moduleMap;
    private Map<ResourceLocation, ItemModule> sb$moduleMap = new HashMap<>();

    @Inject(method = "setupModules", at = @At(value = "RETURN"))
    private void sb$setupModules(Map<ResourceLocation, ModuleData> data, CallbackInfo ci) {
        for (Map.Entry<ResourceLocation, ItemModule> entry : sb$moduleMap.entrySet()) {
            ItemModule module = moduleMap.get(entry.getKey());
            if (module != null && entry.getValue() instanceof SlashBladeSoulModule soulModule) {
                List<String> dataData = Arrays.stream(module.getVariantData()).map(v -> v.key).toList();
                List<VariantData> javaData = Arrays.stream(soulModule.getVariantData()).filter(v -> !dataData.contains(v.key)).toList();
                List<VariantData> outData = new ArrayList<>();
                outData.addAll(javaData);
                outData.addAll(List.of(module.getVariantData()));
                soulModule.setVariantData(outData.toArray(new VariantData[0]));
            }
        }
        moduleMap.putAll(sb$moduleMap);


        try {
            SlashBladeSoulRegistry.registerSchematic(SlashBladeSoulRegistry.SCHEMATIC);
        } catch (InvalidSchematicException e) {
            throw new RuntimeException(e);
        }
    }
    public void registerModule(ResourceLocation key, ItemModule module) {
        sb$moduleMap.put(key, module);
    }
}
