package net.yiran.sbtetra.gui.tooltipgetter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yiran.sbtetra.Config;
import net.yiran.sbtetra.gui.statgetter.StatGetterRefineFactor;
import net.yiran.sbtetra.itemeffect.SBItemEffects;
import se.mickelus.tetra.gui.stats.getter.*;

public class TooltipGetterSoulBladeMapping implements ITooltipGetter {
    //mapping固定值
    public static IStatGetter getter1 = new StatGetterEffectLevel(SBItemEffects.MAPPING);
    //刀魂增幅
    public static IStatGetter getter2 = ToolTipGetterRefineStrengthening.levelGetter;
    //mapping收刀魂影响率
    public static IStatGetter getter3 = new StatGetterEffectEfficiency(SBItemEffects.MAPPING);
    //影响值
    public static IStatGetter getter4 = new StatGetterMultiply(getter2, getter3);
    //数值结算
    public static IStatGetter getter5 = new StatGetterAdd(getter1, getter4);
    //锻造影响因子
    public static IStatGetter getter6 = StatGetterRefineFactor.instance;
    //最终加成值
    public static IStatGetter getter7 = new StatGetterMultiply(getter5, getter6);

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.SoulBladeMapping.tooltip", String.format("%.2f", getter7.getValue(player, itemStack)));
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get(
                "tetra.stats.SoulBladeMapping.tooltip_extended",
                String.format("%.2f", getter7.getValue(player, itemStack)),
                String.format("%.2f", getter5.getValue(player, itemStack)),
                String.format("%.2f", getter1.getValue(player, itemStack)),
                String.format("%.0f", getter4.getValue(player, itemStack)),
                String.format("%.1f%%", getter3.getValue(player, itemStack)*100),
                String.format("%.2f", getter2.getValue(player, itemStack)),
                String.format("%.1f%%", getter6.getValue(player, itemStack) * 100),
                Config.Server.SOUL_BLADE_MAPPING_RULE.get()
        );
    }
}
