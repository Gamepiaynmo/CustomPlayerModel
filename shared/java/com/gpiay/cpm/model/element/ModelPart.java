package com.gpiay.cpm.model.element;

import com.google.common.collect.Lists;
import com.gpiay.cpm.model.ModelInstance;

import java.util.List;

public class ModelPart extends ModelElement {
    int textureWidth, textureHeight;
    public String texture;
    public final List<ModelBone> bones = Lists.newArrayList();

    @Override
    public Instance instantiate(ModelInstance model) {
        Instance instance = new Instance(name, model);
        instance.texture = texture;
        return instance;
    }

    public static class Instance extends ModelElement.Instance implements ModelElement.ITextured {
        public String texture;
        public final List<ModelBone.Instance> bones = Lists.newArrayList();

        public Instance(String name, ModelInstance model) {
            super(name, model);
        }

        @Override
        public String getTexture() {
            return texture;
        }

        @Override
        public void setTexture(String texture) {
            if (model.getModelPack().hasTexture(texture))
                this.texture = texture;
        }
    }
}
