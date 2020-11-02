package com.gpiay.cpm.hook;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.HandSide;

public class PlayerRendererHook {
    public static boolean renderLeftArm(AbstractClientPlayerEntity playerEntity) {
        return renderFirstPerson(playerEntity, HandSide.LEFT);
    }

    public static boolean renderRightArm(AbstractClientPlayerEntity playerEntity) {
        return renderFirstPerson(playerEntity, HandSide.RIGHT);
    }

    private static boolean renderFirstPerson(AbstractClientPlayerEntity playerEntity, HandSide hand) {
        ClientCPMCapability capability = (ClientCPMCapability) playerEntity.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady())
            return true;

        GlStateManager.color3f(1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        model.renderFirstPerson(hand);
        GlStateManager.disableBlend();
        return false;
    }
}
