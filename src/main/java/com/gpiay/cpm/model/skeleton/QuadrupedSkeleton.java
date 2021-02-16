package com.gpiay.cpm.model.skeleton;

import com.gpiay.cpm.model.skeleton.model.CustomQuadrupedModel;

import java.util.Map;

public class QuadrupedSkeleton extends QuadrupedBaseSkeleton<CustomQuadrupedModel> {
    public QuadrupedSkeleton(Map<String, Float> param) {
        super(new CustomQuadrupedModel(param));
    }
}
