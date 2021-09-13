package com.gpiay.cpm.model.skeleton;

import com.google.common.collect.Maps;
import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelBase;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.BlankBone;
import com.gpiay.cpm.model.element.IModelBone;
import com.gpiay.cpm.model.element.ModelBone;
import com.gpiay.cpm.model.element.VanillaBone;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class Skeleton<M extends EntityModel<LivingEntity>> {
    protected final M entityModel;
    private final Map<String, VanillaBone> vanillaBones = Maps.newHashMap();
    protected final VanillaBone none;

    public Skeleton(M model) {
        this.entityModel = model;
        none = new BlankBone();
        vanillaBones.put("none", none);
    }

    protected final VanillaBone registerVanillaBone(String boneName, ModelRenderer box) {
        VanillaBone bone = new VanillaBone(boneName, box);
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
    public void tick(LivingEntity entity) {
    }

    protected void adjustBones(LivingEntity entity, double scale, float partialTicks) {
        for (VanillaBone bone : vanillaBones.values())
            bone.setScale(scale).offset(0, 24 * (1 - scale), 0);
    }

    public List<String> getFirstPersonBones(HandSide hand) { return Collections.emptyList(); }

    public void setupModelAnim(LivingEntity entity, float animPos, float animSpeed, float age, float headYaw, float headPitch) {
        LivingRenderer<LivingEntity, ?> renderer = (LivingRenderer<LivingEntity, ?>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        renderer.getModel().setupAnim(entity, animPos, animSpeed, age, headYaw, headPitch);
    }

    public void update(LivingEntity entity, float animPos, float animSpeed, float age, float headYaw,
                       float headPitch, float partial, double scale, boolean firstPerson) {
        entityModel.attackTime = entity.getAttackAnim(partial);
#if FORGE
        entityModel.riding = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
#elif FABRIC
        entityModel.riding = entity.isPassenger();
#endif
        entityModel.young = entity.isBaby();

        entityModel.prepareMobModel(entity, animPos, animSpeed, partial);
        entityModel.setupAnim(entity, animPos, animSpeed, age, headYaw, headPitch);

        adjustBones(entity, scale, partial);
    }

    protected final void addBuiltinAttachment(EnumAttachment attachment, ModelInstance instance,
                                              String parent, String id, Vector3d position, Vector3d rotation, Vector3d scale) {
        ModelBone.Instance boneInstance = new ModelBone.Instance(id, instance, getBone(parent), null);
        boneInstance.position = position.scl(0.0625f).scl(1, -1, 1);
        boneInstance.rotation = new Vector3d(rotation.y, rotation.x, rotation.z);
        boneInstance.scale = scale;
        boneInstance.visible = true;
        boneInstance.setCalculateTransform();
        instance.addAttachment(attachment, boneInstance);
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
