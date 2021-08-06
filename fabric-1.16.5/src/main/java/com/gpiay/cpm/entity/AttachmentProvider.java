package com.gpiay.cpm.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

public class AttachmentProvider {
    public static Optional<ICPMAttachment> getEntityAttachment(Entity entity) {
        try {
            return CPMComponentProvider.ATTACHMENT.maybeGet(entity).map(ICPMAttachment.class::cast);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }
}
