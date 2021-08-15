package com.gpiay.cpm.model.animation;

import com.gpiay.cpm.model.ModelBase;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.IModelBone;
import com.gpiay.cpm.model.element.ItemModel;
import com.gpiay.cpm.model.element.ParticleEmitter;

public class ScriptModel {
    private final ModelBase model;

    public ScriptModel(ModelBase model) {
        this.model = model;
    }

    public ScriptBone getBone(String name) {
        IModelBone bone = model.getBone(name);
        return bone == null ? null : new ScriptBone(bone);
    }

    public String getTexture(String partName) {
        return model.getTexture(partName);
    }

    public void setTexture(String partName, String textureName) {
        model.setTexture(partName, textureName);
    }

    public ScriptParticle getParticle(String particleName) {
        ParticleEmitter.Instance instance = model.getParticle(particleName);
        return instance == null ? null : new ScriptParticle(instance);
    }

    public ScriptItemModel getItemModel(String itemName) {
        ItemModel.Instance instance = model.getItemModel(itemName);
        return instance == null ? null : new ScriptItemModel(instance);
    }

    public boolean isFirstPerson() {
        return ModelInstance.isRenderingFirstPerson;
    }
}
