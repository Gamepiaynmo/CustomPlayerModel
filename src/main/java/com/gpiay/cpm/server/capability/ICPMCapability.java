package com.gpiay.cpm.server.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICPMCapability extends INBTSerializable<CompoundNBT> {
    String getModelId();
    void setModelId(String modelId);

    double getScale();
    void setScale(double scale);

    void update();
}
