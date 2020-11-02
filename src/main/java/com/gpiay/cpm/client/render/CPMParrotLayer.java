package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.layers.ParrotVariantLayer;
import net.minecraft.client.renderer.entity.model.ParrotModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import org.lwjgl.opengl.GL11;

public class CPMParrotLayer<T extends PlayerEntity> extends ParrotVariantLayer<T> {
    private final ParrotModel parrotModel = new ParrotModel();
    ParrotVariantLayer<T> oldLayer;

    public CPMParrotLayer(LivingRenderer renderer, ParrotVariantLayer<T> oldLayer) {
        super(renderer);
        this.oldLayer = oldLayer;
    }

    @Override
    public void render(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            oldLayer.render(entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        } else {
            GlStateManager.enableRescaleNormal();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawParrot(model, entityIn, limbSwing, limbSwingAmount, partialTicks, netHeadYaw, headPitch, scale, true);
            this.drawParrot(model, entityIn, limbSwing, limbSwingAmount, partialTicks, netHeadYaw, headPitch, scale, false);
            GlStateManager.disableRescaleNormal();
        }
    }

    private void drawParrot(ModelInstance model, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float netHeadYaw, float headPitch, float scale, boolean isLeft) {
        CompoundNBT compoundnbt = isLeft ? entityIn.getLeftShoulderEntity() : entityIn.getRightShoulderEntity();
        EntityType.byKey(compoundnbt.getString("id")).filter(type -> type == EntityType.PARROT).ifPresent(type -> {
            GlStateManager.pushMatrix();
            GL11.glMultMatrixd(model.getInvModelViewMatrix().val);
            this.bindTexture(ParrotRenderer.PARROT_TEXTURES[compoundnbt.getInt("Variant")]);

            for (ModelBone bone : model.getAttachments(isLeft ? EnumAttachment.PARROT_LEFT : EnumAttachment.PARROT_RIGHT)) {
                GlStateManager.pushMatrix();
                GL11.glMultMatrixd(model.getBoneMatrix(bone).val);
                GlStateManager.translatef(0, entityIn.shouldRenderSneaking() ? -1.3F : -1.5F, 0.0F);
                this.parrotModel.renderOnShoulder(limbSwing, limbSwingAmount, netHeadYaw, headPitch, scale, entityIn.ticksExisted);
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
        });
    }
}
