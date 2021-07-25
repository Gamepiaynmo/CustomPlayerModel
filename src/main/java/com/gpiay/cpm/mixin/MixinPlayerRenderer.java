package com.gpiay.cpm.mixin;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.HandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRenderer extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
    public MixinPlayerRenderer(EntityRendererManager p_i50965_1_, PlayerModel<AbstractClientPlayerEntity> p_i50965_2_, float p_i50965_3_) {
        super(p_i50965_1_, p_i50965_2_, p_i50965_3_);
    }

    @Inject(
            at = @At("HEAD"),
            method = "renderRightHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V",
            cancellable = true
    )
    public void renderRightHand(MatrixStack matrixStack, IRenderTypeBuffer renderType, int light, AbstractClientPlayerEntity player, CallbackInfo info) {
        player.getCapability(CPMCapability.CAPABILITY).ifPresent(capability -> {
            ModelInstance model = ((ClientCPMCapability) capability).getModel();
            if (model != null && model.isReady()) {
                model.renderFirstPerson(matrixStack, renderType, light, HandSide.RIGHT);
                info.cancel();
            }
        });
    }

    @Inject(
            at = @At("HEAD"),
            method = "renderLeftHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V",
            cancellable = true
    )
    public void renderLeftHand(MatrixStack matrixStack, IRenderTypeBuffer renderType, int light, AbstractClientPlayerEntity player, CallbackInfo info) {
        player.getCapability(CPMCapability.CAPABILITY).ifPresent(capability -> {
            ModelInstance model = ((ClientCPMCapability) capability).getModel();
            if (model != null && model.isReady()) {
                model.renderFirstPerson(matrixStack, renderType, light, HandSide.LEFT);
                info.cancel();
            }
        });
    }
}
