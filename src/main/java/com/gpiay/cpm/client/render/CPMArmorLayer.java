package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.client.ClientConfig;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.model.element.VanillaBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class CPMArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {
    BipedArmorLayer<T, M, A> oldLayer;
    public CPMArmorLayer(IEntityRenderer<T, M> renderer, BipedArmorLayer<T, M, A> oldLayer) {
        super(renderer, oldLayer.func_215337_a(EquipmentSlotType.LEGS), oldLayer.func_215337_a(EquipmentSlotType.CHEST));
        this.oldLayer = oldLayer;
    }

    @Override
    public void render(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            oldLayer.render(entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        } else if (!ClientConfig.HIDE_ARMORS.get()) {
            GlStateManager.pushMatrix();
            GL11.glMultMatrixd(model.getInvModelViewMatrix().val);
            this.renderArmorLayer(model, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EquipmentSlotType.CHEST);
            this.renderArmorLayer(model, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EquipmentSlotType.LEGS);
            this.renderArmorLayer(model, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EquipmentSlotType.FEET);
            this.renderArmorLayer(model, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EquipmentSlotType.HEAD);
            GlStateManager.popMatrix();
        }
    }

    private void renderArmorLayer(ModelInstance model, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EquipmentSlotType slotIn) {
        ItemStack itemstack = entityIn.getItemStackFromSlot(slotIn);
        if (itemstack.getItem() instanceof ArmorItem) {
            ArmorItem armoritem = (ArmorItem)itemstack.getItem();
            if (armoritem.getEquipmentSlot() == slotIn) {
                A armorModel = this.func_215337_a(slotIn);
                armorModel = getArmorModelHook(entityIn, itemstack, slotIn, armorModel);
                this.getEntityModel().func_217148_a(armorModel);
                armorModel.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
                this.setModelSlotVisible(armorModel, slotIn);
                this.bindTexture(this.getArmorResource(entityIn, itemstack, slotIn, null));
                if (armoritem instanceof net.minecraft.item.IDyeableArmorItem) { // Allow this for anything, not only cloth
                    int i = ((net.minecraft.item.IDyeableArmorItem)armoritem).getColor(itemstack);
                    float colorR = (i >> 16 & 255) / 255.0F;
                    float colorG = (i >> 8 & 255) / 255.0F;
                    float colorB = (i & 255) / 255.0F;
                    GlStateManager.color4f(colorR, colorG, colorB, 1.0f);
                    renderModel(armorModel, slotIn, model, scale);
                    this.bindTexture(this.getArmorResource(entityIn, itemstack, slotIn, "overlay"));
                }

                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                renderModel(armorModel, slotIn, model, scale);
                if (itemstack.hasEffect()) {
                    renderGlint(this::bindTexture, entityIn, armorModel, partialTicks, scale, slotIn, model);
                }

            }
        }
    }

    public void renderGlint(Consumer<ResourceLocation> binder, T entityIn, A armorModel, float partialTicks, float scale, EquipmentSlotType slot, ModelInstance model) {
        float f = entityIn.ticksExisted + partialTicks;
        binder.accept(ENCHANTED_ITEM_GLINT_RES);
        GameRenderer gamerenderer = Minecraft.getInstance().gameRenderer;
        gamerenderer.setupFogColor(true);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(514);
        GlStateManager.depthMask(false);
        GlStateManager.color4f(0.5F, 0.5F, 0.5F, 1.0F);

        for(int i = 0; i < 2; ++i) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.color4f(0.38F, 0.19F, 0.608F, 1.0F);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.scalef(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotatef(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translatef(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(5888);
            renderModel(armorModel, slot, model, scale);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.disableBlend();
        gamerenderer.setupFogColor(false);
    }

    private void renderModel(BipedModel<T> armorModel, EquipmentSlotType slot, ModelInstance model, float scale) {
        switch (slot) {
            case HEAD:
                renderCuboid(model, EnumAttachment.HELMET, armorModel.bipedHead, scale);
                renderCuboid(model, EnumAttachment.HELMET, armorModel.bipedHeadwear, scale);
                break;
            case CHEST:
                renderCuboid(model, EnumAttachment.CHESTPLATE_BODY, armorModel.bipedBody, scale);
                renderCuboid(model, EnumAttachment.CHESTPLATE_LEFT, armorModel.bipedLeftArm, scale);
                renderCuboid(model, EnumAttachment.CHESTPLATE_RIGHT, armorModel.bipedRightArm, scale);
                break;
            case LEGS:
                renderCuboid(model, EnumAttachment.LEGGINGS_BODY, armorModel.bipedBody, scale);
                renderCuboid(model, EnumAttachment.LEGGINGS_LEFT, armorModel.bipedLeftLeg, scale);
                renderCuboid(model, EnumAttachment.LEGGINGS_RIGHT, armorModel.bipedRightLeg, scale);
                break;
            case FEET:
                renderCuboid(model, EnumAttachment.BOOTS_LEFT, armorModel.bipedLeftLeg, scale);
                renderCuboid(model, EnumAttachment.BOOTS_RIGHT, armorModel.bipedRightLeg, scale);
                break;
        }
    }

    private void renderCuboid(ModelInstance model, EnumAttachment feature, RendererModel cuboid, float scale) {
        for (ModelBone bone : model.getAttachments(feature)) {
            if (bone.isVisible()) {
                GlStateManager.pushMatrix();
                GL11.glMultMatrixd(model.getBoneMatrix(bone).val);
                GL11.glMultMatrixd(new VanillaBone("", cuboid).getTransform().inv().val);
                cuboid.render(scale);
                GlStateManager.popMatrix();
            }
        }
    }
}
