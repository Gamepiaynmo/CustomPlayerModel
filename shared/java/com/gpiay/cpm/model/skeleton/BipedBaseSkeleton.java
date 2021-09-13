package com.gpiay.cpm.model.skeleton;

import com.google.common.collect.Lists;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.VanillaBone;
import com.gpiay.cpm.model.skeleton.model.CustomBipedModel;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;

import java.util.List;

public class BipedBaseSkeleton<M extends CustomBipedModel> extends Skeleton<M> {
    protected final VanillaBone head, body, rightArm, leftArm, rightLeg, leftLeg;

    public BipedBaseSkeleton(M model) {
        super(model);

        head = registerVanillaBone("head", model.head);
        body = registerVanillaBone("body", model.body);
        rightArm = registerVanillaBone("right_arm", model.rightArm);
        leftArm = registerVanillaBone("left_arm", model.leftArm);
        rightLeg = registerVanillaBone("right_leg", model.rightLeg);
        leftLeg = registerVanillaBone("left_leg", model.leftLeg);
    }

    public static BipedModel.ArmPose getArmPose(LivingEntity p_241741_0_, Hand p_241741_1_) {
        ItemStack itemstack = p_241741_0_.getItemInHand(p_241741_1_);
        if (itemstack.isEmpty()) {
            return BipedModel.ArmPose.EMPTY;
        } else {
            if (p_241741_0_.getUsedItemHand() == p_241741_1_ && p_241741_0_.getUseItemRemainingTicks() > 0) {
                UseAction useaction = itemstack.getUseAnimation();
                if (useaction == UseAction.BLOCK) {
                    return BipedModel.ArmPose.BLOCK;
                }

                if (useaction == UseAction.BOW) {
                    return BipedModel.ArmPose.BOW_AND_ARROW;
                }

                if (useaction == UseAction.SPEAR) {
                    return BipedModel.ArmPose.THROW_SPEAR;
                }

                if (useaction == UseAction.CROSSBOW && p_241741_1_ == p_241741_0_.getUsedItemHand()) {
                    return BipedModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!p_241741_0_.swinging && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack)) {
                return BipedModel.ArmPose.CROSSBOW_HOLD;
            }

            return BipedModel.ArmPose.ITEM;
        }
    }

    @Override
    public void update(LivingEntity entity, float animPos, float animSpeed, float age, float headYaw,
                       float headPitch, float partial, double scale, boolean firstPerson) {
        if (entity.isSpectator()) {
            entityModel.setAllVisible(false);
            entityModel.head.visible = true;
            entityModel.hat.visible = true;
        } else {
            entityModel.setAllVisible(true);
            entityModel.crouching = entity.isCrouching();
            BipedModel.ArmPose mainPose = getArmPose(entity, Hand.MAIN_HAND);
            BipedModel.ArmPose offPose = getArmPose(entity, Hand.OFF_HAND);
            if (mainPose.isTwoHanded()) {
                offPose = entity.getOffhandItem().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
            }

            if (entity.getMainArm() == HandSide.RIGHT) {
                entityModel.rightArmPose = mainPose;
                entityModel.leftArmPose = offPose;
            } else {
                entityModel.rightArmPose = offPose;
                entityModel.leftArmPose = mainPose;
            }
        }

        if (firstPerson) {
            entityModel.attackTime = 0.0f;
            entityModel.crouching = false;
            entityModel.swimAmount = 0.0f;
            entityModel.setupAnim(entity, animPos, animSpeed, age, headYaw, headPitch);
            entityModel.leftArm.xRot = 0.0f;
            entityModel.rightArm.xRot = 0.0f;
            adjustBones(entity, scale, partial);
        } else {
            super.update(entity, animPos, animSpeed, age, headYaw, headPitch, partial, scale, firstPerson);
        }
    }

    @Override
    protected void adjustBones(LivingEntity entity, double scale, float partialTicks) {
        super.adjustBones(entity, scale, partialTicks);

        if (entity.isBaby()) {
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
            case NONE:
                addBuiltinAttachment(attachment, instance, "none");
                break;
            case HELMET:
            case SKULL:
                addBuiltinAttachment(attachment, instance, "head");
                break;
            case CHESTPLATE_BODY:
            case LEGGINGS_BODY:
            case CAPE:
                addBuiltinAttachment(attachment, instance, "body", new Vector3d(0, 0, 0));
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
