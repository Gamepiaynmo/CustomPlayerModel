package com.gpiay.cpm.model.skeleton.model;

import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

public class CustomZombieModel extends CustomBipedModel {
    public CustomZombieModel(Map<String, Float> param) {
        super(param);
    }

    @Override
    public void setRotationAngles(LivingEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        boolean aggressive = entityIn instanceof MonsterEntity && ((MonsterEntity) entityIn).isAggressive();
        ModelHelper.func_239105_a_(this.bipedLeftArm, this.bipedRightArm, aggressive, this.swingProgress, ageInTicks);
    }
}
