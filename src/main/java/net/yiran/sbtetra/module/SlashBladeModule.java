package net.yiran.sbtetra.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.Filter;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.TweakData;

import java.util.*;


public class SlashBladeModule extends ItemModuleMajor {
    public SlashBladeModule(ResourceLocation identifier, ModuleData data) {
        super(data.slots[0], identifier.getPath());

        variantData = data.variants;

        renderLayer = data.renderLayer;
        namePriority = data.namePriority;
        prefixPriority = data.prefixPriority;

        if (data.improvements.length > 0) {
            improvements = Arrays.stream(data.improvements)
                    .map(rl -> rl.getPath().endsWith("/")
                            ? DataManager.instance.improvementData.getDataIn(rl)
                            : Optional.ofNullable(DataManager.instance.improvementData.getData(rl))
                            .map(Collections::singletonList)
                            .orElseGet(Collections::emptyList))
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .flatMap(Arrays::stream)
                    .filter(Filter.distinct(improvement -> improvement.key + ":" + improvement.level))
                    .toArray(ImprovementData[]::new);

            settleMax = Arrays.stream(improvements)
                    .filter(improvement -> improvement.key.equals(settleImprovement))
                    .mapToInt(ImprovementData::getLevel)
                    .max()
                    .orElse(0);
        }

        if (data.tweakKey != null) {
            TweakData[] tweaks = DataManager.instance.tweakData.getData(data.tweakKey);
            if (tweaks != null) {
                this.tweaks = tweaks;
            } else {
                this.tweaks = new TweakData[0];
            }
        }

    }

    @Override
    public int getSettleMaxCount(ItemStack itemStack) {
        return this.settleMax;
    }

    @Override
    public int getSettleLimit(ItemStack itemStack) {
        return (int) ((ConfigHandler.settleLimitBase.get() + getDurability(itemStack) * ConfigHandler.settleLimitDurabilityMultiplier.get())
                * Math.max(getImprovementLevel(itemStack, settleImprovement) * ConfigHandler.settleLimitLevelMultiplier.get(), 1f));

    }


}
