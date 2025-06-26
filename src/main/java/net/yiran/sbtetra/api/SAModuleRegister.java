package net.yiran.sbtetra.api;

import com.google.common.collect.ArrayListMultimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.yiran.sbtetra.itemeffect.SBItemEffects;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.VariantData;

import java.util.HashMap;
import java.util.UUID;

public class SAModuleRegister {
    public static HashMap<ResourceLocation, ModuleData> dataHashMap = new HashMap<>();
    public static ModuleData defaultModuleData = new ModuleData();

    static {
        defaultModuleData.attributes.put(
                ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(UUID.nameUUIDFromBytes(new byte[]{127, 1, 64, 11, 45, 14}), "Reach amplifer", 3, AttributeModifier.Operation.ADDITION)
        );
        defaultModuleData.addRefineEffectLevel(16);
/*
        dataHashMap.put(
                new ResourceLocation("slashblade:judgement_cut"),
                new ModuleData()
                        .addRefineEffectLevel(100)
                        .addAttributeModifier(
                                ForgeMod.ENTITY_REACH.get(),
                                new AttributeModifier(UUID.nameUUIDFromBytes(new byte[]{127, 1, 64, 11, 45, 14}), "Reach amplifer", 3, AttributeModifier.Operation.MULTIPLY_TOTAL)
                        )
        );*/
    }

    public static class ModuleData {
        public ArrayListMultimap<Attribute, AttributeModifier> attributes = ArrayListMultimap.create();
        public EffectData effects = new EffectData();

        public ModuleData() {
        }

        public ModuleData addRefineEffectLevel(float count) {
            effects.levelMap.put(SBItemEffects.REFINE, count);
            return this;
        }

        public ModuleData addAttributeModifier(Attribute attribute, AttributeModifier modifier) {
            this.attributes.put(attribute, modifier);
            return this;
        }

        public void apply(VariantData variant) {
            variant.attributes = attributes;
            variant.effects = effects;
        }
    }
}
