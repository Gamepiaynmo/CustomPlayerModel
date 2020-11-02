package com.gpiay.cpm.util.function;

public interface ExceptionRunnable<E extends Exception> {
    void run() throws E;
}
