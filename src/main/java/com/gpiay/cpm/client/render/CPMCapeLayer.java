package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class CPMCapeLayer extends CapeLayer {
    CapeLayer orinLayer;

    public CPMCapeLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> playerModelIn, CapeLayer orinLayer) {
        super(playerModelIn);
        this.orinLayer = orinLayer;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            orinLayer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } else {
            if (entityIn.isCapeLoaded() && !entityIn.isInvisible() && entityIn.isModelPartShown(PlayerModelPart.CAPE) && entityIn.getCloakTextureLocation() != null) {
                ItemStack itemstack = entityIn.getItemBySlot(EquipmentSlotType.CHEST);
                if (itemstack.getItem() != Items.ELYTRA) {
                    matrixStackIn.pushPose();
//                    matrixStackIn.translate(0.0D, 0.0D, 0.125D);
                    double d0 = MathHelper.lerp(partialTicks, entityIn.xCloakO, entityIn.xCloak) - MathHelper.lerp(partialTicks, entityIn.xo, entityIn.getX());
                    double d1 = MathHelper.lerp(partialTicks, entityIn.yCloakO, entityIn.yCloak) - MathHelper.lerp(partialTicks, entityIn.yo, entityIn.getY());
                    double d2 = MathHelper.lerp(partialTicks, entityIn.zCloakO, entityIn.zCloak) - MathHelper.lerp(partialTicks, entityIn.zo, entityIn.getZ());
                    float f = entityIn.yBodyRotO + (entityIn.yBodyRot - entityIn.yBodyRotO);
                    double d3 = MathHelper.sin(f * ((float)Math.PI / 180F));
                    double d4 = -MathHelper.cos(f * ((float)Math.PI / 180F));
                    float f1 = (float)d1 * 10.0F;
                    f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
                    float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
                    f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
                    float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
                    f3 = MathHelper.clamp(f3, -20.0F, 20.0F);
                    if (f2 < 0.0F) {
                        f2 = 0.0F;
                    }

                    float f4 = MathHelper.lerp(partialTicks, entityIn.oBob, entityIn.bob);
                    f1 = f1 + MathHelper.sin(MathHelper.lerp(partialTicks, entityIn.walkDistO, entityIn.walkDist) * 6.0F) * 32.0F * f4;

                    model.setupBoneTransform(matrixStackIn, model.getInvModelViewMatrix());
                    for (ModelBone.Instance bone : model.getAttachments(EnumAttachment.CAPE)) {
                        if (bone.isVisible()) {
                            matrixStackIn.pushPose();
                            model.setupBoneTransform(matrixStackIn, bone);
                            matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
                            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(f3 / 2.0F));
                            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F - f3 / 2.0F));
                            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.entitySolid(entityIn.getCloakTextureLocation()));
                            this.getParentModel().renderCloak(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
                            matrixStackIn.popPose();
                        }
                    }
                    matrixStackIn.popPose();
                }
            }
        }
    }
}
