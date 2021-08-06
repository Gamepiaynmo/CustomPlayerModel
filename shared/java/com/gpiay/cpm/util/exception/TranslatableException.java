package com.gpiay.cpm.util.exception;

import net.minecraft.util.text.TranslationTextComponent;

public class TranslatableException extends RuntimeException {
    public TranslatableException() {
        super();
    }

    public TranslatableException(String message, Object... args) {
        super(new TranslationTextComponent(message, args).getString());
    }

    public TranslatableException(String message, Throwable cause, Object... args) {
        super(new TranslationTextComponent(message, args).getString(), cause);
    }

    public TranslatableException(Throwable cause) {
        super(cause);
    }
}
