package com.gpiay.cpm.client.render;

import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.model.element.VanillaBone;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CPMArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {
    BipedArmorLayer<T, M, A> orinLayer;

    public CPMArmorLayer(IEntityRenderer<T, M> p_i50936_1_, BipedArmorLayer<T, M, A> orinLayer) {
        super(p_i50936_1_, null, null);
        this.orinLayer = orinLayer;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientCPMAttachment attachment = (ClientCPMAttachment) AttachmentProvider.getEntityAttachment(entityIn).orElse(null);
        ModelInstance model = attachment != null ? attachment.getModel() : null;
        if (model == null || !model.isReady()) {
            orinLayer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } else if (!CPMConfig.hideArmors()) {
            matrixStackIn.pushPose();
            model.setupBoneTransform(matrixStackIn);
            this.renderArmorLayer(matrixStackIn, bufferIn, entityIn, EquipmentSlotType.CHEST, packedLightIn, model);
            this.renderArmorLayer(matrixStackIn, bufferIn, entityIn, EquipmentSlotType.LEGS, packedLightIn, model);
            this.renderArmorLayer(matrixStackIn, bufferIn, entityIn, EquipmentSlotType.FEET, packedLightIn, model);
            this.renderArmorLayer(matrixStackIn, bufferIn, entityIn, EquipmentSlotType.HEAD, packedLightIn, model);
            matrixStackIn.popPose();
        }
    }

    private void renderArmorLayer(MatrixStack p_241739_1_, IRenderTypeBuffer p_241739_2_, T p_241739_3_, EquipmentSlotType p_241739_4_, int p_241739_5_, ModelInstance model) {
        ItemStack itemstack = p_241739_3_.getItemBySlot(p_241739_4_);
        if (itemstack.getItem() instanceof ArmorItem) {
            ArmorItem armoritem = (ArmorItem)itemstack.getItem();
            A armorModel = orinLayer.getArmorModel(p_241739_4_);
            if (armoritem.getSlot() == p_241739_4_) {
                this.getParentModel().copyPropertiesTo(armorModel);
                this.setPartVisibility(armorModel, p_241739_4_);
                boolean flag1 = itemstack.hasFoil();
                if (armoritem instanceof net.minecraft.item.IDyeableArmorItem) {
                    int i = ((net.minecraft.item.IDyeableArmorItem)armoritem).getColor(itemstack);
                    float f = (float)(i >> 16 & 255) / 255.0F;
                    float f1 = (float)(i >> 8 & 255) / 255.0F;
                    float f2 = (float)(i & 255) / 255.0F;
#if FABRIC
                    this.renderModel(p_241739_1_, p_241739_2_, p_241739_5_, armoritem, flag1, armorModel, f, f1, f2, null, p_241739_4_, model);
                    this.renderModel(p_241739_1_, p_241739_2_, p_241739_5_, armoritem, flag1, armorModel, 1.0F, 1.0F, 1.0F, "overlay", p_241739_4_, model);
#elif FORGE
                    this.renderModel(p_241739_1_, p_241739_2_, p_241739_5_, flag1, armorModel, f, f1, f2, this.getArmorResource(p_241739_3_, itemstack, p_241739_4_, null), p_241739_4_, model);
                    this.renderModel(p_241739_1_, p_241739_2_, p_241739_5_, flag1, armorModel, 1.0F, 1.0F, 1.0F, this.getArmorResource(p_241739_3_, itemstack, p_241739_4_, "overlay"), p_241739_4_, model);
#endif
                } else {
#if FABRIC
                    this.renderModel(p_241739_1_, p_241739_2_, p_241739_5_, armoritem, flag1, armorModel, 1.0F, 1.0F, 1.0F, null, p_241739_4_, model);
#elif FORGE
                    this.renderModel(p_241739_1_, p_241739_2_, p_241739_5_, flag1, armorModel, 1.0F, 1.0F, 1.0F, this.getArmorResource(p_241739_3_, itemstack, p_241739_4_, null), p_241739_4_, model);
#endif
                }

            }
        }
    }

#if FABRIC
    private void renderModel(MatrixStack p_241738_1_, IRenderTypeBuffer p_241738_2_, int p_241738_3_, ArmorItem armorItem, boolean p_241738_5_, A p_241738_6_, float p_241738_8_, float p_241738_9_, float p_241738_10_, String string, EquipmentSlotType slot, ModelInstance model) {
        IVertexBuilder ivertexbuilder = ItemRenderer.getArmorFoilBuffer(p_241738_2_, RenderType.armorCutoutNoCull(this.getArmorLocation(armorItem, usesInnerModel(slot), string)), false, p_241738_5_);
        renderModel(p_241738_6_, slot, model, p_241738_1_, ivertexbuilder, p_241738_3_, OverlayTexture.NO_OVERLAY, p_241738_8_, p_241738_9_, p_241738_10_, 1.0F);
    }
#endif

#if FORGE
    private void renderModel(MatrixStack p_241738_1_, IRenderTypeBuffer p_241738_2_, int p_241738_3_, boolean p_241738_5_, A p_241738_6_, float p_241738_8_, float p_241738_9_, float p_241738_10_, ResourceLocation location, EquipmentSlotType slot, ModelInstance model) {
        IVertexBuilder ivertexbuilder = ItemRenderer.getArmorFoilBuffer(p_241738_2_, RenderType.armorCutoutNoCull(location), false, p_241738_5_);
        renderModel(p_241738_6_, slot, model, p_241738_1_, ivertexbuilder, p_241738_3_, OverlayTexture.NO_OVERLAY, p_241738_8_, p_241738_9_, p_241738_10_, 1.0F);
    }
#endif

    private void renderModel(A armorModel, EquipmentSlotType slot, ModelInstance model, MatrixStack matrixStackIn,
            IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        switch (slot) {
            case HEAD:
                renderCuboid(model, EnumAttachment.HELMET, armorModel.head, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                renderCuboid(model, EnumAttachment.HELMET, armorModel.hat, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                break;
            case CHEST:
                renderCuboid(model, EnumAttachment.CHESTPLATE_BODY, armorModel.body, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                renderCuboid(model, EnumAttachment.CHESTPLATE_LEFT, armorModel.leftArm, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                renderCuboid(model, EnumAttachment.CHESTPLATE_RIGHT, armorModel.rightArm, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                break;
            case LEGS:
                renderCuboid(model, EnumAttachment.LEGGINGS_BODY, armorModel.body, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                renderCuboid(model, EnumAttachment.LEGGINGS_LEFT, armorModel.leftLeg, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                renderCuboid(model, EnumAttachment.LEGGINGS_RIGHT, armorModel.rightLeg, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                break;
            case FEET:
                renderCuboid(model, EnumAttachment.BOOTS_LEFT, armorModel.leftLeg, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                renderCuboid(model, EnumAttachment.BOOTS_RIGHT, armorModel.rightLeg, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                break;
        }
    }

    private void renderCuboid(ModelInstance model, EnumAttachment feature, ModelRenderer cuboid, MatrixStack matrixStackIn,
            IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        for (ModelBone.Instance bone : model.getAttachments(feature)) {
            if (bone.isVisible()) {
                matrixStackIn.pushPose();
                model.setupBoneTransform(matrixStackIn, bone);
                model.setupBoneTransform(matrixStackIn, new VanillaBone("", cuboid).getTransform().inv());
                cuboid.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                matrixStackIn.popPose();
            }
        }
    }
}
