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
import com.gpiay.cpm.util.math.Matrix3d;
import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.script.ScriptContext;
import java.util.*;
import java.util.stream.Collectors;

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
        boolean shouldSit = entity.isPassenger() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        float f2 = entity.rotationYawHead - entity.renderYawOffset;
        if (shouldSit && entity.getRidingEntity() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity.getRidingEntity();
            f2 = entity.rotationYawHead - livingentity.renderYawOffset;
            float f3 = MathHelper.wrapDegrees(f2);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            float f = entity.rotationYawHead - f3;
            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            f2 = entity.rotationYawHead - f;
        }

        return f2;
    }

    public void tick(double scale) {
        skeleton.tick(entity);

        if (physicsEnabled) {
            float netHeadYaw = calculateYaw();
            skeleton.update(entity, entity.limbSwing, entity.limbSwingAmount, entity.ticksExisted + 1,
                    netHeadYaw, entity.rotationPitch, 1, scale, false);
            evaluateAnimation(updateFunc, entity.limbSwing, entity.limbSwingAmount, entity.ticksExisted + 1,
                    netHeadYaw, entity.rotationPitch, 1, scale);
            evaluateAnimation(tickFunc, entity.limbSwing, entity.limbSwingAmount, entity.ticksExisted + 1,
                    netHeadYaw, entity.rotationPitch, 1, scale);

            boolean success = true;
            try {
                baseModelView.idt();

                LivingRendererHook.enableHook(matrixStack -> this.calcModelViewMatrix(1, matrixStack));
                EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getRenderManager().getRenderer(entity);

                MatrixStack matrixstack = new MatrixStack();
                IRenderTypeBuffer.Impl bufferIn = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
                renderer.render(entity, entity.rotationYaw, 1, matrixstack, bufferIn, 15728880);
                bufferIn.finish();

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

            double dx = entity.getPosX() - entity.prevPosX;
            double dy = entity.getPosY() - entity.prevPosY;
            double dz = entity.getPosZ() - entity.prevPosZ;
            Vector3d motion = new Vector3d(-dx, -dy, -dz);
            Matrix4d motionTrans = new Matrix4d().setTranslation(motion);

            for (IModelBone bone : skeleton.getBones()) {
                MatrixGroup matrixGroup = transform.get(bone);
                matrixGroup.current = bone.getTransform().mulLeft(modelViewMatrix);
            }

            for (ModelBone.Instance bone : boneList) {
                if (bone.calculateTransform) {
                    MatrixGroup matrixGroup = transform.get(bone);

                    if (bone.physicalized) {
                        matrixGroup.last = matrixGroup.current.mulLeft(motionTrans);
                        matrixGroup.current = bone.getTransform().mulLeft(transform.get(bone.getParent()).current);
                        double curScale = matrixGroup.current.getScale(new Vector3d()).len() / Math.sqrt(3);

                        double sample = matrixGroup.last.val[Matrix4d.M03];
                        if (Double.isInfinite(sample) || Double.isNaN(sample))
                            matrixGroup.last.set(matrixGroup.current);

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
    }

    public void update(MatrixStack matrixStackIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float partialTicks, double scale) {
        if (transform.isEmpty())
            return;

        calcModelViewMatrix(partialTicks, matrixStackIn);
        skeleton.update(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, scale, isRenderingFirstPerson);
        evaluateAnimation(updateFunc, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, scale);

        for (IModelBone bone : skeleton.getBones())
            transform.get(bone).partial = bone.getTransform().mulLeft(modelViewMatrix);
        for (ModelBone.Instance bone : boneList) {
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
                calculateYaw(), entity.rotationPitch, 1, 1.0);
    }

    public void evaluateAnimation(JSObject func, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float partialTicks, double scale) {
        if (func != null) {
            ScriptEntity entity = new ScriptEntity(this.entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch, partialTicks, scale);
            ScriptModel model = new ScriptModel(this);
            try {
                func.call(null, entity, model);
            } catch (Exception e) {
                CPMMod.warn(new TranslatableException("error.cpm.script.eval", e, modelPack.name));
            }
        }
    }

    private void calcModelViewMatrix(float partialTicks, MatrixStack matrixStack) {
        modelViewMatrix = new Matrix4d(matrixStack.getLast().getMatrix());
        modelViewMatrix.mulLeft(baseModelView);
        double x = (partialTicks - 1) * (entity.getPosX() - entity.prevPosX);
        double y = (partialTicks - 1) * (entity.getPosY() - entity.prevPosY);
        double z = (partialTicks - 1) * (entity.getPosZ() - entity.prevPosZ);
        modelViewMatrix.setTranslation(x, y, z);
        invModelViewMatrix = modelViewMatrix.cpy().inv();
    }

    public Matrix4d getInvModelViewMatrix() { return invModelViewMatrix; }
    public Matrix4d getBoneMatrix(IModelBone bone) { return transform.get(bone).partial; }
    public Matrix4d getBoneCurrentMatrix(IModelBone bone) { return transform.get(bone).current; }

    public void setupBoneTransform(MatrixStack matrixStack, IModelBone bone) {
        setupBoneTransform(matrixStack, getBoneMatrix(bone));
    }

    public void setupBoneTransform(MatrixStack matrixStack, Matrix4d matrix) {
        matrix.mulLeftTo(matrixStack.getLast().getMatrix());
        new Matrix3d().set(matrix).inv().transpose().mulLeftTo(matrixStack.getLast().getNormal());
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, float limbSwing,
            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, double scale) {
        if (transform.isEmpty())
            return;

        isRenderingInventory = partialTicks == 1;
        this.update(matrixStackIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, scale);
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
        matrixStackIn.push();
        setupBoneTransform(matrixStackIn, invModelViewMatrix);
        boolean visible = !entity.isInvisible();
        boolean renderInvisible = !visible && !entity.isInvisibleToPlayer(Minecraft.getInstance().player);
        boolean glowing = Minecraft.getInstance().isEntityGlowing(entity);
        int packedOverlayIn = LivingRenderer.getPackedOverlay(entity, 0.0F);
        float alpha = renderInvisible ? 0.15F : 1.0F;

        for (ModelPart.Instance part : modelPartMap.values()) {
            ResourceLocation texture = modelPack.getTexture(part.texture);
            RenderType rendertype = renderInvisible ? RenderType.getItemEntityTranslucentCull(texture)
                    : visible ? RenderType.getEntityTranslucent(texture)
                    : glowing ? RenderType.getOutline(texture) : null;
            if (rendertype != null) {
                IVertexBuilder vertexBuilder = bufferIn.getBuffer(rendertype);
                for (ModelBone.Instance bone : part.bones) {
                    if ((bones == null || bones.contains(bone)) && bone.isVisible()) {
                        matrixStackIn.push();
                        setupBoneTransform(matrixStackIn, bone);
                        bone.render(matrixStackIn, vertexBuilder, packedLightIn, packedOverlayIn,
                                1.0F, 1.0F, 1.0F, alpha);
                        matrixStackIn.pop();
                    }
                }
            }
        }

        if (visible || renderInvisible) {
            for (ItemModel.Instance itemModel : itemModels.values()) {
                if ((bones == null || bones.contains(itemModel.parent)) && itemModel.parent.isVisible()) {
                    matrixStackIn.push();
                    setupBoneTransform(matrixStackIn, itemModel.parent);
                    itemModel.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, alpha);
                    matrixStackIn.pop();
                }
            }
        }

        matrixStackIn.pop();
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
}
