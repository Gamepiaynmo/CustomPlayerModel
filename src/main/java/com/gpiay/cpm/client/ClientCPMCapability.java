package com.gpiay.cpm.client;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.network.UpdateKeyStatePacket;
import com.gpiay.cpm.server.capability.CPMCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;

public class ClientCPMCapability extends CPMCapability {
    protected ModelInstance model = null;
    private boolean shouldKeepTrying = true;
    private int tryLoadTimer = 0;
    public boolean[] customKeys = new boolean[CPMMod.customKeyCount];

    public ClientCPMCapability(LivingEntity entity) {
        super(entity);

        if (!(entity instanceof PlayerEntity))
            CPMMod.cpmClient.processRenderer(Minecraft.getInstance().getRenderManager().getRenderer(entity));
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

    private void setModel(ModelInstance model, boolean update) {
        this.model = model;
        if (update) {
            this.scale = model.getModelPack().defaultScale;
            onModelUpdate();
        }
    }

    @Override
    public void setModelId(@Nonnull String modelId) {
        setModelId(modelId, true);
    }

    private void setModelId(@Nonnull String modelId, boolean update) {
        if (!this.modelId.equals(modelId)) {
            this.modelId = modelId;

            if (model != null) {
                model.release();
                setModel(null, update);
            }

            shouldKeepTrying = true;
            tryLoadTimer = 0;
            tryLoadModel();
        }
    }

    private void tryLoadModel() {
        if (!modelId.isEmpty() && getEntity().world.isRemote) {
            CPMMod.startRecordingError();
            CPMMod.cpmClient.modelManager.getModelPack(modelId).ifPresent(modelPack -> {
                setModel(modelPack.instantiate(getEntity()), true);
            });

            if (!CPMMod.endRecordingError().isEmpty())
                shouldKeepTrying = false;
        }
    }

    @Override
    public void update() {
        if (model == null && !modelId.isEmpty() && shouldKeepTrying) {
            tryLoadTimer++;
            if ((tryLoadTimer & (tryLoadTimer - 1)) == 0)
                tryLoadModel();

            if (tryLoadTimer > 1024)
                shouldKeepTrying = false;
        }

        if (model != null) {
            model.tick(getScale());
        }
    };

    public ModelInstance getModel() {
        return model;
    }

    public void loadEditingModel(String modelId) {
        setModelId("", true);
        CPMMod.cpmClient.modelManager.loadEditingModelPack(modelId).ifPresent(modelPack -> {
            setModel(modelPack.instantiate(getEntity()), true);
        });
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        setScale(nbt.contains("scale") ? nbt.getDouble("scale") : 1, false);
        setModelId(nbt.getString("model"), false);
        onModelUpdate();
    }

    private void onModelUpdate() {
        getEntity().recalculateSize();
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
                Networking.INSTANCE.send(PacketDistributor.SERVER.noArg(), new UpdateKeyStatePacket(keyStates, getPlayerEntity()));
        }

        this.customKeys = keyStates;
    }
}
