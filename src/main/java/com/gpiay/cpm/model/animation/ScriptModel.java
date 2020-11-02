package com.gpiay.cpm.model.animation;

import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.IModelBone;
import com.gpiay.cpm.model.element.ItemModelInstance;
import com.gpiay.cpm.model.element.ParticleEmitterInstance;

public class ScriptModel {
    private final ModelInstance model;

    public ScriptModel(ModelInstance model) {
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
        ParticleEmitterInstance instance = model.getParticle(particleName);
        return instance == null ? null : new ScriptParticle(instance);
    }

    public ScriptItemModel getItemModel(String itemName) {
        ItemModelInstance instance = model.getItemModel(itemName);
        return instance == null ? null : new ScriptItemModel(instance);
    }

    public boolean isFirstPerson() {
        return model.isRenderingFirstPerson;
    }
}
