package com.gpiay.cpm.entity;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.ModelInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public abstract class ServerCPMAttachment extends CPMAttachment {
    protected ModelInfo model = null;
    private Vector3d eyePosition = null;

    public ServerCPMAttachment(LivingEntity entity) {
        super(entity);
    }

    @Override
    public void setMainModel(String mainModel) {
        setMainModel(mainModel, null);
    }
    private boolean setMainModel(String mainModel, boolean update) {
        return setMainModel(mainModel, null, update);
    }
    public void setMainModel(String mainModel, ServerPlayerEntity sender) {
        setMainModel(mainModel, sender, true);
    }
    private boolean setMainModel(String mainModel, ServerPlayerEntity sender, boolean update) {
        if (!this.mainModel.equals(mainModel)) {
            if (mainModel.isEmpty()) {
                this.mainModel = mainModel;
                model = null;
            } else {
                CPMMod.cpmServer.modelManager.getModelInfo(mainModel, sender).ifPresent(modelInfo -> {
                    this.mainModel = mainModel;
                    model = modelInfo;
                    if (update)
                        this.scale = model.defaultScale;
                });
            }

            if (this.mainModel.equals(mainModel) && update)
                onModelUpdate();

            return true;
        }

        return false;
    }

    @Override
    public void setScale(double scale) { setScale(scale, true); }
    private boolean setScale(double scale, boolean update) {
        if (scale != this.scale) {
            this.scale = scale;
            if (update)
                onModelUpdate();

            return true;
        }

        return false;
    }

    @Override
    public EntitySize changeEntitySize(Pose pose, EntitySize orinSize) {
        if (model != null)
            return model.getEntitySize(getEntity(), pose, orinSize, scale);
        return orinSize;
    }

    @Override
    public float changeEyeHeight(Pose pose, EntitySize entitySize, float orinHeight) {
        if (model != null)
            return model.getEntityEyeHeight(getEntity(), pose, orinHeight, scale);
        return orinHeight;
    }

    @Override
    public Vector3d changeEyePosition(float partial) {
        return eyePosition;
    }

    @Override
    public void update() {}

    @Override
    public void addAccessory(String accessId) {}
    @Override
    public void removeAccessory(String accessId) {}
    @Override
    public void clearAccessories() {}
    private boolean setAccessories(List<String> accessories, boolean update) {
        return false;
    }

    @Override
    public void synchronizeData(String mainModel, double scale, List<String> accessories) {
        boolean update = setScale(scale, false);
        update = setMainModel(mainModel, false) || update;
        update = setAccessories(accessories, false) || update;
        if (update) onModelUpdate();
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, float animPos, float animSpeed, float age, float headYaw, float headPitch, float partial) {}
    @Override
    public boolean renderFirstPerson(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, HandSide hand) { return false; }

    private void onModelUpdate() {
        this.getEntity().refreshDimensions();
        this.syncAttachment();
    }

    public ModelInfo getModel() { return model; }

    public void setEyePosition(Vector3d position) {
        this.eyePosition = position;
    }

    protected abstract void syncAttachment();
}
