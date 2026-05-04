package net.yiran.sbtetra;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Config {

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<List<String>> CantWrapperItems;
    public static final ForgeConfigSpec.ConfigValue<Integer> MaxEnchantedSoulDrop;
    public static final ForgeConfigSpec.ConfigValue<Integer> EnchantedSoulDropNeeded;
    public static final ForgeConfigSpec.ConfigValue<Integer> MaxSoulDrop;
    public static final ForgeConfigSpec.ConfigValue<Integer> SoulDropNeeded;
    public static final ForgeConfigSpec.ConfigValue<Boolean> EnableCiallo;

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
        SPEC = BUILDER.build();
    }
}