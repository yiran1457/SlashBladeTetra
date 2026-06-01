package net.yiran.sbtetra.gui;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yiran.sbtetra.gui.statgetter.SBTStatGetterDurability;
import net.yiran.sbtetra.gui.tooltipgetter.ToolTipGetterRefineStrengthening;
import net.yiran.sbtetra.gui.tooltipgetter.ToolTipGetterSBCap;
import net.yiran.sbtetra.itemeffect.SBItemEffects;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.*;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;

import java.util.ArrayList;
import java.util.List;

public class SBGuiStats {
    public static List<GuiStatBar> BARS;
    public static GuiStatBar REFINE;
    public static GuiStatBar KILL_COUNT;
    public static GuiStatBar PROUD_SOUL_COUNT;
    public static GuiStatBar REFINE_COUNT;
    public static GuiStatBar SBT_DURABILITY;
    public static GuiStatBar SOUL_MAPPING;

    static {
        BARS = new ArrayList<>();
        REFINE = new GuiStatBar(0, 0, StatsHelper.barLength,
                "tetra.stats.RefineStrengthening", 0, 200, false, ToolTipGetterRefineStrengthening.levelGetter, LabelGetterBasic.singleDecimalLabel,
                new ToolTipGetterRefineStrengthening()
        );
        BARS.add(REFINE);

        KILL_COUNT = new GuiStatBar(0, 0, StatsHelper.barLength,
                "tetra.stats.KillCount", 0, 1000, false, ToolTipGetterSBCap.KILL_COUNT, LabelGetterBasic.integerLabel,
                new ToolTipGetterSBCap("KillCount", ToolTipGetterSBCap.KILL_COUNT)
        );
        BARS.add(KILL_COUNT);

        PROUD_SOUL_COUNT = new GuiStatBar(0, 0, StatsHelper.barLength,
                "tetra.stats.ProudSoulCount", 0, 100000, false, ToolTipGetterSBCap.PROUD_SOUL_COUNT, LabelGetterBasic.integerLabel,
                new ToolTipGetterSBCap("ProudSoulCount", ToolTipGetterSBCap.PROUD_SOUL_COUNT)
        );
        BARS.add(PROUD_SOUL_COUNT);

        REFINE_COUNT = new GuiStatBar(0, 0, StatsHelper.barLength,
                "tetra.stats.RefineCount", 0, 500, false, ToolTipGetterSBCap.REFINE_COUNT, LabelGetterBasic.integerLabel,
                new ToolTipGetterSBCap("RefineCount", ToolTipGetterSBCap.REFINE_COUNT)
        );
        BARS.add(REFINE_COUNT);

        SBT_DURABILITY = new GuiStatBar(0, 0, StatsHelper.barLength,
                "tetra.stats.sbtdurability", 0, 2400, false, SBTStatGetterDurability.instance, LabelGetterBasic.integerLabel, new TooltipGetterInteger("tetra.stats.sbtdurability.tooltip", SBTStatGetterDurability.instance));
        BARS.add(SBT_DURABILITY);

        var getter1 = new StatGetterEffectLevel(SBItemEffects.MAPPING);
        var getter2 = ToolTipGetterRefineStrengthening.levelGetter;
        var getter3 = new StatGetterEffectEfficiency(SBItemEffects.MAPPING);
        var getter4 = new StatGetterMultiply(getter2, getter3);
        var getter5 = new StatGetterAdd(getter1, getter4);
        SOUL_MAPPING = new GuiStatBar(0, 0, StatsHelper.barLength,
                "tetra.stats.SoulBladeMapping", 0, 100, false, getter5, LabelGetterBasic.integerLabel, new TooltipGetterMultiValue(
                "tetra.stats.SoulBladeMapping.tooltip",
                new IStatGetter[]{getter5, getter1, getter4, getter3, getter2},
                new IStatFormat[]{StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.twoDecimal,s->String.format("%.02f%%",s*100), StatFormat.oneDecimal}
        ) {
            @Override
            public String getTooltipBase(Player player, ItemStack itemStack) {
                return I18n.get(this.localizationKey, this.formatters[0].get(this.statGetter[0].getValue(player, itemStack)));
            }

            @Override
            public String getTooltipExtension(Player player, ItemStack itemStack) {
                Object[] values = new String[this.statGetter.length];

                for (int i = 0; i < this.statGetter.length; ++i) {
                    values[i] = this.formatters[i].get(this.statGetter[i].getValue(player, itemStack));
                }

                return I18n.get(this.localizationKey + "_extended", values);
            }
        });
        BARS.add(SOUL_MAPPING);

    }

    @OnlyIn(Dist.CLIENT)
    public static void clientInit() {
        for (GuiStatBar bar : BARS) {
            WorkbenchStatsGui.addBar(bar);
            HoloStatsGui.addBar(bar);
        }
    }
}
