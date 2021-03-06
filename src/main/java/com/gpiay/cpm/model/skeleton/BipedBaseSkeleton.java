package com.gpiay.cpm.model.skeleton;

import com.google.common.collect.Lists;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.VanillaBone;
import com.gpiay.cpm.model.skeleton.model.CustomBipedModel;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;

import java.util.List;

public class BipedBaseSkeleton<M extends CustomBipedModel> extends Skeleton<M> {
    protected final VanillaBone head, body, leftArm, rightArm, leftLeg, rightLeg;

    public BipedBaseSkeleton(M model) {
        super(model);

        head = registerVanillaBone("head", model.bipedHead);
        body = registerVanillaBone("body", model.bipedBody);
        rightArm = registerVanillaBone("right_arm", model.bipedRightArm);
        leftArm = registerVanillaBone("left_arm", model.bipedLeftArm);
        rightLeg = registerVanillaBone("right_leg", model.bipedRightLeg);
        leftLeg = registerVanillaBone("left_leg", model.bipedLeftLeg);
    }

    public static BipedModel.ArmPose func_241741_a_(LivingEntity p_241741_0_, Hand p_241741_1_) {
        ItemStack itemstack = p_241741_0_.getHeldItem(p_241741_1_);
        if (itemstack.isEmpty()) {
            return BipedModel.ArmPose.EMPTY;
        } else {
            if (p_241741_0_.getActiveHand() == p_241741_1_ && p_241741_0_.getItemInUseCount() > 0) {
                UseAction useaction = itemstack.getUseAction();
                if (useaction == UseAction.BLOCK) {
                    return BipedModel.ArmPose.BLOCK;
                }

                if (useaction == UseAction.BOW) {
                    return BipedModel.ArmPose.BOW_AND_ARROW;
                }

                if (useaction == UseAction.SPEAR) {
                    return BipedModel.ArmPose.THROW_SPEAR;
                }

                if (useaction == UseAction.CROSSBOW && p_241741_1_ == p_241741_0_.getActiveHand()) {
                    return BipedModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!p_241741_0_.isSwingInProgress && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack)) {
                return BipedModel.ArmPose.CROSSBOW_HOLD;
            }

            return BipedModel.ArmPose.ITEM;
        }
    }

    @Override
    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                       float headPitch, float partialTicks, double scale, boolean firstPerson) {
        if (entity.isSpectator()) {
            entityModel.setVisible(false);
            entityModel.bipedHead.showModel = true;
            entityModel.bipedHeadwear.showModel = true;
        } else {
            entityModel.setVisible(true);
            entityModel.isSneak = entity.isCrouching();
            BipedModel.ArmPose bipedmodel$armpose = func_241741_a_(entity, Hand.MAIN_HAND);
            BipedModel.ArmPose bipedmodel$armpose1 = func_241741_a_(entity, Hand.OFF_HAND);
            if (bipedmodel$armpose.func_241657_a_()) {
                bipedmodel$armpose1 = entity.getHeldItemOffhand().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
            }

            if (entity.getPrimaryHand() == HandSide.RIGHT) {
                entityModel.rightArmPose = bipedmodel$armpose;
                entityModel.leftArmPose = bipedmodel$armpose1;
            } else {
                entityModel.rightArmPose = bipedmodel$armpose1;
                entityModel.leftArmPose = bipedmodel$armpose;
            }
        }

        if (firstPerson) {
            entityModel.swingProgress = 0.0f;
            entityModel.isSneak = false;
            entityModel.swimAnimation = 0.0f;
            entityModel.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            entityModel.bipedLeftArm.rotateAngleX = 0.0f;
            entityModel.bipedRightArm.rotateAngleX = 0.0f;
            adjustBones(entity, scale, partialTicks);
        } else {
            super.update(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, scale, firstPerson);
        }
    }

    @Override
    protected void adjustBones(LivingEntity entity, double scale, float partialTicks) {
        super.adjustBones(entity, scale, partialTicks);

        if (entity.isChild()) {
            none.scale(0.5).offset(0, 12, 0);
            head.scale(0.75).offset(0, 6 + 0.25f * entityModel.headPivotHeight * scale, 0);
            body.scale(0.5).offset(0, 12, 0);
            leftArm.scale(0.5).offset(0, 12, 0);
            rightArm.scale(0.5).offset(0, 12, 0);
            leftLeg.scale(0.5).offset(0, 12, 0);
            rightLeg.scale(0.5).offset(0, 12, 0);
        }
    }

    @Override
    public List<String> getFirstPersonBones(HandSide hand) {
        return Lists.newArrayList(hand == HandSide.LEFT ? "left_arm": "right_arm");
    }

    @Override
    public void addAttachments(EnumAttachment attachment, ModelInstance instance) {
        switch (attachment) {
            case HELMET:
            case SKULL:
                addBuiltinAttachment(attachment, instance, "head");
                break;
            case CHESTPLATE_BODY:
            case LEGGINGS_BODY:
            case CAPE:
                addBuiltinAttachment(attachment, instance, "body", new Vector3d(0, 0, 2));
                break;
            case ELYTRA:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(0, entityModel.bodyPivotHeight - 24, 0));
                break;
            case CHESTPLATE_LEFT:
                addBuiltinAttachment(attachment, instance, "left_arm");
                break;
            case CHESTPLATE_RIGHT:
                addBuiltinAttachment(attachment, instance, "right_arm");
                break;
            case LEGGINGS_LEFT:
            case BOOTS_LEFT:
                addBuiltinAttachment(attachment, instance, "left_leg");
                break;
            case LEGGINGS_RIGHT:
            case BOOTS_RIGHT:
                addBuiltinAttachment(attachment, instance, "right_leg");
                break;
            case ITEM_LEFT:
                addBuiltinAttachment(attachment, instance, "left_arm", new Vector3d(0, -entityModel.armLength, 0));
                break;
            case ITEM_RIGHT:
                addBuiltinAttachment(attachment, instance, "right_arm", new Vector3d(0, -entityModel.armLength, 0));
                break;
            case PARROT_LEFT:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(entityModel.armInterval / 2 + 1.4, entityModel.armPivotHeight - 24, 0));
                break;
            case PARROT_RIGHT:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(-entityModel.armInterval / 2 - 1.4, entityModel.armPivotHeight - 24, 0));
                break;
            default:
                super.addAttachments(attachment, instance);
                break;
        }
    }
}
