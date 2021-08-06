package com.gpiay.cpm.config;

import com.google.common.collect.Maps;
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

        permissions.put("cpm.command.selectSelf", CONFIGS.perm.selectSelf);
        permissions.put("cpm.command.selectOthers", CONFIGS.perm.selectOthers);
        permissions.put("cpm.command.scaleSelf", CONFIGS.perm.scaleSelf);
        permissions.put("cpm.command.scaleOthers", CONFIGS.perm.scaleOthers);
        permissions.put("cpm.command.refresh", CONFIGS.perm.refresh);
        permissions.put("cpm.command.createItem", CONFIGS.perm.createItem);
    }

    public static boolean checkCommandPermission(ServerPlayerEntity player, String node) {
        return permissions.containsKey(node) ? player.hasPermissions(permissions.get(node)) : false;
    }

    public static boolean hideNearParticles() { return CONFIGS.client.hideNearParticles; }
    public static boolean hideArmors() { return CONFIGS.client.hideArmors; }
    public static boolean sendModels() { return CONFIGS.client.sendModels; }

    public static boolean customEyeHeight() { return CONFIGS.server.customEyeHeight; }
    public static boolean customBoundingBox() { return CONFIGS.server.customBoundingBox; }
    public static boolean customEyePosition() { return CONFIGS.server.customEyePosition; }
    public static boolean receiveModels() { return CONFIGS.server.receiveModels; }

    public static boolean serverCustomEyeHeight() { return serverConfigs.customEyeHeight; }
    public static boolean serverCustomBoundingBox() { return serverConfigs.customBoundingBox; }
    public static boolean serverCustomEyePosition() { return serverConfigs.customEyePosition; }
    public static boolean serverReceiveModels() { return serverConfigs.receiveModels; }
}
