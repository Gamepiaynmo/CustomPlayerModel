package com.gpiay.cpm.entity;

import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ServerCPMComponent extends ServerCPMAttachment implements ICPMComponent, CopyableComponent<ICPMComponent> {
    public ServerCPMComponent(LivingEntity entity) {
        super(entity);
    }

    @Override
    protected void syncAttachment() {
        CPMComponentProvider.ATTACHMENT.sync(getEntity());
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return !getMainModel().isEmpty();
    }

    @Override
    public void copyFrom(ICPMComponent other) {
        synchronizeData(other.getMainModel(), other.getScale(), other.getAccessories());
    }
}
