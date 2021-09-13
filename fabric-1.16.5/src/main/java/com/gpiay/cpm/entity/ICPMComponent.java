package com.gpiay.cpm.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.nbt.CompoundNBT;

public interface ICPMComponent extends ICPMAttachment, AutoSyncedComponent {
    default void readFromNbt(CompoundNBT tag) { readFromNBT(tag); }
    default void writeToNbt(CompoundNBT tag) {
        writeToNBT(tag);
    }
}