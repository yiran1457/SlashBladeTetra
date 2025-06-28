package net.yiran.sbtetra;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yiran.sbtetra.compat.CompatHandler;
import net.yiran.sbtetra.item.SlashBladeModularItem;
import net.yiran.sbtetra.module.SlashBladeModule;
import net.yiran.sbtetra.module.SlashBladeSoulRegistry;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.impl.holo.HoloPage;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloGui;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleRegistry;
import se.mickelus.tetra.module.schematic.InvalidSchematicException;

import java.util.Optional;

import static mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE;

@Mod(SlashBladeTetra.MODID)
@SuppressWarnings({"all", "removal"})
public class SlashBladeTetra {
    public static final String MODID = "slashbladetetra";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> MODLUAR = ITEMS.register("slashblade", SlashBladeModularItem::new);
    public static final TagKey<Item> REPLACEMENT = ItemTags.create(new ResourceLocation(MODID, "replacement"));

    public SlashBladeTetra() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        CompatHandler.init();
        commonInit(modEventBus);
        if (FMLEnvironment.dist.isClient()) {
            clientInit(modEventBus);
        }
    }

    public static void clientInit(IEventBus bus) {
        bus.addListener(CompatHandler::clientSetup);

        bus.addListener(SlashBladeClientHandler::doClientStuff);
        bus.addListener(SlashBladeClientHandler::Baked);
        bus.addListener(SlashBladeClientHandler::buildContents);
    }

    public static void commonInit(IEventBus bus) {
        bus.addListener(CompatHandler::setup);

        bus.addListener(SlashBladeTetra::onCommonSetup);
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {

        TetraEnchantmentHelper.registerMapping(ItemAspect.get("slashblade"), new TetraEnchantmentHelper.EnchantmentRules("additions/slashblade", "exclusions/slashblade", EnchantmentCategory.WEAPON, EnchantmentCategory.BREAKABLE));

        ModuleRegistry moduleRegistry = ModuleRegistry.instance;
        moduleRegistry.registerModuleType(new ResourceLocation("slashbladetetra", "blade"), SlashBladeModule::new);
        ItemUpgradeRegistry.instance.registerReplacementHook(SlashBladeTetra::replacementHook);

        SlashBladeSoulRegistry.init();

    }

    public static ItemStack replacementHook(ItemStack itemStack, ItemStack replaceItemStack) {
        if (!(replaceItemStack.getItem() instanceof SlashBladeModularItem slashBladeModularItem))
            return replaceItemStack;
        replaceItemStack.getOrCreateTag().put("bladeState", itemStack.getTag().getCompound("bladeState").copy());
        replaceItemStack.getCapability(BLADESTATE).map(s -> {
            s.deserializeNBT(itemStack.capNBT.copy().getCompound("Parent"));
            s.setMaxDamage(Optional.of(slashBladeModularItem.getPropertiesCached(itemStack)).map((properties) -> properties.durability * properties.durabilityMultiplier).map(Math::round).orElse(0));
            return s;
        });
        return replaceItemStack;
    }
}
