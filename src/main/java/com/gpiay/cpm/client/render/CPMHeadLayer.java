package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class CPMHeadLayer<T extends LivingEntity, M extends EntityModel<T> & IHasHead> extends HeadLayer<T, M> {
    HeadLayer<T, M> oldLayer;

    public CPMHeadLayer(IEntityRenderer<T, M> renderer, HeadLayer<T, M> oldLayer) {
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
            ItemStack itemstack = entityIn.getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                GlStateManager.pushMatrix();
                GL11.glMultMatrixd(model.getInvModelViewMatrix().val);

                for (ModelBone bone : model.getAttachments(EnumAttachment.SKULL)) {
                    GlStateManager.pushMatrix();
                    GL11.glMultMatrixd(model.getBoneMatrix(bone).val);

                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock) {
                        GlStateManager.scalef(1.1875F, -1.1875F, -1.1875F);

                        GameProfile gameprofile = null;
                        if (itemstack.hasTag()) {
                            CompoundNBT compoundnbt = itemstack.getTag();
                            if (compoundnbt.contains("SkullOwner", 10)) {
                                gameprofile = NBTUtil.readGameProfile(compoundnbt.getCompound("SkullOwner"));
                            } else if (compoundnbt.contains("SkullOwner", 8)) {
                                String s = compoundnbt.getString("SkullOwner");
                                if (!StringUtils.isBlank(s)) {
                                    gameprofile = SkullTileEntity.updateGameProfile(new GameProfile((UUID) null, s));
                                    compoundnbt.put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), gameprofile));
                                }
                            }
                        }

                        SkullTileEntityRenderer.instance.render(-0.5F, 0.0F, -0.5F, null, 180.0F, ((AbstractSkullBlock) ((BlockItem) item).getBlock()).getSkullType(), gameprofile, -1, limbSwing);
                    } else if (!(item instanceof ArmorItem) || ((ArmorItem) item).getEquipmentSlot() != EquipmentSlotType.HEAD) {
                        GlStateManager.translatef(0.0F, -0.25F, 0.0F);
                        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.scalef(0.625F, -0.625F, -0.625F);

                        Minecraft.getInstance().getFirstPersonRenderer().renderItem(entityIn, itemstack, ItemCameraTransforms.TransformType.HEAD);
                    }

                    GlStateManager.popMatrix();
                }

                GlStateManager.popMatrix();
            }
        }
    }
}
