package com.gpiay.cpm.entity;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ClientModelManager;
import com.gpiay.cpm.model.ModelInstance;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public abstract class ClientCPMAttachment extends CPMAttachment {
    protected ModelInstance model = null;
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
        if (model != null)
            return model.getModelPack().getEntitySize(getEntity(), pose, orinSize, scale);
        return orinSize;
    }

    @Override
    public float changeEyeHeight(Pose pose, EntitySize entitySize, float orinHeight) {
        if (model != null)
            return model.getModelPack().getEntityEyeHeight(getEntity(), pose, orinHeight, scale);
        return orinHeight;
    }

    @Override
    public Vector3d changeEyePosition(float partial) {
        if (model != null && model.isReady()) {
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
    public void setMainModel(String mainModel) {
        setMainModel(mainModel, true);
    }
    private void setMainModel(String mainModel, boolean update) {
        if (!this.mainModel.equals(mainModel)) {
            this.mainModel = mainModel;

            if (model != null) {
                model.release();
                setModel(null, update);
            }

            shouldKeepTrying = true;
            tryLoadTimer = 0;
            tryLoadModel();
        }
    }

    private void setModel(ModelInstance model, boolean update) {
        this.model = model;
        if (update) {
            onModelUpdate();
        }
    }

    private void tryLoadModel() {
        if (!mainModel.isEmpty() && getEntity().level.isClientSide) {
            CPMMod.startRecordingError();
            CPMMod.cpmClient.modelManager.getModelPack(mainModel).ifPresent(modelPack -> {
                setModel(modelPack.instantiate(getEntity()), true);
            });

            if (!CPMMod.endRecordingError().isEmpty())
                shouldKeepTrying = false;
        }
    }

    @Override
    public void update() {
        if (model == null && !mainModel.isEmpty() && shouldKeepTrying) {
            tryLoadTimer++;
            if ((tryLoadTimer & (tryLoadTimer - 1)) == 0)
                tryLoadModel();

            if (tryLoadTimer > 1024)
                shouldKeepTrying = false;
        }

        if (model != null) {
            model.tick(getScale());
        }
    }

    @Override
    public void addAccessory(String accessId) {}
    @Override
    public void removeAccessory(String accessId) {}
    @Override
    public void clearAccessories() {}

    @Override
    public void synchronizeData(String mainModel, double scale, List<String> accessories) {
        setMainModel(mainModel, false);
        setScale(scale, false);
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
            model.renderFirstPerson(matrixStackIn, bufferIn, packedLightIn, HandSide.RIGHT);
            return true;
        }
        return false;
    }

    public ModelInstance getModel() {
        return model;
    }

    public void loadEditingModel(String modelId) {
        CPMMod.cpmClient.modelManager.loadEditingModelPack(modelId).ifPresent(modelPack -> {
            ClientModelManager.editingModelPack = modelPack;
            setMainModel("");
            setModel(modelPack.instantiate(getEntity()), true);
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
