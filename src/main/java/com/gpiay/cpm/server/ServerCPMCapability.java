package com.gpiay.cpm.server;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.network.Networking;
import com.gpiay.cpm.network.UpdateModelPacket;
import com.gpiay.cpm.server.capability.CPMCapability;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;

public class ServerCPMCapability extends CPMCapability {
    protected ModelInfo model = null;

    public ServerCPMCapability(LivingEntity entity) {
        super(entity);
    }

    @Override
    public void setModelId(@Nonnull String modelId) {
        setModelId(modelId, null);
    }

    private void setModelId(@Nonnull String modelId, boolean update) {
        setModelId(modelId, null, update);
    }

    public void setModelId(@Nonnull String modelId, ServerPlayerEntity sender) {
        setModelId(modelId, sender, true);
    }

    private void setModelId(@Nonnull String modelId, ServerPlayerEntity sender, boolean update) {
        if (!this.modelId.equals(modelId)) {
            if (modelId.isEmpty()) {
                model = null;
                this.modelId = modelId;
            } else {
                CPMMod.cpmServer.modelManager.getModelInfo(modelId, sender).ifPresent(modelInfo -> {
                    model = modelInfo;
                    this.modelId = modelId;
                    if (update)
                        this.scale = model.defaultScale;
                });
            }

            if (this.modelId.equals(modelId) && update) {
                onModelUpdate();
            }
        }
    }

    @Override
    public void setScale(double scale) {
        setScale(scale, true);
    }

    private void setScale(double scale, boolean update) {
        if (scale != this.scale) {
            this.scale = scale;
            if (update)
                onModelUpdate();
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        setScale(nbt.contains("scale") ? nbt.getDouble("scale") : 1, false);
        setModelId(nbt.getString("model"), false);
        onModelUpdate();
    }

    private void onModelUpdate() {
        getEntity().recalculateSize();
        Networking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getEntity),
                new UpdateModelPacket(getEntity()));
    }

    public ModelInfo getModel() { return model; }
}
