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
        public static final ForgeConfigSpec.ConfigValue<Double> SlashEffectMinLuminance;
        public static final ForgeConfigSpec.ConfigValue<Double> SlashEffectAlphaMultiplier;
        public static final ForgeConfigSpec.ConfigValue<Integer> SummonedSwordAlpha;
        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        static {
            EnableRenderWrapper = BUILDER
                    .define("EnableRenderWrapper", true);
            BUILDER.push("SlashEffectRender");
            SlashEffectMinLuminance = BUILDER
                    .comment(
                            "刀光亮度分流阈值 (0.0~1.0)。",
                            "感知亮度低于此值的颜色（如黑色/深色）使用标准混合渲染原色，保证可见；",
                            "高于此值的颜色使用加法辉光渲染（发光）。",
                            "0.0 表示所有颜色都走加法辉光（纯原版行为，黑色不可见）。"
                    )
                    .defineInRange("slashEffectMinLuminance", 0.25, 0.0, 1.0);
            SlashEffectAlphaMultiplier = BUILDER
                    .comment("刀光透明度系数 (0.0~1.0)。")
                    .defineInRange("slashEffectAlphaMultiplier", 1.0, 0.0, 1.0);
            SummonedSwordAlpha = BUILDER
                    .comment("幻影剑透明度 (0~255)，255 为原版不透明。")
                    .defineInRange("summonedSwordAlpha", 255, 0, 255);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }

}
