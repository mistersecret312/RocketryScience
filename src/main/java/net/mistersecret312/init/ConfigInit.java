package net.mistersecret312.init;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLConfig;

@Mod.EventBusSubscriber
public class ConfigInit
{
    //private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    //public static final ForgeConfigSpec COMMON_CONFIG;

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.BooleanValue enable_frost_layer;

    static
    {
        CLIENT_BUILDER.push("Rocketry Science Client Config");
        enable_frost_layer =
                CLIENT_BUILDER.comment("If true, there will be a frost layer on the fuel tanks that shows how filled up they are")
                        .define("client.enable_frost_layer", false);

        CLIENT_BUILDER.pop();
        
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

}
