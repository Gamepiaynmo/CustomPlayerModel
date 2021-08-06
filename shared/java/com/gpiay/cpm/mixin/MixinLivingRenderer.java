package com.gpiay.cpm.mixin;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.IMatrixTransformer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingRenderer.class)
public abstract class MixinLivingRenderer extends EntityRenderer<LivingEntity> implements IEntityRenderer<LivingEntity, EntityModel<LivingEntity>>, IMatrixTransformer {
    protected MixinLivingRenderer(EntityRendererManager p_i46179_1_) {
        super(p_i46179_1_);
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
        AttachmentProvider.getEntityAttachment(entity).ifPresent(attachment -> {
            ModelInstance model = ((ClientCPMAttachment) attachment).getModel();
            if (model == null)
                entityModel.renderToBuffer(matrixStack, vertexBuilder, light, lightCoord, colorR, colorG, colorB, colorA);
        });
    }

    @Shadow
    protected abstract float getBob(LivingEntity p_77044_1_, float p_77044_2_);

    @Shadow
    protected abstract void setupRotations(LivingEntity p_225621_1_, MatrixStack p_225621_2_, float p_225621_3_, float p_225621_4_, float p_225621_5_);

    @Shadow
    protected abstract void scale(LivingEntity p_225620_1_, MatrixStack p_225620_2_, float p_225620_3_);

    @Override
    public void transform(LivingEntity entity, MatrixStack matrixStack, float partial) {
#if FORGE
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
#elif FABRIC
        boolean shouldSit = entity.isPassenger();
#endif
        float f = entity.yBodyRot;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity)
            f = ((LivingEntity) entity.getVehicle()).yBodyRot;

        if (entity.getPose() == Pose.SLEEPING) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                matrixStack.translate(-direction.getStepX() * f4, 0.0D, -direction.getStepZ() * f4);
            }
        }

        float f7 = this.getBob(entity, partial);
        this.setupRotations(entity, matrixStack, f7, f, partial);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, matrixStack, partial);
        matrixStack.translate(0.0D, -1.501F, 0.0D);
    }
}
