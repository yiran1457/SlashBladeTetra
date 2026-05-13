package net.yiran.sbtetra.module.schematic;

import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.SlashBladeTetra;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.module.data.GlyphData;

import java.util.List;
import java.util.Objects;

import static mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE;

@SuppressWarnings("all")
public class EnchantedSoulExtractionSchematic extends BaseSoulSchematic {
    public static TagKey<Item> TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath(SlashBladeTetra.MODID, "enchantedsoulextraction"));

    public EnchantedSoulExtractionSchematic() {
        super(
                new GlyphData(new ResourceLocation("slashbladetetra:textures/gui/texture.png"), 64, 0),
                "enchantedsoulextraction",
                TAG
        );
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        if (slot == null || !slot.equals("slashblade/soul")) return false;
        ISlashBladeState state = targetStack.getCapability(BLADESTATE).orElse(null);
        if (state == null) return false;
        if (state.getKillCount() - 1000 < Config.EnchantedSoulDropNeeded.get()) return false;
        return true;
    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] itemStacks, boolean b, String soul, Player player) {
        //原理图应用逻辑，返回结果物品
        ItemStack newStack = itemStack.copy();
        if (b)
            newStack.getCapability(BLADESTATE).ifPresent((bladeState) -> {
                int need = Config.EnchantedSoulDropNeeded.get();
                int count;
                if (itemStacks[0].is(TAG)) {
                    count = (bladeState.getProudSoulCount() - 1000) / need;
                    itemStacks[0].shrink(1);
                } else {
                    count = Math.min((bladeState.getProudSoulCount() - 1000) / need, Config.MaxEnchantedSoulDrop.get());
                }

                List<Enchantment> enchantments = ForgeRegistries.ENCHANTMENTS.getValues().stream()
                        .filter(newStack::canApplyAtEnchantingTable)
                        .filter(enchantment -> !SlashBladeConfig.NON_DROPPABLE_ENCHANTMENT.get()
                                .contains(Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(enchantment)).toString()))
                        .toList();
                for (int i = 0; i < count; i += 1) {
                    ItemStack enchanted_soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());
                    Enchantment enchant = enchantments.get(player.getRandom().nextInt(0, enchantments.size()));
                    if (enchant != null) {

                        enchanted_soul.enchant(enchant, 1);
                        if (!player.getInventory().add(enchanted_soul)) {
                            player.drop(enchanted_soul, false);
                        }

                    }
                    bladeState.setKillCount(bladeState.getProudSoulCount() - need);
                }
            });
        return newStack;
    }

    @Override
    public Object[] getDescriptionExtraValues(@Nullable ItemStack itemStack) {
        int max = Config.MaxEnchantedSoulDrop.get();
        int need = Config.EnchantedSoulDropNeeded.get();
        if (itemStack != null) {
            var cap = itemStack.getCapability(BLADESTATE).orElse(null);
            if (cap != null) {
                return new Object[]{max, need, Math.min(max, cap.getKillCount() / need), cap.getKillCount() / need};
            }
        }
        return new Object[]{max, need, "?", "?"};
    }
}
