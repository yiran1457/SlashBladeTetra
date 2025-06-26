package net.yiran.sbtetra.item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.schematic.RepairSchematic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSlashBladeModularItem extends ItemSlashBlade implements IModularItem {

    private final Cache<String, Multimap<Attribute, AttributeModifier>> attributeCache;
    private final Cache<String, ToolData> toolCache;
    private final Cache<String, EffectData> effectCache;
    private final Cache<String, ItemProperties> propertyCache;
    protected SynergyData[] synergies;

    public AbstractSlashBladeModularItem() {
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
    public void tickProgression(LivingEntity entity, ItemStack itemStack, int multiplier) {
        if (ConfigHandler.moduleProgression.get()) {
            this.tickHoningProgression(entity, itemStack, multiplier);

            for(ItemModuleMajor module : this.getMajorModules(itemStack)) {
                if(module==null) continue;
                module.tickProgression(entity, itemStack, multiplier);
            }

        }
    }

    @Override
    public void clearCaches() {
        this.attributeCache.invalidateAll();
        this.toolCache.invalidateAll();
        this.effectCache.invalidateAll();
        this.propertyCache.invalidateAll();
    }
    public static String[] DEF_SLOTS = new String[]{"slashblade/handle", "slashblade/blade", "slashblade/tsuba", "slashblade/scabbard"};

    @Override
    public String[] getMajorModuleKeys(ItemStack itemStack) {
        List<String> list= new ArrayList<>(List.of(DEF_SLOTS));
        if(issss(itemStack))
            list.add("slashblade/soul");
        if(checkLimit(itemStack))
            list.add("slashblade/special");
        return list.toArray(new String[0]);//刀柄 刀条;
    }

    @Override
    public String[] getMinorModuleKeys(ItemStack itemStack) {
        //return new String[]{"sb/tsuba","sb/scabbard"};//刀镡 刀鞘;
        return new String[]{};
    }

    @Override
    public String[] getRequiredModules(ItemStack itemStack) {
        return new String[]{"slashblade/handle", "slashblade/blade","slashblade/tsuba","slashblade/scabbard"};
    }

    @Override
    public GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {

        List<Integer> list= new ArrayList<>();
        list.add(-25);
        list.add(-4);
        list.add(9);
        list.add(24);
        list.add(-16);
        list.add(19);
        list.add(4);
        list.add(-2);
        if(issss(itemStack)){
            list.add(50);
            list.add(12);
        }
        if(checkLimit(itemStack)) {
            list.add(-50);
            list.add(32);
        }
        return new GuiModuleOffsets(list.stream().mapToInt(i->i).toArray());
    }

    public boolean issss(ItemStack itemStack) {
        return SwordType.from(itemStack).containsAll(List.of(SwordType.BEWITCHED,SwordType.FIERCEREDGE));

    }

    public boolean checkLimit(ItemStack itemStack) {
        return false;
        /*
        CompoundTag tag = itemStack.getTag();
        if (tag == null) return false;
        for (String s : DEF_SLOTS) {
            String moduleName = tag.getString(s);
            if (moduleName.isEmpty()) return false;
            ItemModuleMajor module = (ItemModuleMajor) ItemUpgradeRegistry.instance.getModule(moduleName);
            if(module.getImprovementLevel(itemStack, "soul_infusion")<6){
                return false;
            }
        }
        return true;*/
    }

    @Override
    public GuiModuleOffsets getMinorGuiOffsets(ItemStack itemStack) {
        //return new GuiModuleOffsets(-18, 0, -18, 24);
        return new GuiModuleOffsets();
    }

    @Override
    public int getHoneBase(ItemStack itemStack) {
        return 512;
    }

    @Override
    public int getHoneIntegrityMultiplier(ItemStack itemStack) {
        return 256;
    }

    @Override
    public boolean canGainHoneProgress(ItemStack itemStack) {
        return true;
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
    public boolean isBookEnchantable(ItemStack itemStack, ItemStack bookStack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack itemStack, Enchantment enchantment) {
        return this.acceptsEnchantment(itemStack, enchantment, true);
    }

    @Override
    public int getEnchantmentValue(ItemStack itemStack) {
        return this.getEnchantability(itemStack);
    }

    @Override
    public boolean isBroken(ItemStack itemStack) {
        return itemStack.getOrCreateTagElement("bladeState").getBoolean("isBroken");
    }

    @Override
    public Component getName(ItemStack stack) {
        String id = this.getDescriptionId(stack);
        if (!id.endsWith("item.slashbladetetra.slashblade"))
            return Component.translatable(id);
        return Component.literal(this.getItemName(stack));
    }

}
