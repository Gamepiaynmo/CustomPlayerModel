package com.gpiay.cpm.model.element;

import com.google.common.collect.Lists;

import java.util.List;

public class BedrockModel {
    int textureWidth, textureHeight;
    public String texture;
    public final List<BoneInfo> bones = Lists.newArrayList();
}
