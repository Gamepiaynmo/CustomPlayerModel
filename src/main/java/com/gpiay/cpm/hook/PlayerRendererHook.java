package com.gpiay.cpm.hook;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;

public class PlayerRendererHook {
    public static boolean renderFirstPerson(PlayerRenderer playerRenderer, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
            int combinedLightIn, AbstractClientPlayerEntity playerIn, ModelRenderer rendererArmIn) {
        HandSide hand = playerRenderer.getEntityModel().bipedLeftArm == rendererArmIn ? HandSide.LEFT : HandSide.RIGHT;
        final boolean[] ret = {false};

        playerIn.getCapability(CPMCapability.CAPABILITY).ifPresent(capability -> {
            ModelInstance model = ((ClientCPMCapability) capability).getModel();
            if (model == null || !model.isReady())
                ret[0] = true;
            else model.renderFirstPerson(matrixStackIn, bufferIn, combinedLightIn, hand);
        });

        return ret[0];
    }
}
