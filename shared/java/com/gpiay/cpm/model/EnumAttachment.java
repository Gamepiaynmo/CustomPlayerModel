package com.gpiay.cpm.model;

import java.util.Optional;

public enum EnumAttachment {
    HELMET("helmet"),
    CHESTPLATE_BODY("chestplate_body"),
    CHESTPLATE_LEFT("chestplate_left"),
    CHESTPLATE_RIGHT("chestplate_right"),
    LEGGINGS_BODY("leggings_body"),
    LEGGINGS_LEFT("leggings_left"),
    LEGGINGS_RIGHT("leggings_right"),
    BOOTS_LEFT("boots_left"),
    BOOTS_RIGHT("boots_right"),

    ITEM_LEFT("item_left"),
    ITEM_RIGHT("item_right"),

    CAPE("cape"),
    SKULL("skull"),
    ELYTRA("elytra"),

    PARROT_LEFT("parrot_left"),
    PARROT_RIGHT("parrot_right");

    private final String attachmentId;

    EnumAttachment(String id) {
        this.attachmentId = id;
    }

    public String getId() {
        return attachmentId;
    }

    public static Optional<EnumAttachment> getByAttachmentId(String id) {
        for (EnumAttachment attachment : values()) {
            if (attachment.attachmentId.equals(id))
                return Optional.of(attachment);
        }

        return Optional.empty();
    }
}
