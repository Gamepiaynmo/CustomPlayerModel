package com.gpiay.cpm.model.skeleton.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

public class CustomBipedModel extends BipedModel<LivingEntity> {
    public final float legLength, legInterval, legOffset;
    public final float bodyPivotHeight, bodyOffset;
    public final float armPivotHeight, armInterval, armOffset, armLength;
    public final float headPivotHeight, headOffset;

    public CustomBipedModel(float legLength, float legInterval, float legOffset, float bodyPivotHeight, float bodyOffset,
                            float armPivotHeight, float armInterval, float armOffset, float armLength, float headPivotHeight, float headOffset) {
        super(0);
        head.setPos(0, 24.0f - headPivotHeight, headOffset);
        body.setPos(0, 24.0f - bodyPivotHeight, bodyOffset);
        rightArm.setPos(-armInterval / 2, 24.0f - armPivotHeight, armOffset);
        leftArm.setPos(armInterval / 2, 24.0f - armPivotHeight, armOffset);
        rightLeg.setPos(-legInterval / 2, 24.0f - legLength, legOffset);
        leftLeg.setPos(legInterval / 2, 24.0f - legLength, legOffset);

        this.legLength = legLength;
        this.legInterval = legInterval;
        this.legOffset = legOffset;
        this.bodyPivotHeight = bodyPivotHeight;
        this.bodyOffset = bodyOffset;
        this.armPivotHeight = armPivotHeight;
        this.armInterval = armInterval;
        this.armOffset = armOffset;
        this.armLength = armLength;
        this.headPivotHeight = headPivotHeight;
        this.headOffset = headOffset;
    }

    public CustomBipedModel(Map<String, Float> param) {
        this(param.getOrDefault("leg_length", 12.0f),
                param.getOrDefault("leg_interval", 3.8f),
                param.getOrDefault("leg_offset", 0.0f),
                param.getOrDefault("body_pivot_height", 24.0f),
                param.getOrDefault("body_offset", 0.0f),
                param.getOrDefault("arm_pivot_height", 22.0f),
                param.getOrDefault("arm_interval", 10.0f),
                param.getOrDefault("arm_offset", 0.0f),
                param.getOrDefault("arm_length", 9.0f),
                param.getOrDefault("head_pivot_height", 24.0f),
                param.getOrDefault("head_offset", 0.0f));
    }

