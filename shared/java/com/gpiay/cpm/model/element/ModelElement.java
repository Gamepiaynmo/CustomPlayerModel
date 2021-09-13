package com.gpiay.cpm.model.element;

import com.gpiay.cpm.model.ModelBase;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.exception.TranslatableException;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.util.ResourceLocation;

public abstract class ModelElement {
    public String name;

    public abstract Instance instantiate(ModelBase model);

    protected static IModelBone findParent(ModelBase model, String parent) {
        IModelBone bone = model.getBone(parent);
        if (bone == null) {
            if (model.getModelPack().isAccessory)
                throw new TranslatableException("error.cpm.loadModel.noParentId", parent);
            bone = model.getBone("none");
        }
        bone.setCalculateTransform();
        return bone;
    }

    protected static ResourceLocation findTexture(ModelBase model, String texture) {
        ResourceLocation location = model.getModelPack().getTexture(texture);
        return location == null ? MissingTextureSprite.getLocation() : location;
    }

    public static class Instance {
        public final String name;
        public final ModelBase model;

        public Instance(String name, ModelBase model) {
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
