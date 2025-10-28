package net.yiran.sbtetra;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Config {

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<List<String>> CantWrapperItems;

    static {
        CantWrapperItems = BUILDER
                .define("cantWrapperItems", new ArrayList<>());
        SPEC = BUILDER.build();
    }
}