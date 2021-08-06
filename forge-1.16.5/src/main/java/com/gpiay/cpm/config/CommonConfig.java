package com.gpiay.cpm.config;

import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.Collections;
import java.util.List;

public class CommonConfig {
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;

    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> SELECT_SELF;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> SELECT_OTHERS;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> SCALE_SELF;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> SCALE_OTHERS;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> REFRESH;
    public static ForgeConfigSpec.EnumValue<DefaultPermissionLevel> CREATE_ITEM;

    public static class ModelPermission {
        public String node;
        public List<String> models;
    }

    public static ForgeConfigSpec.ConfigValue<List<? extends ModelPermission>> MODEL_PERMISSIONS;

    private static final List<String> defaultBlacklist = Lists.newArrayList(
            "minecraft:armor_stand"
    );

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Common Config").push("common");

        BLACKLIST = builder
                .translation("config.cpm.server.blacklist")
                .comment("Do not apply custom models on these entities.")
                .worldRestart()
                .defineList("blacklist", defaultBlacklist, entity ->
                        ForgeRegistries.ENTITIES.containsKey(new ResourceLocation((String) entity)));

        builder.pop();
        builder.comment("Permissions").push("permissions");

        SELECT_SELF = builder
                .translation("config.cpm.permission.selectSelf")
                .comment("Permission for selecting own model.")
                .defineEnum("selectSelf", DefaultPermissionLevel.ALL);

        SELECT_OTHERS = builder
                .translation("config.cpm.permission.selectOthers")
                .comment("Permission for selecting other's model.")
                .defineEnum("selectOthers", DefaultPermissionLevel.OP);

        SCALE_SELF = builder
                .translation("config.cpm.permission.scaleSelf")
                .comment("Permission for resizing own model.")
                .defineEnum("scaleSelf", DefaultPermissionLevel.ALL);

        SCALE_OTHERS = builder
                .translation("config.cpm.permission.scaleOthers")
                .comment("Permission for resizing other's model.")
                .defineEnum("scaleOthers", DefaultPermissionLevel.OP);

        REFRESH = builder
                .translation("config.cpm.permission.refreshModels")
                .comment("Permission for refreshing local model files.")
                .defineEnum("refreshModels", DefaultPermissionLevel.OP);

        CREATE_ITEM = builder
                .translation("config.cpm.permission.createItem")
                .comment("Permission for creating model changing items.")
                .defineEnum("createItems", DefaultPermissionLevel.OP);

        MODEL_PERMISSIONS = builder
                .translation("config.cpm.permission.models")
                .comment("Permission nodes of models.")
                .defineList("models", Collections.emptyList(), node -> true);

        builder.pop();
        CONFIG = builder.build();
    }
}
