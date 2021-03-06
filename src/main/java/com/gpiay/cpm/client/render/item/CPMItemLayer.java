package com.gpiay.cpm.client.render.item;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

public abstract class CPMItemLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    LayerRenderer<T, M> orinLayer;

    public CPMItemLayer(IEntityRenderer<T, M> entityRendererIn, LayerRenderer<T, M> orinLayer) {
        super(entityRendererIn);
        this.orinLayer = orinLayer;
    }

    protected abstract ItemStack getHeldItem(T entityIn);

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            orinLayer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } else {
            HandSide handSide = entityIn.getPrimaryHand();
            ItemStack itemstack = getHeldItem(entityIn);
            if (!itemstack.isEmpty()) {
                matrixStackIn.push();
                model.setupBoneTransform(matrixStackIn, model.getInvModelViewMatrix());
                ItemCameraTransforms.TransformType transform = handSide == HandSide.LEFT ? ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
                        : ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
                this.func_229135_a_(entityIn, itemstack, transform, handSide, matrixStackIn, bufferIn, packedLightIn, model);
                matrixStackIn.pop();
            }
        }
    }

    private void func_229135_a_(LivingEntity p_229135_1_, ItemStack p_229135_2_, ItemCameraTransforms.TransformType p_229135_3_, HandSide p_229135_4_, MatrixStack p_229135_5_, IRenderTypeBuffer p_229135_6_, int p_229135_7_, ModelInstance model) {
        if (!p_229135_2_.isEmpty()) {
            boolean flag = p_229135_4_ == HandSide.LEFT;
            for (ModelBone.Instance bone : model.getAttachments(flag ? EnumAttachment.ITEM_LEFT : EnumAttachment.ITEM_RIGHT)) {
                if (bone.isVisible()) {
                    p_229135_5_.push();
                    model.setupBoneTransform(p_229135_5_, bone);
                    p_229135_5_.rotate(Vector3f.XP.rotationDegrees(-90.0F));
                    p_229135_5_.rotate(Vector3f.YP.rotationDegrees(180.0F));
                    Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(p_229135_1_, p_229135_2_, p_229135_3_, flag, p_229135_5_, p_229135_6_, p_229135_7_);
                    p_229135_5_.pop();
                }
            }
        }
    }
}
