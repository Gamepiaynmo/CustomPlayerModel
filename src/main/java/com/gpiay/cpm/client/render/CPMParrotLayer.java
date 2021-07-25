package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.ParrotVariantLayer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class CPMParrotLayer<T extends PlayerEntity> extends ParrotVariantLayer<T> {
    ParrotVariantLayer<T> orinLayer;

    public CPMParrotLayer(IEntityRenderer<T, PlayerModel<T>> rendererIn, ParrotVariantLayer<T> orinLayer) {
        super(rendererIn);
        this.orinLayer = orinLayer;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            orinLayer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } else {
            this.renderParrot(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, true, model);
            this.renderParrot(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, false, model);
        }
    }

    private void renderParrot(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulderIn, ModelInstance model) {
        CompoundNBT compoundnbt = leftShoulderIn ? entitylivingbaseIn.getShoulderEntityLeft() : entitylivingbaseIn.getShoulderEntityRight();
        EntityType.byString(compoundnbt.getString("id")).filter((p_215344_0_) -> {
            return p_215344_0_ == EntityType.PARROT;
        }).ifPresent((p_229137_11_) -> {
            matrixStackIn.pushPose();
            model.setupBoneTransform(matrixStackIn, model.getInvModelViewMatrix());
            for (ModelBone.Instance bone : model.getAttachments(leftShoulderIn ? EnumAttachment.PARROT_LEFT : EnumAttachment.PARROT_RIGHT)) {
                if (bone.isVisible()) {
                    matrixStackIn.pushPose();
                    model.setupBoneTransform(matrixStackIn, bone);
                    matrixStackIn.translate(0.0D, entitylivingbaseIn.isCrouching() ? (double) -1.3F : -1.5D, 0.0D);
                    IVertexBuilder ivertexbuilder = bufferIn.getBuffer(orinLayer.model.renderType(ParrotRenderer.PARROT_LOCATIONS[compoundnbt.getInt("Variant")]));
                    orinLayer.model.renderOnShoulder(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, limbSwing, limbSwingAmount, netHeadYaw, headPitch, entitylivingbaseIn.tickCount);
                    matrixStackIn.popPose();
                }
            }

            matrixStackIn.popPose();
        });
    }
}
