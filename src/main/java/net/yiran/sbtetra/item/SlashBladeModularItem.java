package net.yiran.sbtetra.item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.event.ModularItemDamageEvent;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.schematic.RepairSchematic;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SlashBladeModularItem extends ItemSlashBlade implements ISlashBladeTetra {

    private final Cache<String, Multimap<Attribute, AttributeModifier>> attributeCache;
    private final Cache<String, ToolData> toolCache;
    private final Cache<String, EffectData> effectCache;
    private final Cache<String, ItemProperties> propertyCache;
    protected SynergyData[] synergies;

    public SlashBladeModularItem() {
        super(Tiers.IRON, 0, 0, new Properties().stacksTo(1));
        this.attributeCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(5L, TimeUnit.MINUTES).build();
        this.toolCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(5L, TimeUnit.MINUTES).build();
        this.effectCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(5L, TimeUnit.MINUTES).build();
        this.propertyCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(5L, TimeUnit.MINUTES).build();
        this.synergies = new SynergyData[0];
        DataManager.instance.moduleData.onReload(this::clearCaches);
        SchematicRegistry.instance.registerSchematic(new RepairSchematic(this, "slashblade"));
    }


    @Override
    public Item getItem() {
        return this;
    }

    @Override
    public void clearCaches() {
        this.attributeCache.invalidateAll();
        this.toolCache.invalidateAll();
        this.effectCache.invalidateAll();
        this.propertyCache.invalidateAll();
    }

    @Override
    public Cache<String, Multimap<Attribute, AttributeModifier>> getAttributeModifierCache() {
        return attributeCache;
    }

    @Override
    public Cache<String, EffectData> getEffectDataCache() {
        return effectCache;
    }

    @Override
    public Cache<String, ItemProperties> getPropertyCache() {
        return propertyCache;
    }

    @Override
    public SynergyData[] getAllSynergyData(ItemStack itemStack) {
        return synergies;
    }

    @Override
    public Component getName(ItemStack stack) {
        String id = this.getDescriptionId(stack);
        if (!id.endsWith("item.slashbladetetra.slashblade")) return Component.translatable(id);
        return Component.literal(this.getItemName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.addAll(this.getTooltip(stack, world, flag));
    }

    @Override
    public ItemStack getDefaultInstance() {
        var stack = new ItemStack(this);
        stack.getOrCreateTag().putString("id", "DefaultInstance");
        IModularItem.putModuleInSlot(stack, "slashblade/handle", "slashblade/handle/handle", "handle/unnamed");
        IModularItem.putModuleInSlot(stack, "slashblade/blade", "slashblade/blade/blade", "blade/unnamed");
        IModularItem.putModuleInSlot(stack, "slashblade/tsuba", "slashblade/tsuba/tsuba", "tsuba/unnamed");
        IModularItem.putModuleInSlot(stack, "slashblade/scabbard", "slashblade/scabbard/scabbard", "scabbard/unnamed");
        return stack;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return sbt$getMaxDamage(stack);
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

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack itemStack) {
        return sbt$getAttributeModifiers(slot, itemStack);
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        sbt$hurtEnemy(itemStack, target, attacker);
        return super.hurtEnemy(itemStack, target, attacker);
    }
}