    @SuppressWarnings({"unchecked"})
    private BipedModel<LivingEntity> getModel(LivingEntity livingEntity) {
        EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);
        EntityModel<?> model = renderer instanceof LivingRenderer ? ((LivingRenderer<?, ?>) renderer).getModel() : null;
        return model instanceof BipedModel ? (BipedModel<LivingEntity>) model : null;
    }

    private static float partial;

    @Override
    public void prepareMobModel(LivingEntity p_212843_1_, float p_212843_2_, float p_212843_3_, float p_212843_4_) {
        super.prepareMobModel(p_212843_1_, p_212843_2_, p_212843_3_, p_212843_4_);
        partial = p_212843_4_;
    }

    @Override
    public void setupAnim(LivingEntity p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
        BipedModel<LivingEntity> model = getModel(p_225597_1_);
        if (partial == 1.0)
            super.setupAnim(p_225597_1_, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);

        if (model == null) {
            boolean lvt_7_1_ = p_225597_1_.getFallFlyingTicks() > 4;
            boolean lvt_8_1_ = p_225597_1_.isVisuallySwimming();
            this.head.yRot = p_225597_5_ * 0.017453292F;
            if (lvt_7_1_) {
                this.head.xRot = -0.7853982F;
            } else if (this.swimAmount > 0.0F) {
                if (lvt_8_1_) {
                    this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, -0.7853982F);
                } else {
                    this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, p_225597_6_ * 0.017453292F);
                }
            } else {
                this.head.xRot = p_225597_6_ * 0.017453292F;
            }

            this.body.yRot = 0.0F;

            float lvt_9_1_ = 1.0F;
            if (lvt_7_1_) {
                lvt_9_1_ = (float) p_225597_1_.getDeltaMovement().lengthSqr();
                lvt_9_1_ /= 0.2F;
                lvt_9_1_ *= lvt_9_1_ * lvt_9_1_;
            }

            if (lvt_9_1_ < 1.0F) {
                lvt_9_1_ = 1.0F;
            }

            this.rightArm.xRot = MathHelper.cos(p_225597_2_ * 0.6662F + 3.1415927F) * 2.0F * p_225597_3_ * 0.5F / lvt_9_1_;
            this.leftArm.xRot = MathHelper.cos(p_225597_2_ * 0.6662F) * 2.0F * p_225597_3_ * 0.5F / lvt_9_1_;
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = MathHelper.cos(p_225597_2_ * 0.6662F) * 1.4F * p_225597_3_ / lvt_9_1_;
            this.leftLeg.xRot = MathHelper.cos(p_225597_2_ * 0.6662F + 3.1415927F) * 1.4F * p_225597_3_ / lvt_9_1_;
            this.rightLeg.yRot = 0.0F;
            this.leftLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
            if (this.riding) {
                this.rightArm.xRot += -0.62831855F;
                this.leftArm.xRot += -0.62831855F;
                this.rightLeg.xRot = -1.4137167F;
                this.rightLeg.yRot = 0.31415927F;
                this.rightLeg.zRot = 0.07853982F;
                this.leftLeg.xRot = -1.4137167F;
                this.leftLeg.yRot = -0.31415927F;
                this.leftLeg.zRot = -0.07853982F;
            }

            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
            boolean lvt_10_1_ = p_225597_1_.getMainArm() == HandSide.RIGHT;
            boolean lvt_11_1_ = lvt_10_1_ ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
            if (lvt_10_1_ != lvt_11_1_) {
                this.poseLeftArm(p_225597_1_);
                this.poseRightArm(p_225597_1_);
            } else {
                this.poseRightArm(p_225597_1_);
                this.poseLeftArm(p_225597_1_);
            }
        }

        this.rightArm.z = 0.0F;
        this.rightArm.x = -armInterval / 2; // change
        this.leftArm.z = 0.0F;
        this.leftArm.x = armInterval / 2; // change

        this.setupAttackAnimation(p_225597_1_, model, p_225597_4_);
        if (this.crouching) {
            this.rightLeg.z = 4.0F + legOffset; // change
            this.leftLeg.z = 4.0F + legOffset; // change
            this.rightLeg.y = 24.2F - legLength; // change
            this.leftLeg.y = 24.2F - legLength; // change
            this.head.y = 28.2F - headPivotHeight; // change
            this.body.y = 27.2F - bodyPivotHeight; // change
            this.leftArm.y = 27.2F - armPivotHeight; // change
            this.rightArm.y = 27.2F - armPivotHeight; // change
        } else {
            this.rightLeg.z = 0.1F + legOffset; // change
            this.leftLeg.z = 0.1F + legOffset; // change
            this.rightLeg.y = 24.0F - legLength; // change
            this.leftLeg.y = 24.0F - legLength; // change
            this.head.y = 24.0F - headPivotHeight; // change
            this.body.y = 24.0F - bodyPivotHeight; // change
            this.leftArm.y = 24.0F - armPivotHeight; // change
            this.rightArm.y = 24.0F - armPivotHeight; // change
        }

        if (model == null) {
            if (this.crouching) {
                this.body.xRot = 0.5F;
                this.rightArm.xRot += 0.4F;
                this.leftArm.xRot += 0.4F;
            } else {
                this.body.xRot = 0.0F;
            }

            ModelHelper.bobArms(this.rightArm, this.leftArm, p_225597_4_);
            if (this.swimAmount > 0.0F) {
                float lvt_12_1_ = p_225597_2_ % 26.0F;
                HandSide lvt_13_1_ = this.getAttackArm(p_225597_1_);
                float lvt_14_1_ = lvt_13_1_ == HandSide.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
                float lvt_15_1_ = lvt_13_1_ == HandSide.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
                float lvt_16_2_;
                if (lvt_12_1_ < 14.0F) {
                    this.leftArm.xRot = this.rotlerpRad(lvt_15_1_, this.leftArm.xRot, 0.0F);
                    this.rightArm.xRot = MathHelper.lerp(lvt_14_1_, this.rightArm.xRot, 0.0F);
                    this.leftArm.yRot = this.rotlerpRad(lvt_15_1_, this.leftArm.yRot, 3.1415927F);
                    this.rightArm.yRot = MathHelper.lerp(lvt_14_1_, this.rightArm.yRot, 3.1415927F);
                    this.leftArm.zRot = this.rotlerpRad(lvt_15_1_, this.leftArm.zRot, 3.1415927F + 1.8707964F * this.quadraticArmUpdate(lvt_12_1_) / this.quadraticArmUpdate(14.0F));
                    this.rightArm.zRot = MathHelper.lerp(lvt_14_1_, this.rightArm.zRot, 3.1415927F - 1.8707964F * this.quadraticArmUpdate(lvt_12_1_) / this.quadraticArmUpdate(14.0F));
                } else if (lvt_12_1_ >= 14.0F && lvt_12_1_ < 22.0F) {
                    lvt_16_2_ = (lvt_12_1_ - 14.0F) / 8.0F;
                    this.leftArm.xRot = this.rotlerpRad(lvt_15_1_, this.leftArm.xRot, 1.5707964F * lvt_16_2_);
                    this.rightArm.xRot = MathHelper.lerp(lvt_14_1_, this.rightArm.xRot, 1.5707964F * lvt_16_2_);
                    this.leftArm.yRot = this.rotlerpRad(lvt_15_1_, this.leftArm.yRot, 3.1415927F);
                    this.rightArm.yRot = MathHelper.lerp(lvt_14_1_, this.rightArm.yRot, 3.1415927F);
                    this.leftArm.zRot = this.rotlerpRad(lvt_15_1_, this.leftArm.zRot, 5.012389F - 1.8707964F * lvt_16_2_);
                    this.rightArm.zRot = MathHelper.lerp(lvt_14_1_, this.rightArm.zRot, 1.2707963F + 1.8707964F * lvt_16_2_);
                } else if (lvt_12_1_ >= 22.0F && lvt_12_1_ < 26.0F) {
                    lvt_16_2_ = (lvt_12_1_ - 22.0F) / 4.0F;
                    this.leftArm.xRot = this.rotlerpRad(lvt_15_1_, this.leftArm.xRot, 1.5707964F - 1.5707964F * lvt_16_2_);
                    this.rightArm.xRot = MathHelper.lerp(lvt_14_1_, this.rightArm.xRot, 1.5707964F - 1.5707964F * lvt_16_2_);
                    this.leftArm.yRot = this.rotlerpRad(lvt_15_1_, this.leftArm.yRot, 3.1415927F);
                    this.rightArm.yRot = MathHelper.lerp(lvt_14_1_, this.rightArm.yRot, 3.1415927F);
                    this.leftArm.zRot = this.rotlerpRad(lvt_15_1_, this.leftArm.zRot, 3.1415927F);
                    this.rightArm.zRot = MathHelper.lerp(lvt_14_1_, this.rightArm.zRot, 3.1415927F);
                }

                this.leftLeg.xRot = MathHelper.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * MathHelper.cos(p_225597_2_ * 0.33333334F + 3.1415927F));
                this.rightLeg.xRot = MathHelper.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * MathHelper.cos(p_225597_2_ * 0.33333334F));
            }
        } else {
            copyRotation(model.head, this.head);
            copyRotation(model.body, this.body);
            copyRotation(model.leftArm, this.leftArm);
            copyRotation(model.rightArm, this.rightArm);
            copyRotation(model.leftLeg, this.leftLeg);
            copyRotation(model.rightLeg, this.rightLeg);
        }
    }

    private void copyRotation(ModelRenderer from, ModelRenderer to) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
    }

    protected void setupAttackAnimation(LivingEntity p_230486_1_, BipedModel<LivingEntity> model, float p_230486_2_) {
        if (!(this.attackTime <= 0.0F)) {
            if (model == null) {
                HandSide lvt_3_1_ = this.getAttackArm(p_230486_1_);
                ModelRenderer lvt_4_1_ = this.getArm(lvt_3_1_);
                float lvt_5_1_ = this.attackTime;
                this.body.yRot = MathHelper.sin(MathHelper.sqrt(lvt_5_1_) * 6.2831855F) * 0.2F;
                if (lvt_3_1_ == HandSide.LEFT) {
                    this.body.yRot *= -1.0F;
                }

                this.rightArm.yRot += this.body.yRot;
                this.leftArm.yRot += this.body.yRot;
                lvt_5_1_ = 1.0F - this.attackTime;
                lvt_5_1_ *= lvt_5_1_;
                lvt_5_1_ *= lvt_5_1_;
                lvt_5_1_ = 1.0F - lvt_5_1_;
                float lvt_6_1_ = MathHelper.sin(lvt_5_1_ * 3.1415927F);
                float lvt_7_1_ = MathHelper.sin(this.attackTime * 3.1415927F) * -(this.head.xRot - 0.7F) * 0.75F;
                lvt_4_1_.xRot = (float) ((double) lvt_4_1_.xRot - ((double) lvt_6_1_ * 1.2D + (double) lvt_7_1_));
                lvt_4_1_.yRot += this.body.yRot * 2.0F;
                lvt_4_1_.zRot += MathHelper.sin(this.attackTime * 3.1415927F) * -0.4F;
            }

            this.rightArm.z = MathHelper.sin(this.body.yRot) * armInterval / 2; // change
            this.rightArm.x = -MathHelper.cos(this.body.yRot) * armInterval / 2; // change
            this.leftArm.z = -MathHelper.sin(this.body.yRot) * armInterval / 2; // change
            this.leftArm.x = MathHelper.cos(this.body.yRot) * armInterval / 2; // change
        }
    }
}
