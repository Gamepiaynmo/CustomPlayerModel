package com.gpiay.cpm.hook;

public class LivingRendererHook {
    private static boolean enabled = false;
    private static Runnable callback;

    public static void enableHook(Runnable cb) {
        enabled = true;
        callback = cb;
    }

    public static boolean isHookEnabled() {
        return enabled;
    }

    public static boolean onBeginRenderModel() {
        if (enabled) {
            callback.run();
            enabled = false;
            return true;
        }

        return false;
    }
}
