package com.gpiay.cpm.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

public class AttachmentProvider {
    public static Optional<ICPMAttachment> getEntityAttachment(Entity entity) {
        return CPMComponentProvider.ATTACHMENT.maybeGet(entity).map(ICPMAttachment.class::cast);
    }
}
