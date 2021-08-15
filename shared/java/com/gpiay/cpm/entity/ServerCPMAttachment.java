package com.gpiay.cpm.entity;

import com.google.common.collect.Lists;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.ModelPack;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class ServerCPMAttachment extends CPMAttachment {
    protected ModelInfo model = null;
    protected final List<ModelInfo> accessoryModels = Lists.newArrayList();
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
                setAccessories(Collections.emptyList(), update);
            } else {
                CPMMod.cpmServer.modelManager.getModelInfo(mainModel, sender).ifPresent(modelInfo -> {
                    if (trySetModel(modelInfo) && update)
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
    public void setAccessories(List<String> toRemove, List<String> toAdd) { setAccessories(toRemove, toAdd, null); }
    private boolean setAccessories(List<String> toRemove, List<String> toAdd, boolean update) { return setAccessories(toRemove, toAdd, null, update); }
    public void setAccessories(List<String> toRemove, List<String> toAdd, ServerPlayerEntity sender) { setAccessories(toRemove, toAdd, sender, true); }
    private boolean setAccessories(List<String> toRemove, List<String> toAdd, ServerPlayerEntity sender, boolean update) {
        final boolean[] updated = {false};
        if (!toRemove.isEmpty()) {
            accessories.removeAll(toRemove);
            accessoryModels.removeIf(modelInfo -> toRemove.contains(modelInfo.id));
            updated[0] = true;
        }

        for (String accessoryId : toAdd) {
            CPMMod.cpmServer.modelManager.getModelInfo(accessoryId, sender).ifPresent(modelInfo -> {
                if (tryAddAccessory(modelInfo))
                    updated[0] = true;
            });
        }

        if (updated[0] && update)
            syncAttachment();

        return updated[0];
    }
    private boolean setAccessories(List<String> accessories, boolean update) {
        List<String> toRemove = subtract(this.accessories, accessories);
        List<String> toAdd = subtract(accessories, this.accessories);
        if (!toRemove.isEmpty() || !toAdd.isEmpty())
            return setAccessories(toRemove, toAdd, update);
        return false;
    }
    public void addAccessory(String accessory, ServerPlayerEntity sender) {
        setAccessories(Collections.emptyList(), Lists.newArrayList(accessory), sender);
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

    private boolean trySetModel(ModelInfo modelInfo) {
        try {
            modelInfo.assertModel();
            this.mainModel = modelInfo.id;
            this.model = modelInfo;
            return true;
        } catch (Exception e) {
            CPMMod.warn(e);
            return false;
        }
    }

    private boolean tryAddAccessory(ModelInfo modelInfo) {
        try {
            modelInfo.assertAccessory();
            this.accessories.add(modelInfo.id);
            this.accessoryModels.add(modelInfo);
            return true;
        } catch (Exception e) {
            CPMMod.warn(e);
            return false;
        }
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
    public void synchronizeData(String mainModel, Double scale, List<String> accessories) {
        boolean update = false;
        if (mainModel != null)
            update = setMainModel(mainModel, false);
        if (scale != null)
            update = setScale(scale, false) || update;
        if (accessories != null)
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
