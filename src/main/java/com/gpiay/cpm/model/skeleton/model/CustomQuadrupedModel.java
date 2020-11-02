package com.gpiay.cpm.model.skeleton.model;

import net.minecraft.client.renderer.entity.model.QuadrupedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

public class CustomQuadrupedModel extends QuadrupedModel<LivingEntity> {
    public final float legLength, legInterval, legOffset;
    public final float bodyPivotHeight, bodyLength, bodyOffset;
    public final float headPivotHeight, headOffset;

    public CustomQuadrupedModel(float legLength, float legInterval, float legOffset, float bodyPivotHeight,
                                float bodyLength, float bodyOffset, float headPivotHeight, float headOffset) {
        super(0, 0);

        headModel.setRotationPoint(0, 24.0f - headPivotHeight, headOffset);
        body.setRotationPoint(0, 24.0f - bodyPivotHeight, bodyOffset);
        legFrontRight.setRotationPoint(-legInterval / 2, 24.0f - legLength, legOffset);
        legFrontLeft.setRotationPoint(legInterval / 2, 24.0f - legLength, legOffset);
        legBackRight.setRotationPoint(-legInterval / 2, 24.0f - legLength, legOffset + bodyLength);
        legBackLeft.setRotationPoint(legInterval / 2, 24.0f - legLength, legOffset + bodyLength);

        this.legLength = legLength;
        this.legInterval = legInterval;
        this.legOffset = legOffset;
        this.bodyPivotHeight = bodyPivotHeight;
        this.bodyLength = bodyLength;
        this.bodyOffset = bodyOffset;
        this.headPivotHeight = headPivotHeight;
        this.headOffset = headOffset;
    }

    public CustomQuadrupedModel(Map<String, Float> param) {
        this(param.getOrDefault("leg_length", 6.0f),
                param.getOrDefault("leg_interval", 6.0f),
                param.getOrDefault("leg_offset", 1.0f),
                param.getOrDefault("body_pivot_height", 11.0f),
                param.getOrDefault("body_length", 12.0f),
                param.getOrDefault("body_Offset", 2.0f),
                param.getOrDefault("head_pivot_height", 12.0f),
                param.getOrDefault("head_offset", -6.0f));
    }

    @Override
    public void setRotationAngles(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        this.headModel.rotateAngleX = headPitch * ((float)Math.PI / 180F);
        this.headModel.rotateAngleY = netHeadYaw * ((float)Math.PI / 180F);
        this.body.rotateAngleX = ((float)Math.PI / 2F);
        this.legBackRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legBackLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.legFrontRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.legFrontLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }
}
