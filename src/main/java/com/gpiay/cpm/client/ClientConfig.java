package com.gpiay.cpm.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.BooleanValue HIDE_NEAR_PARTICLES;
    public static ForgeConfigSpec.BooleanValue HIDE_ARMORS;
    public static ForgeConfigSpec.BooleanValue SEND_MODELS;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Client Config").push("client");

        HIDE_NEAR_PARTICLES = builder
                .translation("config.cpm.client.hideNearParticles")
                .comment("Hide particles that are too close to player's eyes.")
                .define("hideNearParticles", true);

        HIDE_ARMORS = builder
                .translation("config.cpm.client.hideArmors")
                .comment("Hide armors of custom models.")
                .define("hideArmors", false);

        SEND_MODELS = builder
                .translation("config.cpm.client.sendModels")
                .comment("Send models that are not located at the server from client.")
                .define("sendModels", true);

        builder.pop();
        CONFIG = builder.build();
    }
}
