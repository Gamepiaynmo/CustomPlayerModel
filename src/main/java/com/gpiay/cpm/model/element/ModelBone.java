package com.gpiay.cpm.model.element;

import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Quat4d;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.platform.GlStateManager;

public class ModelBone implements IModelBone {
    final ModelInstance model;
    final String id;
    final IModelBone parent;

    public Vector3d position;
    public Vector3d rotation;
    public Vector3d scale;
    public boolean visible;
    public Quat4d color;

    boolean hierarchyVisible;

    public final BoneInfo boneInfo;

    public boolean physicalized = false;
    public double elasticity, stiffness, damping, friction, gravity;
    public Vector3d velocity = Vector3d.Zero.cpy();
    public boolean calculateTransform = false;

    public ModelBone(ModelInstance model, String id, IModelBone parent, BoneInfo boneInfo) {
        this.model = model;
        this.id = id;
        this.parent = parent;
        this.boneInfo = boneInfo;
    }

    public void render() {
        GlStateManager.color4f((float) color.x, (float) color.y, (float) color.z,
                (float) (model.isRenderingInvisible() ? 0.15f * color.w : color.w));

        if (boneInfo != null)
            boneInfo.render();
    }

    public void update() {
        hierarchyVisible = parent.isVisible() && visible;
    }

    public void physicalize(double elasticity, double stiffness, double damping, double friction, double gravity) {
        physicalized = true;
        model.enablePhysics();
        this.elasticity = elasticity;
        this.stiffness = stiffness;
        this.damping = damping;
        this.friction = friction;
        this.gravity = gravity;
        setCalculateTransform();
    }

    public void setCalculateTransform() {
        IModelBone bone = this;
        while (bone instanceof ModelBone) {
            ModelBone modelBone = (ModelBone) bone;
            if (modelBone.calculateTransform)
                break;

            modelBone.calculateTransform = true;
            bone = bone.getParent();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public IModelBone getParent() { return parent; }

    @Override
    public boolean isVisible() {
        return hierarchyVisible;
    }

    @Override
    public Vector3d getPosition() {
        return position.cpy();
    }

    @Override
    public Vector3d getRotation() {
        return rotation.cpy();
    }

    @Override
    public Vector3d getScale() {
        return scale.cpy();
    }
}
