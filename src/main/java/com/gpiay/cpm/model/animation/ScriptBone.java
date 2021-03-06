package com.gpiay.cpm.model.animation;

import com.gpiay.cpm.model.element.IModelBone;
import com.gpiay.cpm.model.element.ModelBone;

public class ScriptBone {
    private final IModelBone bone;

    private final boolean isModel;
    private ModelBone.Instance modelBone;

    public ScriptBone(IModelBone bone) {
        isModel = bone instanceof ModelBone.Instance;
        this.bone = bone;
        if (isModel)
            modelBone = (ModelBone.Instance) bone;
    }

    public ScriptBone getParent() {
        return bone.getParent() == null ? null : new ScriptBone(bone.getParent());
    }

    public double getPositionX() { return bone.getPosition().x * 16; }
    public double getPositionY() { return bone.getPosition().y * -16; }
    public double getPositionZ() { return bone.getPosition().z * 16; }
    public double getRotationX() { return bone.getRotation().y; }
    public double getRotationY() { return bone.getRotation().x; }
    public double getRotationZ() { return bone.getRotation().z; }
    public double getScaleX() { return bone.getScale().x; }
    public double getScaleY() { return bone.getScale().y; }
    public double getScaleZ() { return bone.getScale().z; }
    public double getColorR() { return isModel ? modelBone.color.x : 1; }
    public double getColorG() { return isModel ? modelBone.color.y : 1; }
    public double getColorB() { return isModel ? modelBone.color.z : 1; }
    public double getColorA() { return isModel ? modelBone.color.w : 1; }

    public boolean isVisible() { return bone.isVisible(); }

    public void setPositionX(double value) { if (isModel) modelBone.position.x = value / 16; }
    public void setPositionY(double value) { if (isModel) modelBone.position.y = value / -16; }
    public void setPositionZ(double value) { if (isModel) modelBone.position.z = value / 16; }
    public void setPosition(double x, double y, double z) {
        if (isModel) {
            modelBone.position.x = x / 16;
            modelBone.position.y = y / -16;
            modelBone.position.z = z / 16;
        }
    }

    public void setRotationX(double value) { if (isModel) modelBone.rotation.y = value; }
    public void setRotationY(double value) { if (isModel) modelBone.rotation.x = value; }
    public void setRotationZ(double value) { if (isModel) modelBone.rotation.z = value; }
    public void setRotation(double x, double y, double z) {
        if (isModel) {
            modelBone.rotation.y = x;
            modelBone.rotation.x = y;
            modelBone.rotation.z = z;
        }
    }

    public void setScaleX(double value) { if (isModel) modelBone.scale.x = value; }
    public void setScaleY(double value) { if (isModel) modelBone.scale.y = value; }
    public void setScaleZ(double value) { if (isModel) modelBone.scale.z = value; }
    public void setScale(double x, double y, double z) {
        if (isModel) {
            modelBone.scale.x = x;
            modelBone.scale.y = y;
            modelBone.scale.z = z;
        }
    }

    public void setColorR(double value) { if (isModel) modelBone.color.x = value; }
    public void setColorG(double value) { if (isModel) modelBone.color.y = value; }
    public void setColorB(double value) { if (isModel) modelBone.color.z = value; }
    public void setColorA(double value) { if (isModel) modelBone.color.w = value; }
    public void setColor(double r, double g, double b, double a) {
        if (isModel) {
            modelBone.color.x = r;
            modelBone.color.y = g;
            modelBone.color.z = b;
            modelBone.color.w = a;
        }
    }

    public void setVisible(boolean value) { if (isModel) modelBone.visible = value; }

    public void physicalize(double elasticity, double stiffness, double damping, double friction, double gravity) {
        if (isModel) modelBone.physicalize(elasticity, stiffness, damping, friction, gravity);
    }
}
