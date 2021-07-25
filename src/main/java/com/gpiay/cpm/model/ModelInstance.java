package com.gpiay.cpm.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.animation.ScriptEntity;
import com.gpiay.cpm.model.animation.ScriptModel;
import com.gpiay.cpm.model.element.*;
import com.gpiay.cpm.model.skeleton.Skeleton;
import com.gpiay.cpm.network.EyePositionPacket;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.server.ServerConfig;
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
import java.util.*;

public class ModelInstance {
    final ModelPack modelPack;
    public final LivingEntity entity;
    boolean physicsEnabled = false;

    Skeleton<? extends EntityModel<LivingEntity>> skeleton;

    final Map<String, ModelBone.Instance> boneMap = Maps.newHashMap();
    final List<ModelBone.Instance> boneList = Lists.newArrayList();
    final Map<String, ModelPart.Instance> modelPartMap = Maps.newHashMap();

    final Map<EnumAttachment, List<ModelBone.Instance>> attachments = Maps.newEnumMap(EnumAttachment.class);
    final Map<String, ParticleEmitter.Instance> particleEmitters = Maps.newHashMap();
    final Map<String, ItemModel.Instance> itemModels = Maps.newHashMap();

    final Map<HandSide, Set<ModelBone.Instance>> firstPersonBones = Maps.newEnumMap(HandSide.class);
    public boolean isRenderingFirstPerson = false;
    ModelBone.Instance eyePositionBone = null;

    List<ModelBone.Instance> stuckBones;

    ScriptContext scriptContext;
    JSObject initFunc, updateFunc, tickFunc;

    private static class MatrixGroup {
        Matrix4d last, current, partial;
        public MatrixGroup(Matrix4d initial) {
            last = initial.cpy();
            current = initial.cpy();
            partial = initial.cpy();
        }
    }
    final Map<IModelBone, MatrixGroup> transform = Maps.newHashMap();

    Matrix4d modelViewMatrix = new Matrix4d(), invModelViewMatrix = new Matrix4d();
    public static Matrix4d baseModelView = new Matrix4d();

    public static boolean isRenderingInventory;

    ModelInstance(ModelPack modelPack, LivingEntity entity) {
        this.modelPack = modelPack;
        this.entity = entity;
    }

    public boolean isReady() { return !transform.isEmpty(); }

    public ModelPack getModelPack() {
        return modelPack;
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

    public ParticleEmitter.Instance getParticle(String particleName) { return particleEmitters.get(particleName); }

    public ItemModel.Instance getItemModel(String itemName) { return itemModels.get(itemName); }

    void initMatrix() {
        for (IModelBone bone : skeleton.getBones())
            transform.put(bone, new MatrixGroup(bone.getTransform().mulLeft(modelViewMatrix)));
        for (IModelBone bone : boneList)
            transform.put(bone, new MatrixGroup(bone.getTransform().mulLeft(transform.get(bone.getParent()).partial)));
    }

    public IModelBone getBone(String boneId) {
        IModelBone bone = boneMap.get(boneId);
        if (bone == null)
            bone = skeleton.getBone(boneId);
        return bone;
    }

    public void enablePhysics() {
        physicsEnabled = true;
    }

    private float calculateYaw() {
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        float f2 = entity.yHeadRot - entity.yBodyRot;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity.getVehicle();
            f2 = entity.yHeadRot - livingentity.yBodyRot;
            float f3 = MathHelper.wrapDegrees(f2);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            float f = entity.yHeadRot - f3;
            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            f2 = entity.yHeadRot - f;
        }

        return f2;
    }

