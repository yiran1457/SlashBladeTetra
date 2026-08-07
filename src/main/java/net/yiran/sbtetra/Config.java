package net.yiran.sbtetra;

import net.minecraftforge.common.ForgeConfigSpec;

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
        public static final ForgeConfigSpec.ConfigValue<String> SOUL_BLADE_MAPPING_RULE;
        public static final ForgeConfigSpec.ConfigValue<String> REFINE_ATTACK_RULE;
        public static final ForgeConfigSpec.ConfigValue<Boolean> ModularExchangeTransferTetraData;
        public static final ForgeConfigSpec.ConfigValue<Boolean> ModularExchangeTransferEnchantments;
        public static final ForgeConfigSpec.ConfigValue<Boolean> ModularExchangeTransferProudSoul;
        public static final ForgeConfigSpec.ConfigValue<Boolean> ModularExchangeTransferKillCount;
        public static final ForgeConfigSpec.ConfigValue<Boolean> ModularExchangeTransferRefine;
        public static final ForgeConfigSpec.ConfigValue<List<String>> ModularExchangeAdditionalNbtKeys;
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
            BUILDER.pop();

            BUILDER.push("Expressions");
            SOUL_BLADE_MAPPING_RULE = BUILDER
                    .comment(
                            "设定 剑魂映射 中 锻造影响值 的公式",
                            "设置拔刀剑锻造数到剑魂映射倍率的计算公式。",
                            "可用参数：refine（拔刀剑锻造数）。公式返回值将作为倍率使用。",
                            "默认公式：(18 * refine + 100) / (9 * refine + 1000)"
                    )
                    .define("soulBladeMappingRule", "(18 * refine + 100) / (9 * refine + 1000)");
            REFINE_ATTACK_RULE = BUILDER
                    .comment(
                            "设置拔刀剑锻造数对普通攻击额外伤害的计算公式。",
                            "公式返回的值会作为额外攻击伤害；可用参数：",
                            "refine：拔刀剑锻造数；isFiercerEdge：是否为锋利剑刃（1 或 0）；",
                            "baseDamage：参与公式计算的基础额外伤害。默认公式：",
                            "baseDamage * (1 - 1 / (1 + (isFiercerEdge ? 0.1 : 0.05) * refine))"
                    )
                    .define("refineAttackRule", "baseDamage * (1 - 1 / (1 + (isFiercerEdge ? 0.1 : 0.05) * refine))");
            BUILDER.pop();

            BUILDER.push("ModularExchange");
            ModularExchangeTransferTetraData = BUILDER
                    .comment(
                            "是否转移模块交换水晶中的 Tetra 模组数据。",
                            "这是默认启用的唯一转移类别。"
                    )
                    .define("transferTetraData", true);
            ModularExchangeTransferEnchantments = BUILDER
                    .comment("是否转移原版/Tetra 附魔。")
                    .define("transferEnchantments", false);
            ModularExchangeTransferProudSoul = BUILDER
                    .comment("是否转移拔刀剑的耀魂数量。")
                    .define("transferProudSoul", false);
            ModularExchangeTransferKillCount = BUILDER
                    .comment("是否转移拔刀剑的击杀数。")
                    .define("transferKillCount", false);
            ModularExchangeTransferRefine = BUILDER
                    .comment("是否转移拔刀剑的锻造数。")
                    .define("transferRefine", false);
            ModularExchangeAdditionalNbtKeys = BUILDER
                    .comment(
                            "要转移的顶层 NBT 键。",
                            "交换物品的 TransferSettings NBT 可以覆盖或扩展此列表。"
                    )
                    .define("additionalNbtKeys", new ArrayList<>());
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
