package com.gpiay.cpm.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.hook.LivingRendererHook;
import com.gpiay.cpm.model.animation.ScriptEntity;
import com.gpiay.cpm.model.animation.ScriptModel;
import com.gpiay.cpm.model.element.*;
import com.gpiay.cpm.model.skeleton.Skeleton;
import com.gpiay.cpm.util.exception.TranslatableException;
import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.script.ScriptContext;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelInstance {
    final ModelPack modelPack;
    public final LivingEntity entity;
    public boolean renderInvisible = false;
    boolean physicsEnabled = false;

    Skeleton<? extends EntityModel<LivingEntity>> skeleton;

    final Map<String, ModelBone> boneMap = Maps.newHashMap();
    final List<ModelBone> boneList = Lists.newArrayList();
    final Map<String, ModelPartInstance> partMap = Maps.newHashMap();

    final Map<EnumAttachment, List<ModelBone>> attachments = Maps.newEnumMap(EnumAttachment.class);
    final Map<String, ParticleEmitterInstance> particleEmitters = Maps.newHashMap();
    final Map<String, ItemModelInstance> itemModels = Maps.newHashMap();

    final Map<HandSide, Set<ModelBone>> firstPersonBones = Maps.newEnumMap(HandSide.class);
    public boolean isRenderingFirstPerson = false;

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

    public void addAttachment(EnumAttachment attachment, ModelBone bone) {
        attachments.computeIfAbsent(attachment, k -> Lists.newArrayList()).add(bone);
    }

    public List<ModelBone> getAttachments(EnumAttachment attachment) {
        return attachments.get(attachment);
    }

    public ParticleEmitterInstance getParticle(String particleName) { return particleEmitters.get(particleName); }

    public ItemModelInstance getItemModel(String itemName) { return itemModels.get(itemName); }

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
        boolean shouldSit = entity.isPassenger() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        float f2 = entity.rotationYawHead - entity.renderYawOffset;
        if (shouldSit && entity.getRidingEntity() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) entity.getRidingEntity();
            float offset = livingentity.renderYawOffset;
            f2 = entity.rotationYawHead - offset;
            float f3 = MathHelper.wrapDegrees(f2);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            offset = entity.rotationYawHead - f3;
            if (f3 * f3 > 2500.0F) {
                offset += f3 * 0.2F;
            }

            f2 = entity.rotationYawHead - offset;
        }

        return f2;
    }

    public void tick(double scale) {
        skeleton.tick(entity);

        if (physicsEnabled) {
            float netHeadYaw = calculateYaw();
            skeleton.update(entity, entity.limbSwing, entity.limbSwingAmount, entity.ticksExisted + 1,
                    netHeadYaw, entity.rotationPitch, 0.0625f, 1, scale, false);
            evaluateAnimation(tickFunc, entity.limbSwing, entity.limbSwingAmount, entity.ticksExisted + 1,
                    netHeadYaw, entity.rotationPitch, 0.0625f, 1, scale);

            boolean success = true;
            try {
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.loadIdentity();
                baseModelView.idt();

                LivingRendererHook.enableHook(() -> this.calcModelViewMatrix(1));
                EntityRenderer<LivingEntity> renderer = Minecraft.getInstance().getRenderManager().getRenderer(entity);
                if (renderer != null) {
                    renderer.setRenderOutlines(true);
                    renderer.doRender(entity, entity.posX, entity.posY, entity.posZ, entity.rotationYaw, 1);
                    GlStateManager.activeTexture(GLX.GL_TEXTURE1);
                    GlStateManager.disableTexture();
                    GlStateManager.activeTexture(GLX.GL_TEXTURE0);
                }

                if (LivingRendererHook.isHookEnabled())
                    success = false;
            } catch (Exception e) {
                success = false;
            }

            if (!success) {
                CPMMod.LOGGER.error("Entity rendering simulation failed. Physics features are now disabled.");
                physicsEnabled = false;
                return;
            }

            if (transform.isEmpty())
                initMatrix();

            double dx = entity.posX - entity.prevPosX;
            double dy = entity.posY - entity.prevPosY;
            double dz = entity.posZ - entity.prevPosZ;
            Vector3d motion = new Vector3d(-dx, -dy, -dz);
            Matrix4d motionTrans = new Matrix4d().setTranslation(motion);

            for (IModelBone bone : skeleton.getBones()) {
                MatrixGroup matrixGroup = transform.get(bone);
                matrixGroup.current = bone.getTransform().mulLeft(modelViewMatrix);
            }

            for (ModelBone bone : boneList) {
                if (bone.calculateTransform) {
                    MatrixGroup matrixGroup = transform.get(bone);

                    if (bone.physicalized) {
                        matrixGroup.last = matrixGroup.current.mulLeft(motionTrans);
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

        for (ParticleEmitterInstance particle : particleEmitters.values())
            particle.tick(transform.get(particle.bone).current);
    }

    public void update(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, float partialTicks, double scale) {
        if (transform.isEmpty())
            return;

        calcModelViewMatrix(partialTicks);
        skeleton.update(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, partialTicks, scale, isRenderingFirstPerson);
        evaluateAnimation(updateFunc, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, partialTicks, scale);

        for (IModelBone bone : skeleton.getBones())
            transform.get(bone).partial = bone.getTransform().mulLeft(modelViewMatrix);
        for (ModelBone bone : boneList) {
            bone.update();
            if (physicsEnabled && !isRenderingInventory && bone.physicalized) {
                MatrixGroup matrixGroup = transform.get(bone);
                matrixGroup.partial = matrixGroup.last.cpy().lerp(matrixGroup.current, partialTicks);
            } else {
                transform.get(bone).partial = bone.getTransform().mulLeft(transform.get(bone.getParent()).partial);
            }
        }
    }

    public void evaluateAnimation(JSObject func) {
        evaluateAnimation(func, entity.limbSwing, entity.limbSwingAmount, entity.ticksExisted + 1,
                calculateYaw(), entity.rotationPitch, 0.0625f, 1, 1.0);
    }

    public void evaluateAnimation(JSObject func, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, float partialTicks, double scale) {
        if (func != null) {
            ScriptEntity entity = new ScriptEntity(this.entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch, scaleFactor, partialTicks, scale);
            ScriptModel model = new ScriptModel(this);
            try {
                func.call(null, entity, model);
            } catch (Exception e) {
                CPMMod.warn(new TranslatableException("error.cpm.script.eval", e, modelPack.name));
            }
        }
    }

    private void calcModelViewMatrix(float partialTicks) {
        modelViewMatrix = new Matrix4d(GlStateManager.getMatrix4f(GL11.GL_MODELVIEW_MATRIX));
        modelViewMatrix.mulLeft(baseModelView);
        double x = (partialTicks - 1) * (entity.posX - entity.prevPosX);
        double y = (partialTicks - 1) * (entity.posY - entity.prevPosY);
        double z = (partialTicks - 1) * (entity.posZ - entity.prevPosZ);
        modelViewMatrix.setTranslation(x, y, z);
        invModelViewMatrix = modelViewMatrix.cpy().inv();
    }

    public Matrix4d getInvModelViewMatrix() { return invModelViewMatrix; }
    public Matrix4d getBoneMatrix(IModelBone bone) { return transform.get(bone).partial; }

    public boolean isRenderingInvisible() { return renderInvisible; }

    public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                       float headPitch, float scaleFactor, float partialTicks, double scale) {
        if (transform.isEmpty())
            return;

        this.update(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, partialTicks, scale);
        this.doRender(null);
    }

    public void renderFirstPerson(HandSide hand) {
        if (transform.isEmpty())
            return;

        isRenderingFirstPerson = true;
        this.update(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f, 1.0f, 1.0);
        this.doRender(firstPersonBones.get(hand));
        isRenderingFirstPerson = false;
    }

    private void doRender(Set<ModelBone> bones) {
        boolean visible = !entity.isInvisible() || Minecraft.getInstance().getRenderManager().renderOutlines;
        renderInvisible = !visible && !entity.isInvisibleToPlayer(Minecraft.getInstance().player);

        GlStateManager.pushMatrix();
        GL11.glMultMatrixd(invModelViewMatrix.val);
        TextureManager textureManager = Minecraft.getInstance().textureManager;

        for (ModelPartInstance part : partMap.values()) {
            textureManager.bindTexture(modelPack.textures.get(part.texture));
            for (ModelBone bone : part.bones) {
                if ((bones == null || bones.contains(bone)) && bone.isVisible()) {
                    GlStateManager.pushMatrix();
                    GL11.glMultMatrixd(transform.get(bone).partial.val);
                    bone.render();
                    GlStateManager.popMatrix();
                }
            }
        }

        for (ItemModelInstance itemModel : itemModels.values()) {
            GlStateManager.pushMatrix();
            GL11.glMultMatrixd(transform.get(itemModel.bone).partial.val);
            itemModel.render();
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    public void release() {
        modelPack.refCnt--;
    }

    public ModelBone getRandomBone(Random random) {
        List<ModelBone> bones = boneList.stream().filter(bone ->
                bone.isVisible() && bone.boneInfo != null && !bone.boneInfo.boxes.isEmpty()).collect(Collectors.toList());
        if (bones.isEmpty())
            return null;
        return bones.get(random.nextInt(bones.size()));
    }

    public String getTexture(String partName) {
        return partMap.containsKey(partName) ? partMap.get(partName).texture : "";
    }

    public void setTexture(String partName, String textureName) {
        if (partMap.containsKey(partName) && modelPack.textures.containsKey(textureName)) {
            partMap.get(partName).texture = textureName;
        }
    }
}
