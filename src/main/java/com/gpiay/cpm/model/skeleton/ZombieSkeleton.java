package com.gpiay.cpm.model.skeleton;

import com.gpiay.cpm.model.skeleton.model.CustomZombieModel;

import java.util.Map;

public class ZombieSkeleton extends BipedBaseSkeleton<CustomZombieModel> {
    public ZombieSkeleton(Map<String, Float> param) {
        super(new CustomZombieModel(param));
    }
}
