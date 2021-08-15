package com.gpiay.cpm.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.model.animation.ScriptEntity;
import com.gpiay.cpm.model.animation.ScriptModel;
import com.gpiay.cpm.model.element.*;
import com.gpiay.cpm.util.exception.TranslatableException;
import com.gpiay.cpm.util.math.Matrix3d;
import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.script.ScriptContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ModelBase {
    final ModelPack modelPack;
    public final LivingEntity entity;
    public final ClientCPMAttachment attach;

    final Map<String, ModelBone.Instance> boneMap = Maps.newHashMap();
    final List<ModelBone.Instance> boneList = Lists.newArrayList();
    final Map<String, ModelPart.Instance> modelPartMap = Maps.newHashMap();

    final Map<String, ParticleEmitter.Instance> particleEmitters = Maps.newHashMap();
    final Map<String, ItemModel.Instance> itemModels = Maps.newHashMap();

    ScriptContext scriptContext;
    JSObject initFunc, updateFunc, tickFunc;

    final Map<IModelBone, MatrixGroup> transform = Maps.newHashMap();

    Matrix4d modelViewMatrix = new Matrix4d(), invModelViewMatrix = new Matrix4d();
    public static Matrix4d baseModelView = new Matrix4d();

    public static boolean isRenderingInventory;

    static class MatrixGroup {
        Matrix4d last, current, partial;
        public MatrixGroup(Matrix4d initial) {
            last = initial.cpy();
            current = initial.cpy();
            partial = initial.cpy();
        }
    }

    public ModelBase(ModelPack modelPack, LivingEntity entity) {
        this.modelPack = modelPack;
        this.entity = entity;
        this.attach = (ClientCPMAttachment) AttachmentProvider.getEntityAttachment(entity).get();
    }

    public boolean isReady() { return !transform.isEmpty(); }

    public ModelPack getModelPack() {
        return modelPack;
    }

    public ParticleEmitter.Instance getParticle(String particleName) { return particleEmitters.get(particleName); }

    public ItemModel.Instance getItemModel(String itemName) { return itemModels.get(itemName); }

    public void initMatrix() {
        boolean physicsEnabled = isPhysicsEnabled();
        for (ModelBone.Instance bone : boneList)
            transform.put(bone, new MatrixGroup(physicsEnabled ? bone.getTransform().mulLeft(getParentPartialTransform(bone)) : new Matrix4d()));
    }

    public IModelBone getBone(String boneId) {
        return boneMap.get(boneId);
    }

    public abstract boolean isPhysicsEnabled();
    public abstract void enablePhysics();

    protected abstract Matrix4d getParentPartialTransform(ModelBone.Instance bone);
    protected abstract Matrix4d getParentCurrentTransform(ModelBone.Instance bone);

    public float calculateYaw() {
#if FORGE
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
#elif FABRIC
        boolean shouldSit = entity.isPassenger();
#endif
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

    protected void tick(double scale, float headYaw) {
        evaluateAnimation(updateFunc, scale, headYaw);
        evaluateAnimation(tickFunc, scale, headYaw);

        if (isPhysicsEnabled()) {
            double dx = entity.getX() - entity.xo;
            double dy = entity.getY() - entity.yo;
            double dz = entity.getZ() - entity.zo;
            Vector3d motion = new Vector3d(-dx, -dy, -dz);
            Matrix4d motionTrans = new Matrix4d().setTranslation(motion);

            for (ModelBone.Instance bone : boneList) {
                if (bone.calculateTransform) {
                    MatrixGroup matrixGroup = transform.get(bone);
                    matrixGroup.last = matrixGroup.current.mulLeft(motionTrans);

                    if (bone.physicalized) {
                        matrixGroup.current = bone.getTransform().mulLeft(getParentCurrentTransform(bone));
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
                        matrixGroup.current = bone.getTransform().mulLeft(getParentCurrentTransform(bone));
                    }
                }
            }
        }

        for (ParticleEmitter.Instance particle : particleEmitters.values())
            particle.update();
    }

    public void update(MatrixStack matrixStackIn, float animPos, float animSpeed, float age, float headYaw,
            float headPitch, float partial, double scale) {
        if (transform.isEmpty())
            return;

        evaluateAnimation(updateFunc, animPos, animSpeed, age, headYaw, headPitch, partial, scale);

        for (ModelBone.Instance bone : boneList) {
            bone.update();
            if (isPhysicsEnabled() && !isRenderingInventory && bone.physicalized) {
                MatrixGroup matrixGroup = transform.get(bone);
                matrixGroup.partial = matrixGroup.last.cpy().lerp(matrixGroup.current, partial);
            } else {
                transform.get(bone).partial = bone.getTransform().mulLeft(getParentPartialTransform(bone));
            }
        }
    }

    protected void calcModelViewMatrix(MatrixStack matrixStack) {
        modelViewMatrix = new Matrix4d(matrixStack.last().pose());
        modelViewMatrix.setTranslation(0, 0, 0);
        invModelViewMatrix = modelViewMatrix.cpy().inv();
    }

    protected void calcModelViewMatrix(float partialTicks, MatrixStack matrixStack) {
        modelViewMatrix = new Matrix4d(matrixStack.last().pose());
        modelViewMatrix.mulLeft(baseModelView);
        double x = (partialTicks - 1) * (entity.getX() - entity.xo);
        double y = (partialTicks - 1) * (entity.getY() - entity.yo);
        double z = (partialTicks - 1) * (entity.getZ() - entity.zo);
        modelViewMatrix.setTranslation(x, y, z);
        invModelViewMatrix = modelViewMatrix.cpy().inv();
    }

    public void evaluateAnimation(JSObject func, double scale) {
        evaluateAnimation(func, entity.animationPosition, entity.animationSpeed, entity.tickCount + 1,
                calculateYaw(), entity.xRot, 1, scale);
    }

    public void evaluateAnimation(JSObject func, double scale, float headYaw) {
        evaluateAnimation(func, entity.animationPosition, entity.animationSpeed, entity.tickCount + 1,
                headYaw, entity.xRot, 1, scale);
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

    public Matrix4d getBoneMatrix(IModelBone bone) { return transform.get(bone).partial; }
    public Matrix4d getBoneCurrentMatrix(IModelBone bone) { return transform.get(bone).current; }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, float animPos,
            float animSpeed, float age, float headYaw, float headPitch, float partial, double scale) {
        if (transform.isEmpty())
            return;

        isRenderingInventory = partial == 1;
        this.update(matrixStackIn, animPos, animSpeed, age, headYaw, headPitch, partial, scale);
        this.doRender(matrixStackIn, bufferIn, packedLightIn, null);
        isRenderingInventory = false;
    }

    public void setupBoneTransform(MatrixStack matrixStack, Matrix4d matrix) {
        matrix.mulLeftTo(matrixStack.last().pose());
        new Matrix3d().set(matrix).inv().transpose().mulLeftTo(matrixStack.last().normal());
    }

    public void setupBoneTransform(MatrixStack matrixStack, IModelBone bone) {
        setupBoneTransform(matrixStack, getBoneMatrix(bone));
    }

    public abstract void setupBoneTransform(MatrixStack matrixStack);

    protected void doRender(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, Set<ModelBone.Instance> bones) {
        matrixStackIn.pushPose();
        setupBoneTransform(matrixStackIn);
        boolean visible = !entity.isInvisible();
        boolean renderInvisible = !visible && !entity.isInvisibleTo(Minecraft.getInstance().player);
        boolean glowing = Minecraft.getInstance().shouldEntityAppearGlowing(entity);
        int packedOverlayIn = LivingRenderer.getOverlayCoords(entity, 0.0F);
        float alpha = renderInvisible ? 0.15F : 1.0F;

        for (ModelPart.Instance part : modelPartMap.values()) {
            ResourceLocation texture = getTextureLocation(part.texture);
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

    public String getTexture(String partName) {
        return modelPartMap.containsKey(partName) ? modelPartMap.get(partName).texture : "";
    }

    public void setTexture(String partName, String textureName) {
        if (modelPartMap.containsKey(partName) && (modelPack.hasTexture(textureName)
                || (textureName.equals("skin") && entity instanceof AbstractClientPlayerEntity))) {
            modelPartMap.get(partName).texture = textureName;
        }
    }

    private ResourceLocation getTextureLocation(String texture) {
        if (texture.equals("skin") && entity instanceof AbstractClientPlayerEntity)
            return ((AbstractClientPlayerEntity) entity).getSkinTextureLocation();
        return modelPack.getTexture(texture);
    }
}
