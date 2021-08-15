package com.gpiay.cpm.mixin;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Matrix4d;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingRenderer.class)
public abstract class LivingRendererHook extends EntityRenderer<LivingEntity> implements IEntityRenderer<LivingEntity, EntityModel<LivingEntity>> {
    protected LivingRendererHook(EntityRendererManager entityRendererManager) {
        super(entityRendererManager);
    }

    @Inject(
            at = @At("HEAD"),
            method = "Lnet/minecraft/client/renderer/entity/LivingRenderer;render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V"
    )
    public void render(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int i, CallbackInfo ci) {
        AttachmentProvider.getEntityAttachment(livingEntity).ifPresent(capability -> {
            if (((ClientCPMAttachment) capability).getModel() != null) {
                ModelInstance.baseModelView = new Matrix4d(matrixStack.last().pose()).inv();
            }
        });
    }

    @Redirect(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;IIFFFF)V"
            )
    )
    public void renderToBuffer(EntityModel entityModel, MatrixStack matrixStack, IVertexBuilder vertexBuilder, int light, int lightCoord, float colorR, float colorG, float colorB, float colorA,
            LivingEntity entity, float yaw, float partial, MatrixStack unused1, IRenderTypeBuffer renderType, int unused2) {
        ClientCPMAttachment attachment = (ClientCPMAttachment) AttachmentProvider.getEntityAttachment(entity).orElse(null);
        ModelInstance model = attachment != null ? attachment.getModel() : null;
        if (model == null || !model.isReady())
                entityModel.renderToBuffer(matrixStack, vertexBuilder, light, lightCoord, colorR, colorG, colorB, colorA);
    }
}
