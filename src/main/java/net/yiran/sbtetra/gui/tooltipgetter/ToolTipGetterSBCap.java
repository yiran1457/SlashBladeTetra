package net.yiran.sbtetra.gui.tooltipgetter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.gui.statgetter.StatGetterSBCap;
import se.mickelus.tetra.gui.stats.getter.ITooltipGetter;

public class ToolTipGetterSBCap implements ITooltipGetter {
    public static StatGetterSBCap KILL_COUNT = new StatGetterSBCap(iSlashBladeState-> (double) iSlashBladeState.getKillCount());
    public static StatGetterSBCap PROUD_SOUL_COUNT = new StatGetterSBCap(iSlashBladeState-> (double) iSlashBladeState.getProudSoulCount());
    public static StatGetterSBCap REFINE_COUNT = new StatGetterSBCap(iSlashBladeState-> (double) iSlashBladeState.getRefine());
    public String name;
    public StatGetterSBCap getterSBCap;
    public ToolTipGetterSBCap(String name,StatGetterSBCap getterSBCap) {
        this.name = name;
        this.getterSBCap = getterSBCap;
    }

    public String getTooltipBase(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats."+name+".tooltip",
                String.format("%.0f",getterSBCap.getValue(player, itemStack))
        );
    }

}
