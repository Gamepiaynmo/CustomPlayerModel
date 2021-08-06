package com.gpiay.cpm.model.element;

import com.google.common.collect.Lists;
import com.gpiay.cpm.model.IComponent;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Quat4d;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;

import java.util.List;

public class ModelBone extends ModelElement {
    String parentName = "none";

    Vector3d position = Vector3d.Zero.cpy();
    Vector3d rotation = Vector3d.Zero.cpy();
    Vector3d scale = Vector3d.One.cpy();
    boolean visible = true;
    Quat4d color = Quat4d.One.cpy();

    public List<ModelRenderer> boxes = Lists.newArrayList();

    @Override
    public Instance instantiate(ModelInstance model) {
        Instance bone = null;
        if (parentName.equals("none") && name.endsWith("_c")) {
            String orinName = name.substring(0, name.length() - 2);
            IModelBone parent = model.getBone(orinName);
            if (parent != null) {
                parentName = orinName;
                bone = new Instance(name, model, parent, this);
            }
        }

        if (bone == null) {
            IModelBone parent = model.getBone(parentName);
            if (parent == null) {
                parentName = "none";
                parent = model.getBone(parentName);
            }

            bone = new Instance(name, model, parent, this);
        }

        bone.position = position.cpy();
        ModelBone parentInfo = null;
        if (bone.parent instanceof Instance) {
            parentInfo = ((Instance) bone.parent).modelBone;
            bone.position.sub(parentInfo.position);
        } else bone.position.sub(0, 24, 0);

        bone.position.scl(1, -1, 1).scl(0.0625f);
        if (parentInfo == null)
            bone.position.sub(bone.parent.getPosition());

        bone.rotation = new Vector3d(rotation.y, rotation.x, rotation.z);
        bone.scale = scale.cpy();
        bone.visible = visible;
        bone.color = color.cpy();

        return bone;
    }

    public static class Instance extends ModelElement.Instance implements IModelBone, ModelElement.IParented, IComponent {
        final IModelBone parent;

        public Vector3d position;
        public Vector3d rotation;
        public Vector3d scale;
        public boolean visible;
        public Quat4d color;

        boolean hierarchyVisible;

        public final ModelBone modelBone;

        public boolean physicalized = false;
        public double elasticity, stiffness, damping, friction, gravity;
        public Vector3d velocity = Vector3d.Zero.cpy();
        public boolean calculateTransform = false;

        public Instance(String name, ModelInstance model, IModelBone parent, ModelBone modelBone) {
            super(name, model);
            this.parent = parent;
            this.modelBone = modelBone;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public IModelBone getParent() {
            return parent;
        }

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

        @Override
        public void update() {
            hierarchyVisible = parent.isVisible() && visible;
        }

        @Override
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
                float red, float green, float blue, float alpha) {
            if (modelBone != null) {
                for (ModelRenderer modelRenderer : modelBone.boxes) {
                    modelRenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red * (float) color.x,
                            green * (float) color.y, blue * (float) color.z, alpha * (float) color.w);
                }
            }
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
    }
}
