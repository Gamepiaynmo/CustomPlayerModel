package com.gpiay.cpm.model.element;

import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Quat4d;
import com.gpiay.cpm.util.math.Vector3d;

public interface IModelBone {
    String getId();
    IModelBone getParent();

    boolean isVisible();

    Vector3d getPosition();
    Vector3d getRotation();
    Vector3d getScale();

    default Quat4d getQuaternion() {
        Vector3d rotation = getRotation();
        return new Quat4d().setEulerAngles(rotation.x, rotation.y, rotation.z);
    }

    default Matrix4d getTransform() {
        return new Matrix4d().translate(getPosition()).rotate(getQuaternion()).scale(getScale());
    }
}
