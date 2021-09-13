package com.gpiay.cpm.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.model.animation.ScriptEntity;
import com.gpiay.cpm.model.animation.ScriptModel;
import com.gpiay.cpm.model.element.*;
import com.gpiay.cpm.model.skeleton.Skeleton;
import com.gpiay.cpm.network.packet.EyePositionPacket;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.util.exception.TranslatableException;
import com.gpiay.cpm.util.math.Matrix3d;
import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.script.ScriptContext;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ModelInstance extends ModelBase {
    Skeleton<? extends EntityModel<LivingEntity>> skeleton;
    final Map<EnumAttachment, List<ModelBone.Instance>> attachments = Maps.newEnumMap(EnumAttachment.class);
    final Map<HandSide, Set<ModelBone.Instance>> firstPersonBones = Maps.newEnumMap(HandSide.class);
    public static boolean isRenderingFirstPerson = false;
    ModelBone.Instance eyePositionBone = null;
    List<ModelBone.Instance> stuckBones;
    boolean physicsEnabled = false;

    ModelInstance(ModelPack modelPack, LivingEntity entity) {
        super(modelPack, entity);
    }

    public void allocateAttachment(EnumAttachment attachment) {
        attachments.computeIfAbsent(attachment, k -> Lists.newArrayList());
    }

    public void addAttachment(EnumAttachment attachment, ModelBone.Instance bone) {
        attachments.computeIfAbsent(attachment, k -> Lists.newArrayList()).add(bone);
    }

    public List<ModelBone.Instance> getAttachments(EnumAttachment attachment) {
        return attachments.get(attachment);
    }

    @Override
    public void initMatrix() {
        if (physicsEnabled) {
            skeleton.update(entity, entity.animationPosition, entity.animationSpeed, entity.tickCount + 1,
                    calculateYaw(), entity.xRot, 1, attach.getScale(), false);
            updateModelViewMatrix();
        }

        for (IModelBone bone : skeleton.getBones())
            transform.put(bone, new MatrixGroup(physicsEnabled ? bone.getTransform().mulLeft(modelViewMatrix) : new Matrix4d()));

        super.initMatrix();
    }

    @Override
    public IModelBone getBone(String boneId) {
        IModelBone bone = super.getBone(boneId);
        if (bone == null)
            bone = skeleton.getBone(boneId);
        return bone;
    }

    @Override
    public boolean isPhysicsEnabled() {
        return physicsEnabled;
    }
    @Override
    public void enablePhysics() {
        physicsEnabled = true;
    }

    protected Matrix4d getParentPartialTransform(ModelBone.Instance bone) {
        return transform.get(bone.getParent()).partial;
    }

    protected Matrix4d getParentCurrentTransform(ModelBone.Instance bone) {
        return transform.get(bone.getParent()).current;
    }

    private void updateModelViewMatrix() {
        boolean success = true;
        try {
            EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            MatrixStack matrixstack = new MatrixStack();
            ((IMatrixTransformer) renderer).transform(entity, matrixstack, 1);
            calcModelViewMatrix(matrixstack);
        } catch (Exception e) {
            success = false;
        }

        if (!success) {
            CPMMod.LOGGER.warn("Calculate transformation failed. Physics features are disabled for {}.", entity);
            physicsEnabled = false;
        }
    }

    public void tick(double scale) {
        skeleton.tick(entity);
        float netHeadYaw = calculateYaw();

        if (physicsEnabled) {
            skeleton.update(entity, entity.animationPosition, entity.animationSpeed, entity.tickCount + 1,
                    netHeadYaw, entity.xRot, 1, scale, false);
            updateModelViewMatrix();

            for (IModelBone bone : skeleton.getBones()) {
                MatrixGroup matrixGroup = transform.get(bone);
                matrixGroup.current = bone.getTransform().mulLeft(modelViewMatrix);
            }
        }

        super.tick(scale, netHeadYaw);

        if (CPMConfig.customEyePosition() && CPMMod.cpmClient.isServerModded) {
            if (entity instanceof PlayerEntity && eyePositionBone != null) {
                Vector3d eyePosition = transform.get(eyePositionBone).current.getTranslation(new Vector3d());
                NetworkHandler.send(new EyePositionPacket(eyePosition.x, eyePosition.y + 1.501, eyePosition.z));
            }
        }
    }

    @Override
    public void update(MatrixStack matrixStackIn, float animPos, float animSpeed, float age, float headYaw,
            float headPitch, float partial, double scale) {
        if (transform.isEmpty())
            return;

        calcModelViewMatrix(partial, matrixStackIn);
        skeleton.update(entity, animPos, animSpeed, age, headYaw, headPitch, partial, scale, isRenderingFirstPerson);

        for (IModelBone bone : skeleton.getBones())
            transform.get(bone).partial = bone.getTransform().mulLeft(modelViewMatrix);
        super.update(matrixStackIn, animPos, animSpeed, age, headYaw, headPitch, partial, scale);
    }

    public Matrix4d getInvModelViewMatrix() { return invModelViewMatrix; }

    public void setupBoneTransform(MatrixStack matrixStack) {
        setupBoneTransform(matrixStack, invModelViewMatrix);
    }

    public void renderFirstPerson(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, HandSide hand) {
        if (transform.isEmpty())
            return;

        isRenderingFirstPerson = true;
        this.update(matrixStackIn, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0);
        this.doRender(matrixStackIn, bufferIn, packedLightIn, firstPersonBones.get(hand));
        isRenderingFirstPerson = false;
    }

    public ModelBone.Instance getRandomBone(Random random) {
        return stuckBones.get(random.nextInt(stuckBones.size()));
    }

    public Vector3d getEyePosition(float partial) {
        if (entity instanceof PlayerEntity && eyePositionBone != null) {
            Vector3d eyePosition;
            if (partial == 1.0f) {
                eyePosition = transform.get(eyePositionBone).current.getTranslation(new Vector3d());
            } else {
                MatrixGroup matrixGroup = transform.get(eyePositionBone);
                eyePosition = matrixGroup.last.cpy().lerp(matrixGroup.current, partial).getTranslation(new Vector3d());
            }

            eyePosition.y += 1.501;
            return eyePosition;
        }

        return null;
    }
}
