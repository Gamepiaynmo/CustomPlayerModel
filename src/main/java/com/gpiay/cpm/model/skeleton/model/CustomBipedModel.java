package com.gpiay.cpm.model.skeleton.model;

import net.minecraft.client.renderer.entity.model.BipedModel;
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
        bipedHead.setRotationPoint(0, 24.0f - headPivotHeight, headOffset);
        bipedBody.setRotationPoint(0, 24.0f - bodyPivotHeight, bodyOffset);
        bipedRightArm.setRotationPoint(-armInterval / 2, 24.0f - armPivotHeight, armOffset);
        bipedLeftArm.setRotationPoint(armInterval / 2, 24.0f - armPivotHeight, armOffset);
        bipedRightLeg.setRotationPoint(-legInterval / 2, 24.0f - legLength, legOffset);
        bipedLeftLeg.setRotationPoint(legInterval / 2, 24.0f - legLength, legOffset);

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

    @Override
    public void setRotationAngles(LivingEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = entityIn.getTicksElytraFlying() > 4;
        boolean flag1 = entityIn.isActualySwimming();
        this.bipedHead.rotateAngleY = netHeadYaw * ((float)Math.PI / 180F);
        if (flag) {
            this.bipedHead.rotateAngleX = (-(float)Math.PI / 4F);
        } else if (this.swimAnimation > 0.0F) {
            if (flag1) {
                this.bipedHead.rotateAngleX = this.rotLerpRad(this.swimAnimation, this.bipedHead.rotateAngleX, (-(float)Math.PI / 4F));
            } else {
                this.bipedHead.rotateAngleX = this.rotLerpRad(this.swimAnimation, this.bipedHead.rotateAngleX, headPitch * ((float)Math.PI / 180F));
            }
        } else {
            this.bipedHead.rotateAngleX = headPitch * ((float)Math.PI / 180F);
        }

        this.bipedBody.rotateAngleY = 0.0F;
        this.bipedRightArm.rotationPointZ = 0.0F;
        this.bipedRightArm.rotationPointX = -armInterval / 2; // change
        this.bipedLeftArm.rotationPointZ = 0.0F;
        this.bipedLeftArm.rotationPointX = armInterval / 2; // change
        float f = 1.0F;
        if (flag) {
            f = (float)entityIn.getMotion().lengthSquared();
            f = f / 0.2F;
            f = f * f * f;
        }

        if (f < 1.0F) {
            f = 1.0F;
        }

        this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
        this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;
        this.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
        this.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount / f;
        this.bipedRightLeg.rotateAngleY = 0.0F;
        this.bipedLeftLeg.rotateAngleY = 0.0F;
        this.bipedRightLeg.rotateAngleZ = 0.0F;
        this.bipedLeftLeg.rotateAngleZ = 0.0F;
        if (this.isSitting) {
            this.bipedRightArm.rotateAngleX += (-(float)Math.PI / 5F);
            this.bipedLeftArm.rotateAngleX += (-(float)Math.PI / 5F);
            this.bipedRightLeg.rotateAngleX = -1.4137167F;
            this.bipedRightLeg.rotateAngleY = ((float)Math.PI / 10F);
            this.bipedRightLeg.rotateAngleZ = 0.07853982F;
            this.bipedLeftLeg.rotateAngleX = -1.4137167F;
            this.bipedLeftLeg.rotateAngleY = (-(float)Math.PI / 10F);
            this.bipedLeftLeg.rotateAngleZ = -0.07853982F;
        }

        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedLeftArm.rotateAngleY = 0.0F;
        boolean flag2 = entityIn.getPrimaryHand() == HandSide.RIGHT;
        boolean flag3 = flag2 ? this.leftArmPose.func_241657_a_() : this.rightArmPose.func_241657_a_();
        if (flag2 != flag3) {
            this.func_241655_c_(entityIn);
            this.func_241654_b_(entityIn);
        } else {
            this.func_241654_b_(entityIn);
            this.func_241655_c_(entityIn);
        }

        this.func_230486_a_(entityIn, ageInTicks);
        if (this.isSneak) {
            this.bipedBody.rotateAngleX = 0.5F;
            this.bipedRightArm.rotateAngleX += 0.4F;
            this.bipedLeftArm.rotateAngleX += 0.4F;
            this.bipedRightLeg.rotationPointZ = 4.0F + legOffset;
            this.bipedLeftLeg.rotationPointZ = 4.0F + legOffset;
            this.bipedRightLeg.rotationPointY = 24.2F - legLength;
            this.bipedLeftLeg.rotationPointY = 24.2F - legLength;
            this.bipedHead.rotationPointY = 28.2F - headPivotHeight;
            this.bipedBody.rotationPointY = 27.2F - bodyPivotHeight;
            this.bipedLeftArm.rotationPointY = 27.2F - armPivotHeight;
            this.bipedRightArm.rotationPointY = 27.2F - armPivotHeight;
        } else {
            this.bipedBody.rotateAngleX = 0.0F;
            this.bipedRightLeg.rotationPointZ = 0.1F + legOffset;
            this.bipedLeftLeg.rotationPointZ = 0.1F + legOffset;
            this.bipedRightLeg.rotationPointY = 24.0F - legLength;
            this.bipedLeftLeg.rotationPointY = 24.0F - legLength;
            this.bipedHead.rotationPointY = 24.0F - headPivotHeight;
            this.bipedBody.rotationPointY = 24.0F - bodyPivotHeight;
            this.bipedLeftArm.rotationPointY = 24.0F - armPivotHeight;
            this.bipedRightArm.rotationPointY = 24.0F - armPivotHeight;
        }

        ModelHelper.func_239101_a_(this.bipedRightArm, this.bipedLeftArm, ageInTicks);
        if (this.swimAnimation > 0.0F) {
            float f1 = limbSwing % 26.0F;
            HandSide handside = this.getMainHand(entityIn);
            float f2 = handside == HandSide.RIGHT && this.swingProgress > 0.0F ? 0.0F : this.swimAnimation;
            float f3 = handside == HandSide.LEFT && this.swingProgress > 0.0F ? 0.0F : this.swimAnimation;
            if (f1 < 14.0F) {
                this.bipedLeftArm.rotateAngleX = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleX, 0.0F);
                this.bipedRightArm.rotateAngleX = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleX, 0.0F);
                this.bipedLeftArm.rotateAngleY = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleY, (float)Math.PI);
                this.bipedRightArm.rotateAngleY = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleY, (float)Math.PI);
                this.bipedLeftArm.rotateAngleZ = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleZ, (float)Math.PI + 1.8707964F * this.getArmAngleSq(f1) / this.getArmAngleSq(14.0F));
                this.bipedRightArm.rotateAngleZ = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleZ, (float)Math.PI - 1.8707964F * this.getArmAngleSq(f1) / this.getArmAngleSq(14.0F));
            } else if (f1 >= 14.0F && f1 < 22.0F) {
                float f6 = (f1 - 14.0F) / 8.0F;
                this.bipedLeftArm.rotateAngleX = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleX, ((float)Math.PI / 2F) * f6);
                this.bipedRightArm.rotateAngleX = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleX, ((float)Math.PI / 2F) * f6);
                this.bipedLeftArm.rotateAngleY = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleY, (float)Math.PI);
                this.bipedRightArm.rotateAngleY = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleY, (float)Math.PI);
                this.bipedLeftArm.rotateAngleZ = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleZ, 5.012389F - 1.8707964F * f6);
                this.bipedRightArm.rotateAngleZ = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleZ, 1.2707963F + 1.8707964F * f6);
            } else if (f1 >= 22.0F && f1 < 26.0F) {
                float f4 = (f1 - 22.0F) / 4.0F;
                this.bipedLeftArm.rotateAngleX = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleX, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4);
                this.bipedRightArm.rotateAngleX = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleX, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4);
                this.bipedLeftArm.rotateAngleY = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleY, (float)Math.PI);
                this.bipedRightArm.rotateAngleY = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleY, (float)Math.PI);
                this.bipedLeftArm.rotateAngleZ = this.rotLerpRad(f3, this.bipedLeftArm.rotateAngleZ, (float)Math.PI);
                this.bipedRightArm.rotateAngleZ = MathHelper.lerp(f2, this.bipedRightArm.rotateAngleZ, (float)Math.PI);
            }

            float f7 = 0.3F;
            float f5 = 0.33333334F;
            this.bipedLeftLeg.rotateAngleX = MathHelper.lerp(this.swimAnimation, this.bipedLeftLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float)Math.PI));
            this.bipedRightLeg.rotateAngleX = MathHelper.lerp(this.swimAnimation, this.bipedRightLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F));
        }

        this.bipedHeadwear.copyModelAngles(this.bipedHead);
    }

    @Override
    protected void func_230486_a_(LivingEntity p_230486_1_, float p_230486_2_) {
        if (!(this.swingProgress <= 0.0F)) {
            HandSide handside = this.getMainHand(p_230486_1_);
            ModelRenderer modelrenderer = this.getArmForSide(handside);
            float f = this.swingProgress;
            this.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
            if (handside == HandSide.LEFT) {
                this.bipedBody.rotateAngleY *= -1.0F;
            }

            this.bipedRightArm.rotationPointZ = MathHelper.sin(this.bipedBody.rotateAngleY) * armInterval / 2; // change
            this.bipedRightArm.rotationPointX = -MathHelper.cos(this.bipedBody.rotateAngleY) * armInterval / 2; // change
            this.bipedLeftArm.rotationPointZ = -MathHelper.sin(this.bipedBody.rotateAngleY) * armInterval / 2; // change
            this.bipedLeftArm.rotationPointX = MathHelper.cos(this.bipedBody.rotateAngleY) * armInterval / 2; // change
            this.bipedRightArm.rotateAngleY += this.bipedBody.rotateAngleY;
            this.bipedLeftArm.rotateAngleY += this.bipedBody.rotateAngleY;
//            this.bipedLeftArm.rotateAngleX += this.bipedBody.rotateAngleY;
            f = 1.0F - this.swingProgress;
            f = f * f;
            f = f * f;
            f = 1.0F - f;
            float f1 = MathHelper.sin(f * (float)Math.PI);
            float f2 = MathHelper.sin(this.swingProgress * (float)Math.PI) * -(this.bipedHead.rotateAngleX - 0.7F) * 0.75F;
            modelrenderer.rotateAngleX = (float)((double)modelrenderer.rotateAngleX - ((double)f1 * 1.2D + (double)f2));
            modelrenderer.rotateAngleY += this.bipedBody.rotateAngleY * 2.0F;
            modelrenderer.rotateAngleZ += MathHelper.sin(this.swingProgress * (float)Math.PI) * -0.4F;
        }
    }
}
