package net.yiran.sbtetra.module;

import mods.flammpfeil.slashblade.registry.SlashArtsRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.api.SAModuleRegister;
import net.yiran.sbtetra.core.IModuleRegistry;
import net.yiran.sbtetra.core.IOutcomeMaterial;
import se.mickelus.tetra.module.ModuleRegistry;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetra.module.schematic.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SlashBladeSoulRegistry {
    public static ModuleData MODULE_DATA;
    public static List<VariantData> VARIANT_DATA;
    public static SchematicDefinition SCHEMATIC;
    public static List<OutcomeDefinition> OUTCOME_DEFINITION;

    static {
        MODULE_DATA = new ModuleData();
        MODULE_DATA.slots = new String[]{"slashblade/soul"};
        MODULE_DATA.slotSuffixes = new String[]{};
        VARIANT_DATA = new ArrayList<>();
        SCHEMATIC = new SchematicDefinition();
        SCHEMATIC.glyph.textureLocation = new ResourceLocation("slashbladetetra:textures/gui/texture.png");
        SCHEMATIC.glyph.textureX = 64;
        SCHEMATIC.slots = new String[]{"slashblade/soul"};
        SCHEMATIC.materialSlotCount = 1;
        SCHEMATIC.key = "slashblade/soul/sa";
        SCHEMATIC.displayType = SchematicType.major;
        SCHEMATIC.applicableMaterials = new String[]{"slashblade:proudsoul_sphere"};
        SCHEMATIC.sources = new String[]{"SlashBladeTetra"};
        OUTCOME_DEFINITION = new ArrayList<>();
    }

    public static void addVariantData(ResourceLocation SAName) {
        VariantData test = new VariantData();
        test.key = "sa/" + SAName;
        SAModuleRegister.dataHashMap.getOrDefault(SAName, SAModuleRegister.defaultModuleData).apply(test);
        test.glyph.textureLocation = new ResourceLocation("slashbladetetra:textures/gui/texture.png");
        test.glyph.textureX = 64;
        VARIANT_DATA.add(test);
    }

    public static void addOutcomeDefinition(String SAName) {
        UniqueOutcomeDefinition test = new UniqueOutcomeDefinition();
        ItemStack stack =SlashBladeItems.PROUDSOUL_SPHERE.get().getDefaultInstance();

        CompoundTag tag = new CompoundTag();
        tag.putString("SpecialAttackType", SAName);
        stack.setTag(tag);
        test.material = (OutcomeMaterial) IOutcomeMaterial.create()
                .addItemStack(stack)
                .setCount(4)
                .setItemPredicate(new ItemPredicate(
                        null,
                        Set.of(SlashBladeItems.PROUDSOUL_SPHERE.get()),
                        MinMaxBounds.Ints.ANY,
                        MinMaxBounds.Ints.ANY,
                        EnchantmentPredicate.NONE,
                        EnchantmentPredicate.NONE,
                        null,
                        new NbtPredicate(tag)
                ));
        test.moduleKey = "slashblade/soul/sa";
        test.moduleVariant = "sa/" + SAName;
        OUTCOME_DEFINITION.add(test);
    }

    public static void registerModule(ResourceLocation identifier, ModuleData data) {
        ((IModuleRegistry) ModuleRegistry.instance).registerModule(
                identifier,
                new SlashBladeSoulModule(identifier, data)
        );
    }

    public static void registerSchematic(SchematicDefinition schematic) throws InvalidSchematicException {
        ConfigSchematic schematic1 = new ConfigSchematic(schematic);
        SchematicRegistry.instance.registerSchematic(schematic1);
    }

    public static void init() {

        SlashArtsRegistry.REGISTRY.get().forEach((slashArts) -> {
            ResourceLocation key = SlashArtsRegistry.REGISTRY.get().getKey(slashArts);
            if (!slashArts.equals(SlashArtsRegistry.NONE.get()) && key != null) {
                addVariantData(key);
                addOutcomeDefinition(key.toString());
            }
        });

        MODULE_DATA.variants = VARIANT_DATA.toArray(new VariantData[0]);
        registerModule(
                new ResourceLocation("tetra:slashblade/soul/sa"),
                MODULE_DATA
        );

        SCHEMATIC.outcomes = OUTCOME_DEFINITION.toArray(new OutcomeDefinition[0]);

    }

}
