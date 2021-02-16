package com.gpiay.cpm.model.skeleton;

import com.gpiay.cpm.model.skeleton.model.CustomBipedModel;

import java.util.Map;

public class BipedSkeleton extends BipedBaseSkeleton<CustomBipedModel> {
    public BipedSkeleton(Map<String, Float> param) {
        super(new CustomBipedModel(param));
    }
}
