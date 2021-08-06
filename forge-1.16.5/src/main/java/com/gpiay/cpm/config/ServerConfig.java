package com.gpiay.cpm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.BooleanValue CUSTOM_EYE_HEIGHT;
    public static ForgeConfigSpec.BooleanValue CUSTOM_BOUNDING_BOX;
    public static ForgeConfigSpec.BooleanValue CUSTOM_EYE_POSITION;
    public static ForgeConfigSpec.BooleanValue RECEIVE_MODELS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Server Config").push("server");

        CUSTOM_EYE_HEIGHT = builder
                .translation("text.autoconfig.cpm.option.server.customEyeHeight")
                .comment("Allow models to change eye height. Will not take effect if server side is not modded or not allowed.")
                .define("customEyeHeight", true);

        CUSTOM_BOUNDING_BOX = builder
                .translation("text.autoconfig.cpm.option.server.customBoundingBox")
                .comment("Allow models to change bounding box. Will not take effect if server side is not modded or not allowed.")
                .define("customBoundingBox", true);

        CUSTOM_EYE_POSITION = builder
                .translation("text.autoconfig.cpm.option.server.customEyePosition")
                .comment("Allow models to bind eye position to a specified bone. Will not take effect if server side is not modded or not allowed.")
                .define("customEyePosition", true);

        RECEIVE_MODELS = builder
                .translation("text.autoconfig.cpm.option.server.receiveModels")
                .comment("Receive models that are not located at the server from clients.")
                .define("receiveModels", true);

        builder.pop();
        CONFIG = builder.build();
    }
}
