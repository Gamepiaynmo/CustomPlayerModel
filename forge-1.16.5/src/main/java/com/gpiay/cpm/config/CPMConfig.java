package com.gpiay.cpm.config;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.server.permission.PermissionAPI;

public class CPMConfig {
    public static boolean checkCommandPermission(ServerPlayerEntity player, String node) {
        return PermissionAPI.hasPermission(player, node);
    }

    public static boolean hideNearParticles() { return ClientConfig.HIDE_NEAR_PARTICLES.get(); }
    public static boolean hideArmors() { return ClientConfig.HIDE_ARMORS.get(); }
    public static boolean sendModels() { return ClientConfig.SEND_MODELS.get(); }

    public static boolean customEyeHeight() { return ServerConfig.CUSTOM_EYE_HEIGHT.get(); }
    public static boolean customBoundingBox() { return ServerConfig.CUSTOM_BOUNDING_BOX.get(); }
    public static boolean customEyePosition() { return ServerConfig.CUSTOM_EYE_POSITION.get(); }
    public static boolean receiveModels() { return ServerConfig.RECEIVE_MODELS.get(); }
}
