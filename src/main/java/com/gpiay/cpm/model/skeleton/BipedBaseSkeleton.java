package com.gpiay.cpm.model.skeleton;

import com.google.common.collect.Lists;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.VanillaBone;
import com.gpiay.cpm.model.skeleton.model.CustomBipedModel;
import com.gpiay.cpm.util.math.Vector3d;
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
    protected final VanillaBone head, body, leftArm, rightArm, leftLeg, rightLeg;

    public BipedBaseSkeleton(M model) {
        super(model);

        head = registerVanillaBone("head");
        registerVanillaBone("unused");
        body = registerVanillaBone("body");
        rightArm = registerVanillaBone("right_arm");
        leftArm = registerVanillaBone("left_arm");
        rightLeg = registerVanillaBone("right_leg");
        leftLeg = registerVanillaBone("left_leg");
    }

    @Override
    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                       float headPitch, float scaleFactor, float partialTicks, double scale, boolean firstPerson) {
        if (entity.isSpectator()) {
            entityModel.setVisible(false);
            entityModel.bipedHead.showModel = true;
            entityModel.bipedHeadwear.showModel = true;
        } else {
            ItemStack mainItem = entity.getHeldItemMainhand();
            ItemStack offItem = entity.getHeldItemOffhand();
            entityModel.setVisible(true);
            entityModel.isSneak = entity.shouldRenderSneaking();
            BipedModel.ArmPose mainPose = this.getArmPose(entity, mainItem, offItem, Hand.MAIN_HAND);
            BipedModel.ArmPose offPose = this.getArmPose(entity, mainItem, offItem, Hand.OFF_HAND);
            if (entity.getPrimaryHand() == HandSide.RIGHT) {
                entityModel.rightArmPose = mainPose;
                entityModel.leftArmPose = offPose;
            } else {
                entityModel.rightArmPose = offPose;
                entityModel.leftArmPose = mainPose;
            }
        }

        if (firstPerson) {
            entityModel.setVisible(true);
            entityModel.swingProgress = 0.0f;
            entityModel.isSneak = false;
            entityModel.swimAnimation = 0.0f;
            entityModel.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            entityModel.bipedLeftArm.rotateAngleX = 0.0f;
            entityModel.bipedRightArm.rotateAngleX = 0.0f;
            adjustBones(entity, scale, partialTicks, true);
        } else {
            super.update(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, partialTicks, scale, firstPerson);
        }
    }

    private BipedModel.ArmPose getArmPose(LivingEntity entity, ItemStack mainItem, ItemStack offItem, Hand hand) {
        BipedModel.ArmPose pose = BipedModel.ArmPose.EMPTY;
        ItemStack itemstack = hand == Hand.MAIN_HAND ? mainItem : offItem;
        if (!itemstack.isEmpty()) {
            pose = BipedModel.ArmPose.ITEM;
            if (entity.getItemInUseCount() > 0) {
                UseAction useaction = itemstack.getUseAction();
                if (useaction == UseAction.BLOCK) {
                    pose = BipedModel.ArmPose.BLOCK;
                } else if (useaction == UseAction.BOW) {
                    pose = BipedModel.ArmPose.BOW_AND_ARROW;
                } else if (useaction == UseAction.SPEAR) {
                    pose = BipedModel.ArmPose.THROW_SPEAR;
                } else if (useaction == UseAction.CROSSBOW && hand == entity.getActiveHand()) {
                    pose = BipedModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else {
                if (mainItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(mainItem)) {
                    pose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }

                if (offItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(offItem)
                        && mainItem.getItem().getUseAction(mainItem) == UseAction.NONE) {
                    pose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }

        return pose;
    }

    @Override
    protected void adjustBones(LivingEntity entity, double scale, float partialTicks, boolean firstPerson) {
        super.adjustBones(entity, scale, partialTicks, firstPerson);

        if (entity.isChild()) {
            none.scale(0.5).offset(0, 12, 0);
            head.scale(0.75).offset(0, 6 + 0.25f * entityModel.headPivotHeight * scale, 0);
            body.scale(0.5).offset(0, 12, 0);
            leftArm.scale(0.5).offset(0, 12, 0);
            rightArm.scale(0.5).offset(0, 12, 0);
            leftLeg.scale(0.5).offset(0, 12, 0);
            rightLeg.scale(0.5).offset(0, 12, 0);
        } else if (entity.shouldRenderSneaking() && !firstPerson) {
            none.offset(0, 3.2 * scale, 0);
            head.offset(0, 3.2 * scale, 0);
            body.offset(0, 3.2 * scale, 0);
            leftArm.offset(0, 3.2 * scale, 0);
            rightArm.offset(0, 3.2 * scale, 0);
            leftLeg.offset(0, 3.2 * scale, 0);
            rightLeg.offset(0, 3.2 * scale, 0);
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
                addBuiltinAttachment(attachment, instance, "body");
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
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(entityModel.armInterval / 2 + 1, entityModel.armPivotHeight - 24, 0));
                break;
            case PARROT_RIGHT:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(-entityModel.armInterval / 2 - 1, entityModel.armPivotHeight - 24, 0));
                break;
            default:
                super.addAttachments(attachment, instance);
                break;
        }
    }
}
