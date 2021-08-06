package com.gpiay.cpm.model.element;

import com.gpiay.cpm.model.ModelInstance;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.util.ResourceLocation;

public abstract class ModelElement {
    public String name;

    public abstract Instance instantiate(ModelInstance model);

    protected static IModelBone findParent(ModelInstance model, String parent) {
        IModelBone bone = model.getBone(parent);
        if (bone == null)
            bone = model.getBone("none");
        bone.setCalculateTransform();
        return bone;
    }

    protected static ResourceLocation findTexture(ModelInstance model, String texture) {
        ResourceLocation location = model.getModelPack().getTexture(texture);
        return location == null ? MissingTextureSprite.getLocation() : location;
    }

    public static class Instance {
        public final String name;
        public final ModelInstance model;

        public Instance(String name, ModelInstance model) {
            this.name = name;
            this.model = model;
        }
    }

    public interface IParented {
        IModelBone getParent();
    }

    public interface ITextured {
        String getTexture();
        void setTexture(String texture);
    }
}
