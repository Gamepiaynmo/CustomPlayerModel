package com.gpiay.cpm.util.function;

public interface ExceptionFunction<I, O, E extends Exception> {
    O apply(I input) throws E;
}
