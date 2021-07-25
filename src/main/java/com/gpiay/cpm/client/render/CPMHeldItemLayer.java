package com.gpiay.cpm.client.render;

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

public class CPMHeldItemLayer<T extends LivingEntity, M extends EntityModel<T> & IHasArm> extends HeldItemLayer<T, M> {
    HeldItemLayer<T, M> orinLayer;

    public CPMHeldItemLayer(IEntityRenderer<T, M> p_i50934_1_, HeldItemLayer<T, M> orinLayer) {
        super(p_i50934_1_);
        this.orinLayer = orinLayer;
    }

    @Override
    public void render(MatrixStack p_225628_1_, IRenderTypeBuffer p_225628_2_, int p_225628_3_, T p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        ClientCPMCapability capability = (ClientCPMCapability) p_225628_4_.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            orinLayer.render(p_225628_1_, p_225628_2_, p_225628_3_, p_225628_4_, p_225628_5_, p_225628_6_, p_225628_7_, p_225628_8_, p_225628_9_, p_225628_10_);
        } else {
            boolean lvt_11_1_ = p_225628_4_.getMainArm() == HandSide.RIGHT;
            ItemStack lvt_12_1_ = lvt_11_1_ ? p_225628_4_.getOffhandItem() : p_225628_4_.getMainHandItem();
            ItemStack lvt_13_1_ = lvt_11_1_ ? p_225628_4_.getMainHandItem() : p_225628_4_.getOffhandItem();
            if (!lvt_12_1_.isEmpty() || !lvt_13_1_.isEmpty()) {
                p_225628_1_.pushPose();
                model.setupBoneTransform(p_225628_1_);
                this.renderArmWithItem(p_225628_4_, lvt_13_1_, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, p_225628_1_, p_225628_2_, p_225628_3_, model);
                this.renderArmWithItem(p_225628_4_, lvt_12_1_, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, p_225628_1_, p_225628_2_, p_225628_3_, model);
                p_225628_1_.popPose();
            }
        }
    }

    private void renderArmWithItem(LivingEntity p_229135_1_, ItemStack p_229135_2_, ItemCameraTransforms.TransformType p_229135_3_, HandSide p_229135_4_, MatrixStack p_229135_5_, IRenderTypeBuffer p_229135_6_, int p_229135_7_, ModelInstance model) {
        if (!p_229135_2_.isEmpty()) {
            boolean lvt_8_1_ = p_229135_4_ == HandSide.LEFT;
            for (ModelBone.Instance bone : model.getAttachments(lvt_8_1_ ? EnumAttachment.ITEM_LEFT : EnumAttachment.ITEM_RIGHT)) {
                if (bone.isVisible()) {
                    p_229135_5_.pushPose();
                    model.setupBoneTransform(p_229135_5_, bone);
                    p_229135_5_.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                    p_229135_5_.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                    Minecraft.getInstance().getItemInHandRenderer().renderItem(p_229135_1_, p_229135_2_, p_229135_3_, lvt_8_1_, p_229135_5_, p_229135_6_, p_229135_7_);
                    p_229135_5_.popPose();
                }
            }
        }
    }
}
