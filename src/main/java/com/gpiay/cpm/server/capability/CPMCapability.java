package com.gpiay.cpm.server.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;

public abstract class CPMCapability implements ICPMCapability {
    @CapabilityInject(ICPMCapability.class)
    public static Capability<ICPMCapability> CAPABILITY;

    private final LivingEntity entity;
    protected String modelId = "";
    protected double scale = 1;

    public CPMCapability(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public double getScale() { return scale; }

    @Override
    public abstract void setScale(double scale);

    @Override
    public String getModelId() {
        return modelId;
    }

    @Override
    public abstract void setModelId(@Nonnull String modelId);

    @Override
    public void update() {};

    public boolean isPlayer() {
        return entity instanceof PlayerEntity;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public PlayerEntity getPlayerEntity() {
        return isPlayer() ? (PlayerEntity) entity : null;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putString("model", modelId);
        compoundNBT.putDouble("scale", scale);
        return compoundNBT;
    }

    @Override
    public abstract void deserializeNBT(CompoundNBT nbt);
}
