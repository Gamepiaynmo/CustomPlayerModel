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
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CPMElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {
    ElytraLayer<T, M> orinLayer;

    public CPMElytraLayer(IEntityRenderer<T, M> rendererIn, ElytraLayer<T, M> orinLayer) {
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
            ItemStack itemstack = entityIn.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (shouldRender(itemstack, entityIn)) {
                ResourceLocation resourcelocation;
                if (entityIn instanceof AbstractClientPlayerEntity) {
                    AbstractClientPlayerEntity abstractclientplayerentity = (AbstractClientPlayerEntity)entityIn;
                    if (abstractclientplayerentity.isPlayerInfoSet() && abstractclientplayerentity.getLocationElytra() != null) {
                        resourcelocation = abstractclientplayerentity.getLocationElytra();
                    } else if (abstractclientplayerentity.hasPlayerInfo() && abstractclientplayerentity.getLocationCape() != null && abstractclientplayerentity.isWearing(PlayerModelPart.CAPE)) {
                        resourcelocation = abstractclientplayerentity.getLocationCape();
                    } else {
                        resourcelocation = getElytraTexture(itemstack, entityIn);
                    }
                } else {
                    resourcelocation = getElytraTexture(itemstack, entityIn);
                }

                matrixStackIn.push();
                matrixStackIn.translate(0.0D, 0.0D, 0.125D);
                this.getEntityModel().copyModelAttributesTo(orinLayer.modelElytra);
                orinLayer.modelElytra.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                IVertexBuilder ivertexbuilder = ItemRenderer.getArmorVertexBuilder(bufferIn, RenderType.getArmorCutoutNoCull(resourcelocation), false, itemstack.hasEffect());

                model.setupBoneTransform(matrixStackIn, model.getInvModelViewMatrix());
                for (ModelBone.Instance bone : model.getAttachments(EnumAttachment.ELYTRA)) {
                    if (bone.isVisible()) {
                        matrixStackIn.push();
                        model.setupBoneTransform(matrixStackIn, bone);
                        orinLayer.modelElytra.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                        matrixStackIn.pop();
                    }
                }

                matrixStackIn.pop();
            }
        }
    }
}
