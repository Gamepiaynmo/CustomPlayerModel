package com.gpiay.cpm.config;

import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;

public class CPMConfig {
    public static Configs CONFIGS;
    public static final Configs.Server serverConfigs = new Configs.Server();

    private static final Map<String, Integer> permissions = Maps.newHashMap();

    public static void registerConfig() {
        AutoConfig.register(Configs.class, Toml4jConfigSerializer::new);
        CONFIGS = AutoConfig.getConfigHolder(Configs.class).get();

        serverConfigs.customEyeHeight = CONFIGS.server.customEyeHeight;
        serverConfigs.customBoundingBox = CONFIGS.server.customBoundingBox;
        serverConfigs.customEyePosition = CONFIGS.server.customEyePosition;
        serverConfigs.receiveModels = CONFIGS.server.receiveModels;

        permissions.put("cpm.command.selectSelf", CONFIGS.perm.selectSelf);
        permissions.put("cpm.command.selectOthers", CONFIGS.perm.selectOthers);
        permissions.put("cpm.command.scaleSelf", CONFIGS.perm.scaleSelf);
        permissions.put("cpm.command.scaleOthers", CONFIGS.perm.scaleOthers);
        permissions.put("cpm.command.refresh", CONFIGS.perm.refresh);
        permissions.put("cpm.command.createItem", CONFIGS.perm.createItem);
    }

    public static boolean checkCommandPermission(ServerPlayerEntity player, String node) {
        return permissions.containsKey(node) && player.hasPermissions(permissions.get(node));
    }

    public static boolean hideNearParticles() { return CONFIGS.client.hideNearParticles; }
    public static boolean hideArmors() { return CONFIGS.client.hideArmors; }
    public static boolean sendModels() { return CONFIGS.client.sendModels; }

    public static boolean customEyeHeight() { return CPMMod.cpmClient.isServerModded && CONFIGS.server.customEyeHeight && serverConfigs.customEyeHeight; }
    public static boolean customBoundingBox() { return CPMMod.cpmClient.isServerModded && CONFIGS.server.customBoundingBox && serverConfigs.customBoundingBox; }
    public static boolean customEyePosition() { return CPMMod.cpmClient.isServerModded && CONFIGS.server.customEyePosition && serverConfigs.customEyePosition; }
    public static boolean receiveModels() { return CPMMod.cpmClient.isServerModded && CONFIGS.server.receiveModels && serverConfigs.receiveModels; }
}
