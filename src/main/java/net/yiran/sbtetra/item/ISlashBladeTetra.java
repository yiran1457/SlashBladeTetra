package net.yiran.sbtetra.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.common.util.LazyOptional;
import net.yiran.sbtetra.item.api.ModuleSlotManager;
import net.yiran.sbtetra.itemeffect.SBItemEffects;
import org.jetbrains.annotations.NotNull;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.effect.SculkTaintEffect;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.properties.AttributeHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public interface ISlashBladeTetra extends IModularItem, IForgeItem {

    @Override
    default String[] getMajorModuleKeys(ItemStack itemStack) {
        return ModuleSlotManager.getMajorModuleKeys(itemStack);
    }

    @Override
    default String[] getMinorModuleKeys(ItemStack itemStack) {
        return ModuleSlotManager.getMinorModuleKeys(itemStack);
    }

    @Override
    default String[] getRequiredModules(ItemStack itemStack) {
        return ModuleSlotManager.getMajorModuleKeys(itemStack);
    }

    @Override
    default void tickProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        if (ConfigHandler.moduleProgression.get()) {
            this.tickHoningProgression(entity, itemStack, multiplier);

            for (ItemModuleMajor module : this.getMajorModules(itemStack)) {
                if (module == null) continue;
                module.tickProgression(entity, itemStack, multiplier);
            }

        }
    }

    @Override
    default GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        return ModuleSlotManager.getMajorGuiOffsets(itemStack);
    }

    @Override
    default GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        return new GuiModuleOffsets();
    }

    @Override
    default int getHoneBase(ItemStack itemStack) {
        return 512;
    }

    @Override
    default int getHoneIntegrityMultiplier(ItemStack itemStack) {
        return 256;
    }

    @Override
    default boolean canGainHoneProgress(ItemStack itemStack) {
        return true;
    }

    @Override
    default boolean isBookEnchantable(ItemStack itemStack, ItemStack bookStack) {
        return false;
    }

    @Override
    default boolean canApplyAtEnchantingTable(ItemStack itemStack, Enchantment enchantment) {
        return this.acceptsEnchantment(itemStack, enchantment, true);
    }

    @Override
    default int getEnchantmentValue(ItemStack itemStack) {
        return this.getEnchantability(itemStack);
    }

    @Override
    default boolean isBroken(ItemStack itemStack) {
        return itemStack.getOrCreateTagElement("bladeState").getBoolean("isBroken");
    }

    @Override
    default Multimap<Attribute, AttributeModifier> getModuleAttributes(ItemStack itemStack) {
        return this.getAllModules(itemStack)
                .stream()
                .map((module) -> {
                    if (Objects.equals(module.getVariantData(itemStack).key, "blade/unnamed")) {
                        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
                        result.putAll(module.getAttributeModifiers(itemStack));
                        LazyOptional<ISlashBladeState> state = itemStack.getCapability(ItemSlashBlade.BLADESTATE);
                        state.ifPresent((s) -> {
                            result.put(Attributes.ATTACK_DAMAGE,
                                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", s.getBaseAttackModifier(), AttributeModifier.Operation.ADDITION)
                            );
                        });
                        return result;
                    }
                    return module.getAttributeModifiers(itemStack);
                })
                .filter(Objects::nonNull)
                .reduce(null, AttributeHelper::merge);
    }

    @Override
    default void assemble(ItemStack itemStack, @Nullable Level world, float severity) {
        itemStack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
            state.setMaxDamage(sbt$getMaxDamage(itemStack));
        });
        IModularItem.super.assemble(itemStack, world, severity);
    }

    default void sbt$inventoryTick(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn.tickCount % 40 == 0) {
            ModuleSlotManager.tryAddSoul(stack, true);
        }
    }

    @Override
    default EffectData getEffectData(ItemStack itemStack) {
        if (logger.isDebugEnabled()) {
            logger.debug("Gathering effect data for {} ({})", this.getItemName(itemStack), this.getDataCacheKey(itemStack));
        }

        return Stream.of(
                        this.getAllModules(itemStack).stream().map((module) -> module.getEffectData(itemStack)),
                        Arrays.stream(this.getSynergyData(itemStack)).map((synergy) -> synergy.effects),
                        Arrays.stream(getSBEffectData(itemStack))
                )
                .flatMap(Function.identity())
                .filter(Objects::nonNull)
                .reduce(null, EffectData::merge);
    }

    default EffectData[] getSBEffectData(ItemStack itemStack) {
        EffectData[] SBEffectData = new EffectData[]{new EffectData()};
        itemStack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
            SBEffectData[0].efficiencyMap.put(SBItemEffects.REFINE, state.getBaseAttackModifier());
            SBEffectData[0].levelMap.put(SBItemEffects.REFINE, state.getBaseAttackModifier());
        });
        return SBEffectData;
    }

    default void sbt$hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        if (!this.isBroken(itemStack)) {

            ItemEffectHandler.applyHitEffects(itemStack, target, attacker);
            this.applyPositiveUsageEffects(attacker, itemStack, 1.0F);


            int skulkTaintLevel = this.getEffectLevel(itemStack, ItemEffect.sculkTaint);
            if (skulkTaintLevel > 0) {
                SculkTaintEffect.perform((ServerLevel) target.level(), target.blockPosition(), skulkTaintLevel, this.getEffectEfficiency(itemStack, ItemEffect.sculkTaint));
            }

            this.applyNegativeUsageEffects(attacker, itemStack, (double) 1.0F);
        }
    }

    default int sbt$getMaxDamage(ItemStack stack) {
        return Math.max(1, Optional.of(this.getPropertiesCached(stack)).map((properties) -> properties.durability * properties.durabilityMultiplier).map(Math::round).orElse(0));
    }

    default Multimap<Attribute, AttributeModifier> sbt$getAttributeModifiers(EquipmentSlot slot, ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
        if (this.isBroken(itemStack)) {
        } else if (slot == EquipmentSlot.MAINHAND) {
            Multimap<Attribute, AttributeModifier> Tetra = this.getAttributeModifiersCached(itemStack);
            result.putAll(Tetra);
            LazyOptional<ISlashBladeState> state = itemStack.getCapability(ItemSlashBlade.BLADESTATE);
            state.ifPresent((s) -> {
                EnumSet<SwordType> swordType = SwordType.from(itemStack);
                float baseAttackModifier = getEffectLevel(itemStack, SBItemEffects.REFINE);
                int refine = s.getRefine();
                float refineFactor = swordType.contains(SwordType.FIERCEREDGE) ? 0.1F : 0.05F;
                float attackAmplifier = (1.0F - 1.0F / (1.0F + refineFactor * (float) refine)) * baseAttackModifier;
                SlashBladeEvent.UpdateAttackEvent event = new SlashBladeEvent.UpdateAttackEvent(itemStack, s, attackAmplifier);
                MinecraftForge.EVENT_BUS.post(event);
                AttributeModifier attack = new AttributeModifier(UUID.nameUUIDFromBytes(new byte[]{127, 0, 0, 11, 45, 14}), "Weapon modifier", event.getNewDamage(), AttributeModifier.Operation.ADDITION);
                result.put(Attributes.ATTACK_DAMAGE, attack);
            });
        }
        return result;
    }

    static void putDefaultModule(ItemStack itemStack) {
        itemStack.getOrCreateTag().putString("id", "DefaultInstance");
        IModularItem.putModuleInSlot(itemStack, "slashblade/handle", "slashblade/handle/handle", "handle/unnamed");
        IModularItem.putModuleInSlot(itemStack, "slashblade/blade", "slashblade/blade/blade", "blade/unnamed");
        IModularItem.putModuleInSlot(itemStack, "slashblade/tsuba", "slashblade/tsuba/tsuba", "tsuba/unnamed");
        IModularItem.putModuleInSlot(itemStack, "slashblade/scabbard", "slashblade/scabbard/scabbard", "scabbard/unnamed");
    }

}
