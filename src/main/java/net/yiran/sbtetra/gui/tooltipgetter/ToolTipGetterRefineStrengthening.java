package net.yiran.sbtetra.gui.tooltipgetter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.*;

import static net.yiran.sbtetra.itemeffect.SBItemEffects.REFINE;

public class ToolTipGetterRefineStrengthening implements ITooltipGetter {

    public static final IStatGetter levelGetter = new StatGetterEffectLevel(REFINE) ;
    public static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(REFINE);

    public ToolTipGetterRefineStrengthening() {
    }

    public String getTooltipBase(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.RefineStrengthening.tooltip",
                String.format("%.1f",this.levelGetter.getValue(player, itemStack))
        );
    }

    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    public String getTooltipExtension(Player player, ItemStack itemStack) {
        double level = this.levelGetter.getValue(player, itemStack);
        double efficiency = this.efficiencyGetter.getValue(player, itemStack);
        return I18n.get("tetra.stats.RefineStrengthening.tooltip_extended",
                String.format("%.1f", level-efficiency),
                String.format("%.1f", efficiency)
        );
    }
}
