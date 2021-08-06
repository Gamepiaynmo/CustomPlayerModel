package com.gpiay.cpm.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICPMCapability extends ICPMAttachment, INBTSerializable<CompoundNBT> {
    default CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        writeToNBT(nbt);
        return nbt;
    }

    default void deserializeNBT(CompoundNBT nbt) {
        readFromNBT(nbt);
    }
}
