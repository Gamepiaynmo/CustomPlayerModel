package com.gpiay.cpm.util.function;

public interface ExceptionConsumer<T, E extends Exception> {
    void accept(T t) throws E;
}