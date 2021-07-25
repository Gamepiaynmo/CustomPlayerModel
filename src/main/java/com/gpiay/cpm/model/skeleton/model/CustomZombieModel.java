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
    public void setupAnim(LivingEntity p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
        super.setupAnim(p_225597_1_, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
        boolean aggressive = p_225597_1_ instanceof MonsterEntity && ((MonsterEntity) p_225597_1_).isAggressive();
        ModelHelper.animateZombieArms(this.leftArm, this.rightArm, aggressive, this.attackTime, p_225597_4_);
    }
}
