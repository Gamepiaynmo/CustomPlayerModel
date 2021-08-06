package com.gpiay.cpm.model.skeleton.model;

import net.minecraft.client.renderer.entity.model.QuadrupedModel;
import net.minecraft.entity.LivingEntity;

import java.util.Map;

public class CustomQuadrupedModel extends QuadrupedModel<LivingEntity> {
    public final float legLength, legInterval, legOffset;
    public final float bodyPivotHeight, bodyLength, bodyOffset;
    public final float headPivotHeight, headOffset;

    public CustomQuadrupedModel(float legLength, float legInterval, float legOffset, float bodyPivotHeight,
                                float bodyLength, float bodyOffset, float headPivotHeight, float headOffset) {
        super(0, 0, false, headPivotHeight / 2, headOffset / 2, 2, 2, 24);

        head.setPos(0, 24.0f - headPivotHeight, headOffset);
        body.setPos(0, 24.0f - bodyPivotHeight, bodyOffset);
        leg0.setPos(-legInterval / 2, 24.0f - legLength, legOffset);
        leg1.setPos(legInterval / 2, 24.0f - legLength, legOffset);
        leg2.setPos(-legInterval / 2, 24.0f - legLength, legOffset + bodyLength);
        leg3.setPos(legInterval / 2, 24.0f - legLength, legOffset + bodyLength);

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
                param.getOrDefault("body_offset", 2.0f),
                param.getOrDefault("head_pivot_height", 12.0f),
                param.getOrDefault("head_offset", -6.0f));
    }
}
