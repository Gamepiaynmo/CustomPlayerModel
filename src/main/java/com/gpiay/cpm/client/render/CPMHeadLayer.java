package com.gpiay.cpm.client.render;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class CPMHeadLayer<T extends LivingEntity, M extends EntityModel<T> & IHasHead> extends HeadLayer<T, M> {
    HeadLayer<T, M> orinLayer;

    public CPMHeadLayer(IEntityRenderer<T, M> p_i50946_1_, HeadLayer<T, M> orinLayer) {
        super(p_i50946_1_);
        this.orinLayer = orinLayer;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ClientCPMCapability capability = (ClientCPMCapability) entityIn.getCapability(CPMCapability.CAPABILITY).orElse(null);
        ModelInstance model = capability != null ? capability.getModel() : null;
        if (model == null || !model.isReady()) {
            orinLayer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        } else {
            ItemStack itemstack = entityIn.getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                matrixStackIn.push();
                model.setupBoneTransform(matrixStackIn, model.getInvModelViewMatrix());

                for (ModelBone.Instance bone : model.getAttachments(EnumAttachment.SKULL)) {
                    if (bone.isVisible()) {
                        matrixStackIn.push();
                        model.setupBoneTransform(matrixStackIn, bone);

                        matrixStackIn.scale(orinLayer.field_239402_a_, orinLayer.field_239403_b_, orinLayer.field_239404_c_);
                        if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock) {
                            matrixStackIn.scale(1.1875F, -1.1875F, -1.1875F);

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

                            matrixStackIn.translate(-0.5D, 0.0D, -0.5D);
                            SkullTileEntityRenderer.render((Direction) null, 180.0F, ((AbstractSkullBlock) ((BlockItem) item).getBlock()).getSkullType(), gameprofile, limbSwing, matrixStackIn, bufferIn, packedLightIn);
                        } else if (!(item instanceof ArmorItem) || ((ArmorItem) item).getEquipmentSlot() != EquipmentSlotType.HEAD) {
                            float f2 = 0.625F;
                            matrixStackIn.translate(0.0D, -0.25D, 0.0D);
                            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
                            matrixStackIn.scale(0.625F, -0.625F, -0.625F);

                            Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(entityIn, itemstack, ItemCameraTransforms.TransformType.HEAD, false, matrixStackIn, bufferIn, packedLightIn);
                        }

                        matrixStackIn.pop();
                    }
                }

                matrixStackIn.pop();
            }
        }
    }
}
