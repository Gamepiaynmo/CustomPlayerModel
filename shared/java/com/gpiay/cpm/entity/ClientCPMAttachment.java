package com.gpiay.cpm.entity;

import com.google.common.collect.Lists;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ClientModelManager;
import com.gpiay.cpm.model.AccessoryInstance;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.ModelPack;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class ClientCPMAttachment extends CPMAttachment {
    protected ModelInstance model = null;
    protected final List<AccessoryInstance> accessoryModels = Lists.newArrayList();
    private boolean shouldKeepTrying = true;
    private int tryLoadTimer = 0;
    public boolean[] customKeys = new boolean[CPMMod.customKeyCount];

    public ClientCPMAttachment(LivingEntity entity) {
        super(entity);

        if (!(entity instanceof PlayerEntity) && !CPMMod.cpmServer.blacklist.contains(entity.getType()))
            CPMMod.cpmClient.processRenderer(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity));
    }

    @Override
    public EntitySize changeEntitySize(Pose pose, EntitySize orinSize) {
        if (model != null && CPMMod.cpmClient.isServerModded)
            return model.getModelPack().getEntitySize(getEntity(), pose, orinSize, scale);
        return orinSize;
    }

    @Override
    public float changeEyeHeight(Pose pose, EntitySize entitySize, float orinHeight) {
        if (model != null && CPMMod.cpmClient.isServerModded)
            return model.getModelPack().getEntityEyeHeight(getEntity(), pose, orinHeight, scale);
        return orinHeight;
    }

    @Override
    public Vector3d changeEyePosition(float partial) {
        if (model != null && model.isReady() && CPMMod.cpmClient.isServerModded) {
            com.gpiay.cpm.util.math.Vector3d pos = model.getEyePosition(partial);
            if (pos != null)
                return new Vector3d(pos.x, pos.y, pos.z);
        }

        return null;
    }

    @Override
    public void setScale(double scale) {
        setScale(scale, true);
    }
    private void setScale(double scale, boolean update) {
        if (this.scale != scale) {
            this.scale = scale;
            if (update)
                onModelUpdate();
        }
    }

    @Override
    public void setMainModel(String mainModel) { setMainModel(mainModel, true); }
    private void setMainModel(String mainModel, boolean update) {
        if (!this.mainModel.equals(mainModel)) {
            this.mainModel = mainModel;

            if (model != null) {
                model.release();
                setModel(null, update);
            }

            if (!mainModel.isEmpty())
                startLoadingModel();
        }
    }

    @Override
    public void setAccessories(List<String> toRemove, List<String> toAdd) {
        if (!toRemove.isEmpty()) {
            accessories.removeAll(toRemove);
            for (Iterator<AccessoryInstance> iter = accessoryModels.iterator(); iter.hasNext(); ) {
                AccessoryInstance model = iter.next();
                if (toRemove.contains(model.getModelPack().id)) {
                    model.release();
                    iter.remove();
                }
            }
        }

        if (!this.mainModel.isEmpty() && !toAdd.isEmpty()) {
            accessories.addAll(toAdd);
            startLoadingModel();
        }
    }

    private void startLoadingModel() {
        shouldKeepTrying = true;
        tryLoadTimer = 0;
        tryLoadModel();
    }

    private void setModel(ModelInstance model, boolean update) {
        this.model = model;
        if (model == null) {
            clearAccessoryModels();
        } else {
            for (int i = 0; i < accessoryModels.size(); i++) {
                AccessoryInstance instance = accessoryModels.get(i);
                accessoryModels.set(i, instance.getModelPack().instantiateAccessory(getEntity()));
                instance.release();
            }
        }

        if (update) {
            onModelUpdate();
        }
    }

    private void clearAccessoryModels() {
        for (AccessoryInstance instance : accessoryModels)
            instance.release();
        accessoryModels.clear();
    }

    private boolean trySetModel(ModelPack modelPack) {
        try {
            modelPack.assertModel();
            ModelInstance model = modelPack.instantiateModel(getEntity());
            setModel(model, true);
            return true;
        } catch (Exception e) {
            CPMMod.warn(e);
            return false;
        }
    }

    private boolean tryAddAccessory(ModelPack modelPack) {
        try {
            modelPack.assertAccessory();
            accessoryModels.add(modelPack.instantiateAccessory(getEntity()));
            return true;
        } catch (Exception e) {
            CPMMod.warn(e);
            return false;
        }
    }

    private void tryLoadModel() {
        if (!mainModel.isEmpty() && model == null) {
            CPMMod.startRecordingError();
            CPMMod.cpmClient.modelManager.getModelPack(mainModel).ifPresent(this::trySetModel);
            if (!CPMMod.endRecordingError().isEmpty())
                shouldKeepTrying = false;
        }

        if (model != null) {
            for (String accessoryId : accessories) {
                if (accessoryModels.stream().noneMatch(model -> model.getModelPack().id.equals(accessoryId))) {
                    CPMMod.startRecordingError();
                    CPMMod.cpmClient.modelManager.getModelPack(accessoryId).ifPresent(this::tryAddAccessory);
                    if (!CPMMod.endRecordingError().isEmpty())
                        shouldKeepTrying = false;
                }
            }
        }
    }

    @Override
    public void update() {
        if (shouldKeepTrying &&
                ((!mainModel.isEmpty() && model == null) ||
                        (accessories.size() != accessoryModels.size()))) {
            tryLoadTimer++;
            if ((tryLoadTimer & (tryLoadTimer - 1)) == 0)
                tryLoadModel();

            if (tryLoadTimer > 1024)
                shouldKeepTrying = false;
        }

        if (model != null)
            model.tick(scale);
        for (AccessoryInstance model : accessoryModels)
            model.tick(scale);
    }

    @Override
    public void synchronizeData(String mainModel, Double scale, List<String> accessories) {
        if (mainModel != null)
            setMainModel(mainModel, false);
        if (scale != null)
            setScale(scale, false);
        if (accessories != null)
            setAccessories(accessories);
        onModelUpdate();
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, float animPos, float animSpeed, float age, float headYaw, float headPitch, float partial) {
        if (model != null) {
            model.render(matrixStackIn, bufferIn, packedLightIn, animPos, animSpeed, age, headYaw, headPitch, partial, scale);
        }
    }

    @Override
    public boolean renderFirstPerson(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, HandSide hand) {
        if (model != null && model.isReady()) {
            model.renderFirstPerson(matrixStackIn, bufferIn, packedLightIn, hand);
            return true;
        }
        return false;
    }

    public ModelInstance getModel() {
        return model;
    }

    public List<AccessoryInstance> getAccessoryModels() {
        return accessoryModels;
    }

    public void loadEditingModel(String modelId) {
        CPMMod.cpmClient.modelManager.loadEditingModelPack(modelId).ifPresent(modelPack -> {
            if (modelPack.isAccessory) {
                if (model != null) {
                    clearAccessoryModels();
                    if (tryAddAccessory(modelPack))
                        ClientModelManager.editingModelPack = modelPack;
                }
            } else {
                setModel(null, false);
                if (trySetModel(modelPack))
                    ClientModelManager.editingModelPack = modelPack;
            }
        });
    }

    private void onModelUpdate() {
        getEntity().refreshDimensions();
    }

    public void updateCustomKeyState(boolean[] keyStates) {
        if (getEntity() == Minecraft.getInstance().player) {
            boolean updated = false;
            for (int i = 0; i < customKeys.length; i++) {
                if (customKeys[i] != keyStates[i]) {
                    updated = true;
                    break;
                }
            }

            if (updated && CPMMod.cpmClient.isServerModded)
                syncKeyState(keyStates);
        }

        this.customKeys = keyStates;
    }

    protected abstract void syncKeyState(boolean[] keyStates);
}
