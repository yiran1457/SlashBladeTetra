package net.yiran.sbtetra.module.schematic;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.init.SBItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.SlashBladeTetra;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.module.data.GlyphData;

import static mods.flammpfeil.slashblade.item.ItemSlashBlade.BLADESTATE;

@SuppressWarnings("all")
public class SoulExtractionSchematic extends BaseSoulSchematic {
    public static TagKey<Item> TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath(SlashBladeTetra.MODID, "soulextraction"));

    public SoulExtractionSchematic() {
        super(
                new GlyphData(new ResourceLocation("slashbladetetra:textures/gui/texture.png"), 64, 0),
                "soulextraction",
                TAG
        );
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        if (slot == null || !slot.equals("slashblade/soul")) return false;
        ISlashBladeState state = targetStack.getCapability(BLADESTATE).orElse(null);
        if (state == null) return false;
        if (state.getProudSoulCount() < Config.Server.SoulDropNeeded.get()) return false;
        return true;
    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] itemStacks, boolean b, String soul, Player player) {
        //原理图应用逻辑，返回结果物品
        ItemStack newStack = itemStack.copy();
        if (b)
            newStack.getCapability(BLADESTATE).ifPresent((bladeState) -> {
                int need = Config.Server.SoulDropNeeded.get();
                int count;
                if (itemStacks[0].is(TAG)) {
                    count = bladeState.getProudSoulCount() / need;
                    itemStacks[0].shrink(1);
                } else {
                    count = Math.min(Config.Server.MaxSoulDrop.get(), bladeState.getProudSoulCount() / need);
                }

                while (count > 0) {
                    if (count > 64) {
                        ItemStack soulStack = new ItemStack(SBItems.proudsoul_tiny.asItem());
                        soulStack.setCount(64);
                        if (!player.getInventory().add(soulStack)) {
                            player.drop(soulStack, false);
                        }
                        bladeState.setProudSoulCount(bladeState.getProudSoulCount() - need * 64);
                        count -= 64;
                    } else {
                        ItemStack soulStack = new ItemStack(SBItems.proudsoul_tiny.asItem());
                        soulStack.setCount(count);
                        if (!player.getInventory().add(soulStack)) {
                            player.drop(soulStack, false);
                        }
                        bladeState.setProudSoulCount(bladeState.getProudSoulCount() - need * count);
                        count = 0;
                    }
                }

            });
        return newStack;
    }

    @Override
    public Object[] getDescriptionExtraValues(@Nullable ItemStack itemStack) {
        int max = Config.Server.MaxSoulDrop.get();
        int need = Config.Server.SoulDropNeeded.get();
        if (itemStack != null) {
            var cap = itemStack.getCapability(BLADESTATE).orElse(null);
            if (cap != null) {
                return new Object[]{max, need, Math.min(max, cap.getProudSoulCount() / need), cap.getProudSoulCount() / need};
            }
        }
        return new Object[]{max, need, "?", "?"};
    }
}
