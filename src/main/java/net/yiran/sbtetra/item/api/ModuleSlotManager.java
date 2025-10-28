package net.yiran.sbtetra.item.api;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.GuiModuleOffsets;

import java.util.ArrayList;
import java.util.List;

public class ModuleSlotManager {
    public static String[] DEF_SLOTS = new String[]{"slashblade/handle", "slashblade/blade", "slashblade/tsuba", "slashblade/scabbard"};


    public static String[] getMajorModuleKeys(ItemStack itemStack) {
        List<String> list = new ArrayList<>(List.of(DEF_SLOTS));
        if (hasSoul(itemStack))
            list.add("slashblade/soul");
        return list.toArray(new String[0]);
    }

    public static GuiModuleOffsets getMajorGuiOffsets(ItemStack itemStack) {
        IntArrayList list = new IntArrayList();
        list.add(-25);
        list.add(-4);
        list.add(9);
        list.add(24);
        list.add(-16);
        list.add(19);
        list.add(4);
        list.add(-2);
        if(hasSoul(itemStack)){
            list.add(50);
            list.add(12);
        }
        return new GuiModuleOffsets(list.elements());
    }

    public static String[] getMinorModuleKeys(ItemStack itemStack) {
        return new String[]{};
    }
    public static boolean hasSoul(ItemStack itemStack) {
        try {
            return SwordType.from(itemStack).containsAll(List.of(SwordType.BEWITCHED, SwordType.FIERCEREDGE));
        } catch (Exception e) {
            return false;
        }
    }
}
