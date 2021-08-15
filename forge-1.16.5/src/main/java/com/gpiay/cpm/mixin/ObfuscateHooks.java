package com.gpiay.cpm.mixin;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrcrayfish.obfuscate.client.Hooks;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Hooks.class)
public class ObfuscateHooks {
    private static LivingEntity currentEntity;

    @Inject(
            method = "Lcom/mrcrayfish/obfuscate/client/Hooks;fireRenderPlayer(Lnet/minecraft/client/renderer/entity/model/EntityModel;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFFLnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void hookRenderPlayer(EntityModel model, MatrixStack matrixStack, IVertexBuilder builder, int light, int overlay, float red, float green, float blue, float alpha, LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        currentEntity = entity;
    }

    @Redirect(
            method = "Lcom/mrcrayfish/obfuscate/client/Hooks;fireRenderPlayer(Lnet/minecraft/client/renderer/entity/model/EntityModel;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFFLnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFF)V",
                    ordinal = 1
            )
    )
    private static void renderToBuffer(EntityModel model, MatrixStack matrixStack, IVertexBuilder builder, int light, int overlay, float red, float green, float blue, float alpha) {
        ClientCPMAttachment attachment = (ClientCPMAttachment) AttachmentProvider.getEntityAttachment(currentEntity).orElse(null);
        ModelInstance modelInstance = attachment != null ? attachment.getModel() : null;
        if (modelInstance == null || !modelInstance.isReady())
            model.renderToBuffer(matrixStack, builder, light, overlay, red, green, blue, alpha);
    }
}
