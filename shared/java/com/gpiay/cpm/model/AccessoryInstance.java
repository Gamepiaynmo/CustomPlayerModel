package com.gpiay.cpm.model;

import com.google.common.collect.Maps;
import com.gpiay.cpm.model.element.IModelBone;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.LivingEntity;

import java.util.List;
import java.util.Map;

public class AccessoryInstance extends ModelBase {
    final Map<EnumAttachment, List<ModelBone.Instance>> addons = Maps.newEnumMap(EnumAttachment.class);
    final Map<ModelBone.Instance, ModelBone.Instance> parentMap = Maps.newHashMap();

    public AccessoryInstance(ModelPack modelPack, LivingEntity entity) {
        super(modelPack, entity);
    }

    @Override
    public boolean isPhysicsEnabled() {
        return attach.getModel() != null && attach.getModel().physicsEnabled;
    }
    @Override
    public void enablePhysics() {}

    @Override
    public void initMatrix() {
        boolean physicsEnabled = isPhysicsEnabled();
        for (EnumAttachment attachment : addons.keySet()) {
            List<ModelBone.Instance> boneList = attach.getModel().getAttachments(attachment);
            if (boneList.isEmpty()) continue;

            ModelBone.Instance parent = boneList.get(0);
            Matrix4d matrix = attach.getModel().getBoneCurrentMatrix(parent);
            for (ModelBone.Instance bone : addons.get(attachment)) {
                bone.parent = parent;
                transform.put(bone, new MatrixGroup(physicsEnabled ? bone.getTransform().mulLeft(matrix) : new Matrix4d()));
                parentMap.put(bone, parent);
            }
        }

        for (ModelBone.Instance bone : boneList)
            if (!parentMap.containsKey(bone))
                transform.put(bone, new MatrixGroup(physicsEnabled ? bone.getTransform().mulLeft(transform.get(bone.getParent()).partial) : new Matrix4d()));
    }

    @Override
    public IModelBone getBone(String boneId) {
        IModelBone bone = super.getBone(boneId);
        if (bone == null)
            bone = attach.getModel().skeleton.getBone(boneId);
        return bone;
    }

    protected Matrix4d getParentPartialTransform(ModelBone.Instance bone) {
        ModelBone.Instance parent = parentMap.get(bone);
        if (parent != null)
            return attach.getModel().getBoneMatrix(parent);
        else return transform.get(bone.getParent()).partial;
    }

    protected Matrix4d getParentCurrentTransform(ModelBone.Instance bone) {
        ModelBone.Instance parent = parentMap.get(bone);
        if (parent != null)
            return attach.getModel().getBoneCurrentMatrix(parent);
        else return transform.get(bone.getParent()).current;
    }

    public void tick(double scale) {
        super.tick(scale, calculateYaw());
    }

    @Override
    public void update(MatrixStack matrixStackIn, float animPos, float animSpeed, float age, float headYaw,
            float headPitch, float partial, double scale) {
        if (transform.isEmpty())
            return;

        calcModelViewMatrix(partial, matrixStackIn);
        super.update(matrixStackIn, animPos, animSpeed, age, headYaw, headPitch, partial, scale);
    }

    @Override
    public void setupBoneTransform(MatrixStack matrixStack) {
        attach.getModel().setupBoneTransform(matrixStack);
    }
}
