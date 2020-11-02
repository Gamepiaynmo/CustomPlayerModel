package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import org.lwjgl.opengl.GL11;

public class CPMHeldItemLayer<T extends LivingEntity, M extends EntityModel<T> & IHasArm> extends HeldItemLayer<T, M> {
    HeldItemLayer<T, M> oldLayer;
    public CPMHeldItemLayer(IEntityRenderer<T, M> entityRendererIn, HeldItemLayer<T, M> oldLayer) {
        super(entityRendererIn);
        this.oldLayer = oldLayer;
    }

    @Override
    public void render(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            oldLayer.render(entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        } else {
            boolean flag = entityIn.getPrimaryHand() == HandSide.RIGHT;
            ItemStack itemLeft = flag ? entityIn.getHeldItemOffhand() : entityIn.getHeldItemMainhand();
            ItemStack itemRight = flag ? entityIn.getHeldItemMainhand() : entityIn.getHeldItemOffhand();
            if (!itemLeft.isEmpty() || !itemRight.isEmpty()) {
                GlStateManager.pushMatrix();

                this.renderHeldItem(model, entityIn, itemRight, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT);
                this.renderHeldItem(model, entityIn, itemLeft, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT);
                GlStateManager.popMatrix();
            }
        }
    }

    private void renderHeldItem(ModelInstance model, LivingEntity entityIn, ItemStack item, ItemCameraTransforms.TransformType transformType, HandSide handSide) {
        if (!item.isEmpty()) {
            GlStateManager.pushMatrix();

            GL11.glMultMatrixd(model.getInvModelViewMatrix().val);
            boolean flag = handSide == HandSide.LEFT;
            for (ModelBone bone : model.getAttachments(flag ? EnumAttachment.ITEM_LEFT : EnumAttachment.ITEM_RIGHT)) {
                if (bone.isVisible()) {
                    GlStateManager.pushMatrix();
                    GL11.glMultMatrixd(model.getBoneMatrix(bone).val);

                    GlStateManager.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                    Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(entityIn, item, transformType, flag);
                    GlStateManager.popMatrix();
                }
            }
            GlStateManager.popMatrix();
        }
    }
}