    public void tick(double scale) {
        skeleton.tick(entity);

        if (physicsEnabled) {
            float netHeadYaw = calculateYaw();
            skeleton.update(entity, entity.animationSpeed, entity.animationPosition, entity.tickCount + 1,
                    netHeadYaw, entity.xRot, 1, scale, false);
            evaluateAnimation(updateFunc, entity.animationSpeed, entity.animationPosition, entity.tickCount + 1,
                    netHeadYaw, entity.xRot, 1, scale);
            evaluateAnimation(tickFunc, entity.animationSpeed, entity.animationPosition, entity.tickCount + 1,
                    netHeadYaw, entity.xRot, 1, scale);

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
                return;
            }

            if (transform.isEmpty())
                initMatrix();

            double dx = entity.getX() - entity.xo;
            double dy = entity.getY() - entity.yo;
            double dz = entity.getZ() - entity.zo;
            Vector3d motion = new Vector3d(-dx, -dy, -dz);
            Matrix4d motionTrans = new Matrix4d().setTranslation(motion);

            for (IModelBone bone : skeleton.getBones()) {
                MatrixGroup matrixGroup = transform.get(bone);
                matrixGroup.current = bone.getTransform().mulLeft(modelViewMatrix);
            }

            for (ModelBone.Instance bone : boneList) {
                if (bone.calculateTransform) {
                    MatrixGroup matrixGroup = transform.get(bone);
                    matrixGroup.last = matrixGroup.current.mulLeft(motionTrans);

                    if (bone.physicalized) {
                        matrixGroup.current = bone.getTransform().mulLeft(transform.get(bone.getParent()).current);
                        double curScale = matrixGroup.current.getScale(new Vector3d()).len() / Math.sqrt(3);

                        Vector3d lastEnd = new Vector3d(0, 0, 1).mul(matrixGroup.last);
                        Vector3d curStart = matrixGroup.current.getTranslation(new Vector3d());
                        Vector3d curTarget = new Vector3d(0, 0, 1).mul(matrixGroup.current);

                        Vector3d tempCurEnd = lastEnd.add(bone.velocity);
                        Vector3d curEnd = tempCurEnd.cpy().sub(curStart).setLength(curScale).add(curStart);

                        bone.velocity.add(curEnd.cpy().sub(tempCurEnd).scl(bone.elasticity / 16));
                        bone.velocity.add(curTarget.sub(curEnd).scl(bone.stiffness / 16));
                        bone.velocity.add(motion.cpy().scl(curScale * bone.friction / 16));
                        bone.velocity.y -= curScale * bone.gravity / 16;
                        bone.velocity.scl(bone.damping);

                        Vector3d upVec = new Vector3d(0, 1, 0).mul(matrixGroup.current).sub(curStart);
                        matrixGroup.current = new Matrix4d().setToLookAt(curStart, curEnd, upVec).inv()
                                .rotate(0, 1, 0, 180)
                                .scale(curScale, curScale, curScale);
                    } else {
                        matrixGroup.current = bone.getTransform().mulLeft(transform.get(bone.getParent()).current);
                    }
                }
            }
        }

        if (!physicsEnabled && transform.isEmpty()) {
            for (IModelBone bone : skeleton.getBones())
                transform.put(bone, new MatrixGroup(new Matrix4d()));
            for (IModelBone bone : boneList)
                transform.put(bone, new MatrixGroup(new Matrix4d()));
        }

        for (ParticleEmitter.Instance particle : particleEmitters.values())
            particle.update();

