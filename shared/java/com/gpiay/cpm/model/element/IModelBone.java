package com.gpiay.cpm.model.element;

import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Quat4d;
import com.gpiay.cpm.util.math.Vector3d;

public interface IModelBone {
    String getName();
    IModelBone getParent();

    boolean isVisible();

    Vector3d getPosition();
    Vector3d getRotation();
    Vector3d getScale();

    default Quat4d getQuaternion() {
        Vector3d rotation = getRotation();
        return new Quat4d().setEulerAngles(rotation.x, rotation.y, 0).mulLeft(new Quat4d().setFromAxis(0, 0, 1, rotation.z));
    }

    default Matrix4d getTransform() {
        return new Matrix4d().translate(getPosition()).rotate(getQuaternion()).scale(getScale());
    }

    default void setCalculateTransform() {
        IModelBone bone = this;
        while (bone instanceof ModelBone.Instance) {
            ModelBone.Instance boneInstance = (ModelBone.Instance) bone;
            if (boneInstance.calculateTransform)
                break;

            boneInstance.calculateTransform = true;
            bone = bone.getParent();
        }
    }
}
