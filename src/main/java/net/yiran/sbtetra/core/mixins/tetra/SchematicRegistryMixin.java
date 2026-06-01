package net.yiran.sbtetra.core.mixins.tetra;

import net.minecraft.resources.ResourceLocation;
import net.yiran.sbtetra.api.SchematicRegisterManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.SchematicDefinition;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;

@Mixin(value = SchematicRegistry.class,remap = false)
public class SchematicRegistryMixin {
    @Shadow
    private Map<ResourceLocation, UpgradeSchematic> schematicMap;

    @Inject(method = "setupSchematics", at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/module/RepairRegistry;injectFromSchematics(Ljava/util/Collection;)V"))
    private void sbt$setup(Map<ResourceLocation, SchematicDefinition> data, CallbackInfo ci) {
        for (UpgradeSchematic schematic : SchematicRegisterManager.getSchematic()) {
            this.schematicMap.put(SchematicRegisterManager.genSchematicKey(schematic), schematic);
        }
    }
}
