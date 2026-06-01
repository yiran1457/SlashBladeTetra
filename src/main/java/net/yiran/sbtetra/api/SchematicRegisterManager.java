package net.yiran.sbtetra.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class SchematicRegisterManager {
    public static List<SchematicProvider> providers = new ObjectArrayList<>();
    public static List<SchematicProvider> staticSchematic = new ObjectArrayList<>();

    public static void registerStaticSchematic(SchematicProvider schematic) {
        if (staticSchematic != null)
            staticSchematic.add(schematic);
        else
            throw new RuntimeException("static schematic is already register");
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            staticSchematic.stream()
                    .map(SchematicProvider::get)
                    .filter(Objects::nonNull)
                    .forEach(SchematicRegistry.instance::registerSchematic);
            staticSchematic.clear();
            staticSchematic = null;
        });
    }

    public static ResourceLocation genSchematicKey(UpgradeSchematic schematic) {
        return new ResourceLocation("sbt", schematic.getKey());
    }

    public static void registerProvider(SchematicProvider provider) {
        providers.add(provider);
    }

    public static void registerProvider(Supplier<Boolean> match, SchematicProvider schematic) {
        registerProvider(() -> match.get() ? schematic.get() : null);
    }

    public static void registerProvider(Supplier<Boolean> match, UpgradeSchematic schematic) {
        registerProvider(match, () -> schematic);
    }

    public static List<UpgradeSchematic> getSchematic() {
        return providers.stream()
                .map(SchematicProvider::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @FunctionalInterface
    public interface SchematicProvider {
        @Nullable
        UpgradeSchematic get();
    }
}
