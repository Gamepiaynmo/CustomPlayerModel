package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class CPMCapeLayer extends CapeLayer {
    CapeLayer oldLayer;
    public <E extends LivingEntity> CPMCapeLayer(LivingRenderer renderer, CapeLayer oldLayer) {
        super(renderer);
        this.oldLayer = oldLayer;
    }

    @Override
    public void render(AbstractClientPlayerEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            oldLayer.render(entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        } else {
            if (entityIn.hasPlayerInfo() && !entityIn.isInvisible() && entityIn.isWearing(PlayerModelPart.CAPE) && entityIn.getLocationCape() != null) {
                ItemStack itemstack = entityIn.getItemStackFromSlot(EquipmentSlotType.CHEST);
                if (itemstack.getItem() != Items.ELYTRA) {
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.bindTexture(entityIn.getLocationCape());
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(0.0F, 0.0F, 0.125F);
                    double d0 = MathHelper.lerp(partialTicks, entityIn.prevChasingPosX, entityIn.chasingPosX) - MathHelper.lerp(partialTicks, entityIn.prevPosX, entityIn.posX);
                    double d1 = MathHelper.lerp(partialTicks, entityIn.prevChasingPosY, entityIn.chasingPosY) - MathHelper.lerp(partialTicks, entityIn.prevPosY, entityIn.posY);
                    double d2 = MathHelper.lerp(partialTicks, entityIn.prevChasingPosZ, entityIn.chasingPosZ) - MathHelper.lerp(partialTicks, entityIn.prevPosZ, entityIn.posZ);
                    float f = entityIn.prevRenderYawOffset + (entityIn.renderYawOffset - entityIn.prevRenderYawOffset);
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

                    float f4 = MathHelper.lerp(partialTicks, entityIn.prevCameraYaw, entityIn.cameraYaw);
                    f1 = f1 + MathHelper.sin(MathHelper.lerp(partialTicks, entityIn.prevDistanceWalkedModified, entityIn.distanceWalkedModified) * 6.0F) * 32.0F * f4;

                    GL11.glMultMatrixd(model.getInvModelViewMatrix().val);
                    for (ModelBone bone : model.getAttachments(EnumAttachment.CAPE)) {
                        GlStateManager.pushMatrix();

                        GL11.glMultMatrixd(model.getBoneMatrix(bone).val);
                        GlStateManager.rotatef(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotatef(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
                        GlStateManager.rotatef(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                        this.getEntityModel().renderCape(0.0625F);
                        GlStateManager.popMatrix();
                    }

                    GlStateManager.popMatrix();
                }
            }
        }
    }
}
