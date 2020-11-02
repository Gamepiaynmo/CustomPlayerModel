package com.gpiay.cpm.model.skeleton;

import com.google.common.collect.Maps;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.BlankBone;
import com.gpiay.cpm.model.element.IModelBone;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.model.element.VanillaBone;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class Skeleton<M extends EntityModel<LivingEntity>> {
    protected final M entityModel;
    private final Map<String, VanillaBone> vanillaBones = Maps.newHashMap();
    private int boneCount = 0;
    protected final VanillaBone none;

    public Skeleton(M model) {
        this.entityModel = model;
        none = new BlankBone();
        vanillaBones.put("none", none);
    }

    protected final VanillaBone registerVanillaBone(String boneName) {
        VanillaBone bone = new VanillaBone(boneName, entityModel.boxList.get(boneCount++));
        vanillaBones.put(boneName, bone);
        return bone;
    }

    public final IModelBone getBone(String boneName) {
        return vanillaBones.get(boneName);
    }
    public final Collection<VanillaBone> getBones() { return vanillaBones.values(); }

    public void addAttachments(EnumAttachment attachment, ModelInstance instance) {
        instance.allocateAttachment(attachment);
    }
    public void tick(LivingEntity entity) {};

    protected void adjustBones(LivingEntity entity, double scale, float partialTicks, boolean firstPerson) {
        for (VanillaBone bone : vanillaBones.values())
            bone.setScale(scale).offset(0, 24 * (1 - scale), 0);
    }

    public List<String> getFirstPersonBones(HandSide hand) { return Collections.emptyList(); }

    public void update(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                       float headPitch, float scaleFactor, float partialTicks, double scale, boolean firstPerson) {
        entityModel.swingProgress = entity.getSwingProgress(partialTicks);
        entityModel.isSitting = entity.isPassenger() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        entityModel.isChild = entity.isChild();

        entityModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        entityModel.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

        adjustBones(entity, scale, partialTicks, firstPerson);
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance,
                                              String parent, String id, Vector3d position, Vector3d rotation, Vector3d scale) {
        ModelBone bone = new ModelBone(instance, id, getBone(parent), null);
        bone.position = position.scl(0.0625f).scl(1, -1, 1);
        bone.rotation = new Vector3d(rotation.y, rotation.x, rotation.z);
        bone.scale = scale;
        bone.visible = true;
        bone.setCalculateTransform();
        instance.addAttachment(attachment, bone);
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance,
                                              String parent, Vector3d position, Vector3d rotation, Vector3d scale) {
        addBuiltinAttachment(attachment, instance, parent, "builtin_" + attachment.getId() + "_" + parent,
                position, rotation, scale);
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance,
                                              String parent, Vector3d position, Vector3d rotation) {
        addBuiltinAttachment(attachment, instance, parent, "builtin_" + attachment.getId() + "_" + parent,
                position, rotation, Vector3d.One.cpy());
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance,
                                              String parent, Vector3d position) {
        addBuiltinAttachment(attachment, instance, parent, "builtin_" + attachment.getId() + "_" + parent,
                position, Vector3d.Zero.cpy(), Vector3d.One.cpy());
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance, String parent) {
        addBuiltinAttachment(attachment, instance, parent, "builtin_" + attachment.getId() + "_" + parent,
                Vector3d.Zero.cpy(), Vector3d.Zero.cpy(), Vector3d.One.cpy());
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance,
                                              String parent, String id, Vector3d position, Vector3d rotation) {
        addBuiltinAttachment(attachment, instance, parent, id,
                position, rotation, Vector3d.One.cpy());
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance,
                                              String parent, String id, Vector3d position) {
        addBuiltinAttachment(attachment, instance, parent, id,
                position, Vector3d.Zero.cpy(), Vector3d.One.cpy());
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance,
                                              String parent, String id) {
        addBuiltinAttachment(attachment, instance, parent, id,
                Vector3d.Zero.cpy(), Vector3d.Zero.cpy(), Vector3d.One.cpy());
    }
}
