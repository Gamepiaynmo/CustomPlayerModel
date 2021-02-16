package com.gpiay.cpm.util.exception;

public class ModelNotFoundException extends TranslatableException {
    public ModelNotFoundException(String fileName) {
        super("error.cpm.loadModel.notfound", fileName);
    }
}

