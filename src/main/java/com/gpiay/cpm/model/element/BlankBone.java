package com.gpiay.cpm.model.element;

import com.gpiay.cpm.util.math.Vector3d;

public class BlankBone extends VanillaBone {
    public BlankBone() {
        super("none", null);
    }

    @Override
    public VanillaBone scale(double scale) {
        return scale(scale, scale, scale);
    }

    @Override
    public BlankBone scale(double x, double y, double z) {
        this.scale.scl(x, y, z);
        this.offset.scl(x, y, z);
        return this;
    }

    @Override
    public VanillaBone setScale(double scale) {
        return setScale(scale, scale, scale);
    }

    @Override
    public BlankBone setScale(double x, double y, double z) {
        this.scale.set(x, y, z);
        this.offset = Vector3d.Zero.cpy();
        return this;
    }

    @Override
    public String getId() {
        return "none";
    }

    @Override
    public IModelBone getParent() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public Vector3d getPosition() {
        return offset.cpy().scl(0.0625);
    }

    @Override
    public Vector3d getRotation() {
        return Vector3d.Zero.cpy();
    }
}
