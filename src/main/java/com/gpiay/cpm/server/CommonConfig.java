package com.gpiay.cpm.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.*;

public class CommonConfig {
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> APPLY_ENTITIES;

    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> SELECT_SELF;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> SELECT_OTHERS;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> SCALE_SELF;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> SCALE_OTHERS;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> REFRESH;

    public static class ModelPermission {
        public String node;
        public List<String> models;
    }

    public static ForgeConfigSpec.ConfigValue<List<? extends ModelPermission>> MODEL_PERMISSIONS;

    private static final List<String> defaultEntities = Lists.newArrayList("minecraft:player",
            "minecraft:bat",
            "minecraft:blaze",
            "minecraft:cat",
            "minecraft:cave_spider",
            "minecraft:chicken",
            "minecraft:cod",
            "minecraft:cow",
            "minecraft:creeper",
            "minecraft:dolphin",
            "minecraft:donkey",
            "minecraft:drowned",
            "minecraft:elder_guardian",
            "minecraft:ender_dragon",
            "minecraft:enderman",
            "minecraft:endermite",
            "minecraft:evoker",
            "minecraft:fox",
            "minecraft:ghast",
            "minecraft:giant",
            "minecraft:guardian",
            "minecraft:horse",
            "minecraft:husk",
            "minecraft:illusioner",
            "minecraft:iron_golem",
            "minecraft:llama",
            "minecraft:magma_cube",
            "minecraft:mooshroom",
            "minecraft:mule",
            "minecraft:ocelot",
            "minecraft:panda",
            "minecraft:parrot",
            "minecraft:phantom",
            "minecraft:pig",
            "minecraft:pillager",
            "minecraft:polar_bear",
            "minecraft:pufferfish",
            "minecraft:rabbit",
            "minecraft:ravager",
            "minecraft:salmon",
            "minecraft:sheep",
            "minecraft:shulker",
            "minecraft:silverfish",
            "minecraft:skeleton",
            "minecraft:skeleton_horse",
            "minecraft:slime",
            "minecraft:snow_golem",
            "minecraft:spider",
            "minecraft:squid",
            "minecraft:stray",
            "minecraft:trader_llama",
            "minecraft:tropical_fish",
            "minecraft:turtle",
            "minecraft:vex",
            "minecraft:villager",
            "minecraft:vindicator",
            "minecraft:wandering_trader",
            "minecraft:witch",
            "minecraft:wither",
            "minecraft:wither_skeleton",
            "minecraft:wolf",
            "minecraft:zombie",
            "minecraft:zombie_horse",
            "minecraft:zombie_pigman",
            "minecraft:zombie_villager"
    );

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Common Config").push("permissions");

        APPLY_ENTITIES = builder
                .translation("config.cpm.server.applyEntities")
                .comment("Only apply custom models on these entities.")
                .worldRestart()
                .defineList("applyEntities", defaultEntities, entity ->
                        ForgeRegistries.ENTITIES.containsKey(new ResourceLocation((String) entity)));

        SELECT_SELF = builder
                .translation("config.cpm.permission.selectSelf")
                .comment("Permission for selecting Own Model.")
                .defineEnum("selectSelf", DefaultPermissionLevel.ALL);

        SELECT_OTHERS = builder
                .translation("config.cpm.permission.selectOthers")
                .comment("Permission for selecting Other's Model.")
                .defineEnum("selectOthers", DefaultPermissionLevel.OP);

        SCALE_SELF = builder
                .translation("config.cpm.permission.scaleSelf")
                .comment("Permission for resizing Own Model.")
                .defineEnum("scaleSelf", DefaultPermissionLevel.ALL);

        SCALE_OTHERS = builder
                .translation("config.cpm.permission.scaleOthers")
                .comment("Permission for resizing Other's Model.")
                .defineEnum("scaleOthers", DefaultPermissionLevel.OP);

        REFRESH = builder
                .translation("config.cpm.permission.refreshModels")
                .comment("Permission for refreshing Local Model Files.")
                .defineEnum("refreshModels", DefaultPermissionLevel.OP);

        MODEL_PERMISSIONS = builder
                .translation("config.cpm.permission.models")
                .comment("Permission nodes of models.")
                .defineList("models", Collections.emptyList(), node -> true);

        builder.pop();
        CONFIG = builder.build();
    }
}
