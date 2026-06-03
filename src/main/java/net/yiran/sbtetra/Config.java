package net.yiran.sbtetra;

import net.minecraftforge.common.ForgeConfigSpec;
import net.yiran.sbtetra.gui.statgetter.StatGetterRefineFactor;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static class Server {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.ConfigValue<List<String>> CantWrapperItems;
        public static final ForgeConfigSpec.ConfigValue<Integer> MaxEnchantedSoulDrop;
        public static final ForgeConfigSpec.ConfigValue<Integer> EnchantedSoulDropNeeded;
        public static final ForgeConfigSpec.ConfigValue<Integer> MaxSoulDrop;
        public static final ForgeConfigSpec.ConfigValue<Integer> SoulDropNeeded;
        public static final ForgeConfigSpec.ConfigValue<Boolean> EnableCiallo;
        public static final ForgeConfigSpec.ConfigValue<StatGetterRefineFactor.Rule> SOUL_BLADE_MAPPING_RULE;
        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        static {
            CantWrapperItems = BUILDER
                    .define("cantWrapperItems", new ArrayList<>());
            BUILDER.push("Schematic");
            MaxEnchantedSoulDrop = BUILDER
                    .comment("一次最多掉落的附魔耀魂数量")
                    .define("maxEnchantedSoulDrop", 10);
            EnchantedSoulDropNeeded = BUILDER
                    .comment("一个附魔耀魂所需杀敌数的量")
                    .define("enchantedSoulDropNeeded", 15);
            MaxSoulDrop = BUILDER
                    .comment("一次最多掉落的普通耀魂数量")
                    .define("maxSoulDrop", 20);
            SoulDropNeeded = BUILDER
                    .comment("一个普通耀魂所需耀魂值的量")
                    .define("soulDropNeeded", 88);
            EnableCiallo = BUILDER
                    .comment("是否添加ciallo的两个原理图（联动）")
                    .define("enableCiallo", true);
            BUILDER.comment("设定 剑魂映射 中 锻造影响值 的公式");
            for (StatGetterRefineFactor.Rule value : StatGetterRefineFactor.Rule.values()) {
                BUILDER.comment("  " + value.name() + " : " + value.desc);
            }
            SOUL_BLADE_MAPPING_RULE = BUILDER.defineEnum("soulBladeMappingRule", StatGetterRefineFactor.Rule.COMMON);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }

    public static class Client {
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.ConfigValue<Boolean> EnableRenderWrapper;
        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        static {
            EnableRenderWrapper = BUILDER
                    .define("EnableRenderWrapper", true);
            SPEC = BUILDER.build();
        }
    }

}