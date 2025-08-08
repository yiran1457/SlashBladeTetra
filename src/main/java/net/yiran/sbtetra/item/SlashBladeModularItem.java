package net.yiran.sbtetra.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.yiran.sbtetra.itemeffect.SBItemEffects;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.effect.SculkTaintEffect;
import se.mickelus.tetra.event.ModularItemDamageEvent;
import se.mickelus.tetra.module.data.EffectData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SlashBladeModularItem extends AbstractSlashBladeModularItem {
    public SlashBladeModularItem() {
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.addAll(this.getTooltip(stack, world, flag));
    }

    @Override
    public int getDamage(ItemStack stack) {
        //return stack.getOrCreateTagElement("bladeState").getInt("Damage");
        return stack.getCapability(BLADESTATE).map((s) -> s.getDamage()).orElse(0);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Math.max(1,  Optional.of(this.getPropertiesCached(stack)).map((properties) -> properties.durability * properties.durabilityMultiplier).map(Math::round).orElse(0));
        //return stack.getCapability(BLADESTATE).map(ISlashBladeState::getMaxDamage).orElse(super.getMaxDamage(stack));
    }

    @Override
    public void assemble(ItemStack itemStack, @Nullable Level world, float severity) {
        itemStack.getCapability(BLADESTATE).ifPresent(state -> {
            state.setMaxDamage(getMaxDamage(itemStack));
            /*
            state.setMaxDamage(Math.max(1, Optional.of(this.getPropertiesCached(itemStack)).map((properties) -> properties.durability * properties.durabilityMultiplier).map(Math::round).orElse(0)));*/
        });
        super.assemble(itemStack, world, severity);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        ModularItemDamageEvent event = new ModularItemDamageEvent(entity, stack, amount);
        MinecraftForge.EVENT_BUS.post(event);
        amount = event.getAmount();
        amount = super.damageItem(stack, amount, entity, onBroken);
        this.applyUsageEffects(entity, stack, amount);
        return amount;
    }

    public EffectData[] getSBEffectData(ItemStack itemStack) {
        EffectData[] SBEffectData = new EffectData[]{new EffectData()};
        itemStack.getCapability(BLADESTATE).ifPresent(state -> {
            SBEffectData[0].efficiencyMap.put(SBItemEffects.REFINE, state.getBaseAttackModifier());
            SBEffectData[0].levelMap.put(SBItemEffects.REFINE, state.getBaseAttackModifier());
        });
        return SBEffectData;
    }

    public EffectData getEffectData(ItemStack itemStack) {
        if (logger.isDebugEnabled()) {
            logger.debug("Gathering effect data for {} ({})", this.getItemName(itemStack), this.getDataCacheKey(itemStack));
        }

        return
                Stream.concat(
                                Stream.concat(
                                        this.getAllModules(itemStack).stream().map((module) -> module.getEffectData(itemStack)),
                                        Arrays.stream(this.getSynergyData(itemStack)).map((synergy) -> synergy.effects)
                                ),
                                Arrays.stream(getSBEffectData(itemStack))
                        )
                        .filter(Objects::nonNull)
                        .reduce(null, EffectData::merge);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
        if (this.isBroken(itemStack)) {
        } else if (slot == EquipmentSlot.MAINHAND) {
            Multimap<Attribute, AttributeModifier> Tetra = this.getAttributeModifiersCached(itemStack);
            result.putAll(Tetra);
            LazyOptional<ISlashBladeState> state = itemStack.getCapability(BLADESTATE);
            state.ifPresent((s) -> {
                EnumSet<SwordType> swordType = SwordType.from(itemStack);
                float baseAttackModifier = getEffectLevel(itemStack, SBItemEffects.REFINE);
                int refine = s.getRefine();
                float refineFactor = swordType.contains(SwordType.FIERCEREDGE) ? 0.1F : 0.05F;
                float attackAmplifier = (1.0F - 1.0F / (1.0F + refineFactor * (float) refine)) * baseAttackModifier;


                //double damage = (double) attackAmplifier;//+ (double) baseAttackModifier  - (double) 4.0F;
                SlashBladeEvent.UpdateAttackEvent event = new SlashBladeEvent.UpdateAttackEvent(itemStack, s, attackAmplifier);
                MinecraftForge.EVENT_BUS.post(event);
                AttributeModifier attack = new AttributeModifier(UUID.nameUUIDFromBytes(new byte[]{127, 0, 0, 11, 45, 14}), "Weapon modifier", event.getNewDamage(), AttributeModifier.Operation.ADDITION);
                //result.remove(Attributes.ATTACK_DAMAGE, attack);
                result.put(Attributes.ATTACK_DAMAGE, attack);
                //result.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(UUID.nameUUIDFromBytes(new byte[]{127, 1, 0, 11, 45, 14}), "Reach amplifer", s.isBroken() ? ReachModifier.BrokendReach() : ReachModifier.BladeReach(), AttributeModifier.Operation.ADDITION));
            });
        }
        return result;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        if (!this.isBroken(itemStack)) {

            ItemEffectHandler.applyHitEffects(itemStack, target, attacker);
            this.applyPositiveUsageEffects(attacker, itemStack, 1.0F);


            int skulkTaintLevel = this.getEffectLevel(itemStack, ItemEffect.sculkTaint);
            if (skulkTaintLevel > 0) {
                SculkTaintEffect.perform((ServerLevel) target.level(), target.blockPosition(), skulkTaintLevel, this.getEffectEfficiency(itemStack, ItemEffect.sculkTaint));
            }

            this.applyNegativeUsageEffects(attacker, itemStack, (double) 1.0F);
        }
        return super.hurtEnemy(itemStack, target, attacker);
    }
}
