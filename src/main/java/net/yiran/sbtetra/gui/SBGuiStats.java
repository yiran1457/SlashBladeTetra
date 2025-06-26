package net.yiran.sbtetra.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yiran.sbtetra.gui.tooltipgetter.ToolTipGetterSBCap;
import net.yiran.sbtetra.gui.tooltipgetter.ToolTipGetterRefineStrengthening;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.LabelGetterBasic;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;

import java.util.ArrayList;
import java.util.List;

import static net.yiran.sbtetra.gui.tooltipgetter.ToolTipGetterRefineStrengthening.levelGetter;

public class SBGuiStats {
    public static List<GuiStatBar> BARS;
    public static GuiStatBar REFINE;
    public static GuiStatBar KILL_COUNT;
    public static GuiStatBar PROUD_SOUL_COUNT;
    public static GuiStatBar REFINE_COUNT;
    static{
        BARS = new ArrayList<>();
        REFINE = new GuiStatBar(0, 0, StatsHelper.barLength,
                "tetra.stats.RefineStrengthening", 0, 200, false, levelGetter, LabelGetterBasic.singleDecimalLabel,
                new ToolTipGetterRefineStrengthening()
        );
        BARS.add(REFINE);

        KILL_COUNT = new GuiStatBar(0,0, StatsHelper.barLength,
                "tetra.stats.KillCount",0,1000,false, ToolTipGetterSBCap.KILL_COUNT, LabelGetterBasic.integerLabel,
                new ToolTipGetterSBCap("KillCount",ToolTipGetterSBCap.KILL_COUNT)
                );
        BARS.add(KILL_COUNT);

        PROUD_SOUL_COUNT = new GuiStatBar(0,0, StatsHelper.barLength,
                "tetra.stats.ProudSoulCount",0,100000,false, ToolTipGetterSBCap.PROUD_SOUL_COUNT, LabelGetterBasic.integerLabel,
                new ToolTipGetterSBCap("ProudSoulCount",ToolTipGetterSBCap.PROUD_SOUL_COUNT)
        );
        BARS.add(PROUD_SOUL_COUNT);

        REFINE_COUNT = new GuiStatBar(0,0, StatsHelper.barLength,
                "tetra.stats.RefineCount",0,500,false, ToolTipGetterSBCap.REFINE_COUNT, LabelGetterBasic.integerLabel,
                new ToolTipGetterSBCap("RefineCount",ToolTipGetterSBCap.REFINE_COUNT)
        );
        BARS.add(REFINE_COUNT);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientInit() {
        for (GuiStatBar bar : BARS) {
            WorkbenchStatsGui.addBar(bar);
            HoloStatsGui.addBar(bar);
        }
    }
}
