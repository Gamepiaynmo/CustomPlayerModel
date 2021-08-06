package com.gpiay.cpm.client.render;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

public class CPMElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {
    ElytraLayer<T, M> orinLayer;

    public CPMElytraLayer(IEntityRenderer<T, M> rendererIn, ElytraLayer<T, M> orinLayer) {
        super(rendererIn);
        this.orinLayer = orinLayer;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientCPMAttachment attachment = (ClientCPMAttachment) AttachmentProvider.getEntityAttachment(entityIn).orElse(null);
        ModelInstance model = attachment != null ? attachment.getModel() : null;
        if (model == null || !model.isReady()) {
            orinLayer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } else {
            ItemStack itemstack = entityIn.getItemBySlot(EquipmentSlotType.CHEST);
            if (itemstack.getItem() == Items.ELYTRA) {
                ResourceLocation resourcelocation;
                if (entityIn instanceof AbstractClientPlayerEntity) {
                    AbstractClientPlayerEntity abstractclientplayerentity = (AbstractClientPlayerEntity)entityIn;
                    if (abstractclientplayerentity.isElytraLoaded() && abstractclientplayerentity.getElytraTextureLocation() != null) {
                        resourcelocation = abstractclientplayerentity.getElytraTextureLocation();
                    } else if (abstractclientplayerentity.isCapeLoaded() && abstractclientplayerentity.getCloakTextureLocation() != null && abstractclientplayerentity.isModelPartShown(PlayerModelPart.CAPE)) {
                        resourcelocation = abstractclientplayerentity.getCloakTextureLocation();
                    } else {
                        resourcelocation = getElytraTexture(itemstack, entityIn);
                    }
                } else {
                    resourcelocation = getElytraTexture(itemstack, entityIn);
                }

                matrixStackIn.pushPose();
//                matrixStackIn.translate(0.0D, 0.0D, 0.125D);
                this.getParentModel().copyPropertiesTo(orinLayer.elytraModel);
                orinLayer.elytraModel.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                IVertexBuilder ivertexbuilder = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(resourcelocation), false, itemstack.hasFoil());

                model.setupBoneTransform(matrixStackIn, model.getInvModelViewMatrix());
                for (ModelBone.Instance bone : model.getAttachments(EnumAttachment.ELYTRA)) {
                    if (bone.isVisible()) {
                        matrixStackIn.pushPose();
                        model.setupBoneTransform(matrixStackIn, bone);
                        orinLayer.elytraModel.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                        matrixStackIn.popPose();
                    }
                }

                matrixStackIn.popPose();
            }
        }
    }

#if FABRIC
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        return WINGS_LOCATION;
    }
#endif
}
