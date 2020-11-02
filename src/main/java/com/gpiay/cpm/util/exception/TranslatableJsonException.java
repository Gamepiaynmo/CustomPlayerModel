package com.gpiay.cpm.util.exception;

import com.google.gson.stream.JsonReader;
import net.minecraft.util.text.TranslationTextComponent;

public class TranslatableJsonException extends TranslatableException {
    public TranslatableJsonException() {
        super();
    }

    public TranslatableJsonException(String message, JsonReader json, Object... args) {
        super(new TranslationTextComponent(message, args).getUnformattedComponentText() + ". " + json);
    }

    public TranslatableJsonException(String message, JsonReader json, Throwable cause, Object... args) {
        super(new TranslationTextComponent(message, args).getUnformattedComponentText() + ". " + json, cause);
    }

    public TranslatableJsonException(Throwable cause) {
        super(cause);
    }
}
