package com.gpiay.cpm.model.element;

import com.gpiay.cpm.util.math.Quat4d;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.renderer.model.ModelRenderer;

public class VanillaBone implements IModelBone {
    private final String boneName;
    private final ModelRenderer model;

    protected Vector3d offset = Vector3d.Zero.cpy();
    protected Vector3d scale = Vector3d.One.cpy();

    public VanillaBone(String boneName, ModelRenderer model) {
        this.boneName = boneName;
        this.model = model;
    }

    public VanillaBone scale(double scale) {
        return scale(scale, scale, scale);
    }

    public VanillaBone scale(double x, double y, double z) {
        this.scale.scl(x, y, z);
        Vector3d position = new Vector3d(model.x, model.y, model.z);
        this.offset.add(position).scl(x, y, z).sub(position);
        return this;
    }

    public VanillaBone setScale(double scale) {
        return setScale(scale, scale, scale);
    }

    public VanillaBone setScale(double x, double y, double z) {
        this.scale.set(x, y, z);
        this.offset = new Vector3d(model.x, model.y, model.z).scl(x - 1, y - 1, z - 1);
        return this;
    }

    public VanillaBone offset(double dx, double dy, double dz) {
        offset.add(dx, dy, dz);
        return this;
    }

    public VanillaBone setOffset(double x, double y, double z) {
        offset.set(x, y, z);
        return this;
    }

    public VanillaBone reset() {
        scale.set(Vector3d.One);
        offset.set(Vector3d.Zero);
        return this;
    }

    @Override
    public String getName() {
        return boneName;
    }

    @Override
    public IModelBone getParent() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return model.visible;
    }

    @Override
    public Vector3d getPosition() {
        return new Vector3d(model.x, model.y, model.z).add(offset).scl(0.0625f);
    }

    @Override
    public Vector3d getRotation() {
        return new Vector3d(model.yRot, model.xRot, model.zRot).scl(Quat4d.radiansToDegrees);
    }

    @Override
    public Vector3d getScale() {
        return scale.cpy();
    }
}
