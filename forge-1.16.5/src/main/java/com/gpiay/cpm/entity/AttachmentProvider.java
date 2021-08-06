package com.gpiay.cpm.entity;

import net.minecraft.entity.Entity;

import java.util.Optional;

public class AttachmentProvider {
    public static Optional<ICPMAttachment> getEntityAttachment(Entity entity) {
        return entity.getCapability(CPMCapabilityProvider.CAPABILITY).resolve().map(ICPMAttachment.class::cast);
    }
}