        if (ServerConfig.CUSTOM_EYE_POSITION.get()) {
            if (entity instanceof PlayerEntity && eyePositionBone != null) {
                Vector3d eyePosition = transform.get(eyePositionBone).current.getTranslation(new Vector3d());
                net.minecraft.util.math.vector.Vector3d pos = new net.minecraft.util.math.vector.Vector3d(eyePosition.x, eyePosition.y + 1.501, eyePosition.z);
                Networking.INSTANCE.sendToServer(new EyePositionPacket(pos));
            }
        }
    }

    public void update(MatrixStack matrixStackIn, float animPos, float animSpeed, float age, float headYaw,
            float headPitch, float partial, double scale) {
        if (transform.isEmpty())
            return;

        calcModelViewMatrix(partial, matrixStackIn);
        skeleton.update(entity, animPos, animSpeed, age, headYaw, headPitch, partial, scale, isRenderingFirstPerson);
        evaluateAnimation(updateFunc, animPos, animSpeed, age, headYaw, headPitch, partial, scale);

        for (IModelBone bone : skeleton.getBones())
            transform.get(bone).partial = bone.getTransform().mulLeft(modelViewMatrix);
        for (ModelBone.Instance bone : boneList) {
            bone.update();
            if (physicsEnabled && !isRenderingInventory && bone.physicalized) {
                MatrixGroup matrixGroup = transform.get(bone);
                matrixGroup.partial = matrixGroup.last.cpy().lerp(matrixGroup.current, partial);
            } else {
                transform.get(bone).partial = bone.getTransform().mulLeft(transform.get(bone.getParent()).partial);
            }
        }
    }

    public void evaluateAnimation(JSObject func) {
        evaluateAnimation(func, entity.attackAnim, entity.oAttackAnim, entity.tickCount + 1,
                calculateYaw(), entity.xRot, 1, 1.0);
    }

    public void evaluateAnimation(JSObject func, float animPos, float animSpeed, float age, float headYaw,
                                  float headPitch, float partial, double scale) {
        if (func != null) {
            ScriptEntity entity = new ScriptEntity(this.entity, animPos, animSpeed, age, headYaw,
                    headPitch, partial, scale);
            ScriptModel model = new ScriptModel(this);
            try {
                func.call(null, entity, model);
            } catch (Exception e) {
                CPMMod.warn(new TranslatableException("error.cpm.script.eval", e, modelPack.name));
            }
        }
    }

    private void calcModelViewMatrix(MatrixStack matrixStack) {
        modelViewMatrix = new Matrix4d(matrixStack.last().pose());
        modelViewMatrix.setTranslation(0, 0, 0);
        invModelViewMatrix = modelViewMatrix.cpy().inv();
    }

    private void calcModelViewMatrix(float partialTicks, MatrixStack matrixStack) {
        modelViewMatrix = new Matrix4d(matrixStack.last().pose());
        modelViewMatrix.mulLeft(baseModelView);
        double x = (partialTicks - 1) * (entity.getX() - entity.xo);
        double y = (partialTicks - 1) * (entity.getY() - entity.yo);
        double z = (partialTicks - 1) * (entity.getZ() - entity.zo);
        modelViewMatrix.setTranslation(x, y, z);
        invModelViewMatrix = modelViewMatrix.cpy().inv();
    }

    public Matrix4d getInvModelViewMatrix() { return invModelViewMatrix; }
    public Matrix4d getBoneMatrix(IModelBone bone) { return transform.get(bone).partial; }
    public Matrix4d getBoneCurrentMatrix(IModelBone bone) { return transform.get(bone).current; }

    public void setupBoneTransform(MatrixStack matrixStack, Matrix4d matrix) {
        matrix.mulLeftTo(matrixStack.last().pose());
        new Matrix3d().set(matrix).inv().transpose().mulLeftTo(matrixStack.last().normal());
    }

    public void setupBoneTransform(MatrixStack matrixStack, IModelBone bone) {
        setupBoneTransform(matrixStack, getBoneMatrix(bone));
    }

    public void setupBoneTransform(MatrixStack matrixStack) {
        setupBoneTransform(matrixStack, invModelViewMatrix);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, float animPos,
            float animSpeed, float age, float headYaw, float headPitch, float partial, double scale) {
        if (transform.isEmpty())
            return;

        isRenderingInventory = partial == 1;
        this.update(matrixStackIn, animPos, animSpeed, age, headYaw, headPitch, partial, scale);
        this.doRender(matrixStackIn, bufferIn, packedLightIn, null);
        isRenderingInventory = false;
    }

    public void renderFirstPerson(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, HandSide hand) {
        if (transform.isEmpty())
            return;

        isRenderingFirstPerson = true;
        this.update(matrixStackIn, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0);
        this.doRender(matrixStackIn, bufferIn, packedLightIn, firstPersonBones.get(hand));
        isRenderingFirstPerson = false;
    }

    private void doRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, Set<ModelBone.Instance> bones) {
        matrixStackIn.pushPose();
        setupBoneTransform(matrixStackIn, invModelViewMatrix);
        boolean visible = !entity.isInvisible();
        boolean renderInvisible = !visible && !entity.isInvisibleTo(Minecraft.getInstance().player);
        boolean glowing = Minecraft.getInstance().shouldEntityAppearGlowing(entity);
        int packedOverlayIn = LivingRenderer.getOverlayCoords(entity, 0.0F);
        float alpha = renderInvisible ? 0.15F : 1.0F;

        for (ModelPart.Instance part : modelPartMap.values()) {
            ResourceLocation texture = modelPack.getTexture(part.texture);
            RenderType rendertype = renderInvisible ? RenderType.itemEntityTranslucentCull(texture)
                    : visible ? RenderType.entityTranslucent(texture)
                    : glowing ? RenderType.outline(texture) : null;
            if (rendertype != null) {
                IVertexBuilder vertexBuilder = bufferIn.getBuffer(rendertype);
                for (ModelBone.Instance bone : part.bones) {
                    if ((bones == null || bones.contains(bone)) && bone.isVisible()) {
                        matrixStackIn.pushPose();
                        setupBoneTransform(matrixStackIn, bone);
                        bone.render(matrixStackIn, vertexBuilder, packedLightIn, packedOverlayIn,
                                1.0F, 1.0F, 1.0F, alpha);
                        matrixStackIn.popPose();
                    }
                }
            }
        }

        if (visible || renderInvisible) {
            for (ItemModel.Instance itemModel : itemModels.values()) {
                if ((bones == null || bones.contains(itemModel.parent)) && itemModel.parent.isVisible()) {
                    matrixStackIn.pushPose();
                    setupBoneTransform(matrixStackIn, itemModel.parent);
                    itemModel.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, alpha);
                    matrixStackIn.popPose();
                }
            }
        }

        matrixStackIn.popPose();
    }

    public void release() {
        for (ParticleEmitter.Instance particle : particleEmitters.values())
            particle.release();
    }

    public ModelBone.Instance getRandomBone(Random random) {
        return stuckBones.get(random.nextInt(stuckBones.size()));
    }

    public String getTexture(String partName) {
        return modelPartMap.containsKey(partName) ? modelPartMap.get(partName).texture : "";
    }

    public void setTexture(String partName, String textureName) {
        if (modelPartMap.containsKey(partName) && modelPack.hasTexture(textureName)) {
            modelPartMap.get(partName).texture = textureName;
        }
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
