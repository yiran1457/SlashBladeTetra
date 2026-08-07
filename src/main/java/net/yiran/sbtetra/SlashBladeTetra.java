package net.yiran.sbtetra;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yiran.sbtetra.api.SchematicRegisterManager;
import net.yiran.sbtetra.compat.CompatHandler;
import net.yiran.sbtetra.craft.SBTIngredientManager;
import net.yiran.sbtetra.item.ISlashBladeTetra;
import net.yiran.sbtetra.item.ModularExchangeItem;
import net.yiran.sbtetra.item.SlashBladeModularItem;
import net.yiran.sbtetra.module.SlashBladeModule;
import net.yiran.sbtetra.module.SlashBladeSoulRegistry;
import net.yiran.sbtetra.module.schematic.EnchantedSoulExtractionSchematic;
import net.yiran.sbtetra.module.schematic.SoulExtractionSchematic;
import net.yiran.sbtetra.module.schematic.TintingSchematic;
import net.yiran.sbtetra.recipe.SBTRecipeSerializers;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleRegistry;

import java.util.Optional;

import static mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE;

@Mod(SlashBladeTetra.MODID)
@SuppressWarnings({"all", "removal"})
public class SlashBladeTetra {
    public static final String MODID = "slashbladetetra";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> MODLUAR = ITEMS.register("slashblade", SlashBladeModularItem::new);
    public static final RegistryObject<Item> MODULAR_EXCHANGE = ITEMS.register("modular_exchange", ModularExchangeItem::new);
    public static final TagKey<Item> REPLACEMENT = ItemTags.create(new ResourceLocation(MODID, "replacement"));

    public SlashBladeTetra() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.SERVER,
                Config.Server.SPEC
        );
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                Config.Client.SPEC
        );

        ITEMS.register(modEventBus);
        SBTRecipeSerializers.register(modEventBus);
        SBTIngredientManager.register(MODLUAR);
        CompatHandler.init();
        commonInit(modEventBus);
        if (FMLEnvironment.dist.isClient()) {
            clientInit(modEventBus);
        }
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, SlashBladeTetra::onBladeStandAttack);
        //MinecraftForge.EVENT_BUS.register(SummonSwordHandler.class);

        SchematicRegisterManager.registerStaticSchematic(SoulExtractionSchematic::new);
        SchematicRegisterManager.registerStaticSchematic(EnchantedSoulExtractionSchematic::new);
        SchematicRegisterManager.registerStaticSchematic(TintingSchematic::new);
    }

    public static void clientInit(IEventBus bus) {
        bus.addListener(CompatHandler::clientSetup);
        SlashBladeClientHandler.init(bus);
    }

    public static void onBladeStandAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity livingEntity)) return;
        if (!(livingEntity.getMainHandItem().getItem() instanceof SlashBladeModularItem)) return;
        MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(event.getEntity(), event.getSource(), event.getAmount()));

    }

    public static void commonInit(IEventBus bus) {
        bus.addListener(CompatHandler::setup);
        bus.addListener(SchematicRegisterManager::onCommonSetup);
        bus.addListener(SlashBladeTetra::onCommonSetup);
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TetraEnchantmentHelper.registerMapping(ItemAspect.get("slashblade"), new TetraEnchantmentHelper.EnchantmentRules("additions/slashblade", "exclusions/slashblade", EnchantmentCategory.WEAPON, EnchantmentCategory.BREAKABLE));
            ModuleRegistry moduleRegistry = ModuleRegistry.instance;
            moduleRegistry.registerModuleType(new ResourceLocation("slashbladetetra", "blade"), SlashBladeModule::new);
            ItemUpgradeRegistry.instance.registerReplacementHook(SlashBladeTetra::replacementHook);
            SlashBladeSoulRegistry.init();
        });
    }

    public static ItemStack replacementHook(ItemStack itemStack, ItemStack replaceItemStack) {
        if (!(replaceItemStack.getItem() instanceof ISlashBladeTetra slashBladeModularItem))
            return replaceItemStack;
        ItemStack result = new ItemStack(SBTIngredientManager.getReplacement(itemStack));
        result.setTag(replaceItemStack.getOrCreateTag());
        result.getTag().put("bladeState", itemStack.getTag().getCompound("bladeState").copy());
        if (itemStack.capNBT != null)
            result.getCapability(BLADESTATE).map(s -> {
                s.deserializeNBT(itemStack.capNBT.copy().getCompound("Parent"));
                s.setMaxDamage(Optional.of(slashBladeModularItem.getPropertiesCached(replaceItemStack)).map((properties) -> properties.durability * properties.durabilityMultiplier).map(Math::round).orElseGet(() -> s.getMaxDamage()));
                return s;
            });
        return result;
    }
}
