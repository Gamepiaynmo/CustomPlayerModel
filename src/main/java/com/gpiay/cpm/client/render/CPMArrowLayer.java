package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.model.element.ModelBox;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class CPMArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends ArrowLayer<T, M> {
    ArrowLayer<T, M> oldLayer;

    public CPMArrowLayer(LivingRenderer<T, M> rendererIn, ArrowLayer<T, M> oldLayer) {
        super(rendererIn);
        this.oldLayer = oldLayer;
    }

    @Override
    public void render(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            oldLayer.render(entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        } else {
            int i = entityIn.getArrowCountInEntity();
            if (i > 0) {
                Entity entity = new ArrowEntity(entityIn.world, entityIn.posX, entityIn.posY, entityIn.posZ);
                Random random = new Random(entityIn.getEntityId());
                RenderHelper.disableStandardItemLighting();
                GlStateManager.pushMatrix();
                GL11.glMultMatrixd(model.getInvModelViewMatrix().val);

                for (int j = 0; j < i; ++j) {
                    GlStateManager.pushMatrix();
                    ModelBone bone = model.getRandomBone(random);
                    if (bone != null && bone.boneInfo != null) {
                        ModelBox modelbox = bone.boneInfo.boxes.get(random.nextInt(bone.boneInfo.boxes.size()));
                        GL11.glMultMatrixd(model.getBoneMatrix(bone).val);
                        float scaleX = random.nextFloat();
                        float scaleY = random.nextFloat();
                        float scaleZ = random.nextFloat();
                        float posX = MathHelper.lerp(scaleX, modelbox.posX1, modelbox.posX2) / 16.0F;
                        float posY = MathHelper.lerp(scaleY, modelbox.posY1, modelbox.posY2) / 16.0F;
                        float posZ = MathHelper.lerp(scaleZ, modelbox.posZ1, modelbox.posZ2) / 16.0F;
                        GlStateManager.translatef(posX, posY, posZ);
                        scaleX = scaleX * 2.0F - 1.0F;
                        scaleY = scaleY * 2.0F - 1.0F;
                        scaleZ = scaleZ * 2.0F - 1.0F;
                        scaleX = scaleX * -1.0F;
                        scaleY = scaleY * -1.0F;
                        scaleZ = scaleZ * -1.0F;
                        float angle = MathHelper.sqrt(scaleX * scaleX + scaleZ * scaleZ);
                        entity.rotationYaw = (float) (Math.atan2((double) scaleX, (double) scaleZ) * (double) (180F / (float) Math.PI));
                        entity.rotationPitch = (float) (Math.atan2((double) scaleY, (double) angle) * (double) (180F / (float) Math.PI));
                        entity.prevRotationYaw = entity.rotationYaw;
                        entity.prevRotationPitch = entity.rotationPitch;
                        Minecraft.getInstance().getRenderManager().renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
                        GlStateManager.popMatrix();
                    }
                }

                GlStateManager.popMatrix();
                RenderHelper.enableStandardItemLighting();
            }
        }
    }
}
