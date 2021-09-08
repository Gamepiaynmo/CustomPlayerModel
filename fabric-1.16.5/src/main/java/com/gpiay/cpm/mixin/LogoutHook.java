package com.gpiay.cpm.mixin;

import com.gpiay.cpm.CPMMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IWindowEventListener;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class LogoutHook extends RecursiveEventLoop<Runnable> implements ISnooperInfo, IWindowEventListener {
    public LogoutHook(String string) {
        super(string);
    }

    @Inject(
            at = @At("TAIL"),
            method = "Lnet/minecraft/client/Minecraft;clearLevel(Lnet/minecraft/client/gui/screen/Screen;)V"
    )
    public void onClearLevel(Screen screen, CallbackInfo ci) {
        CPMMod.cpmClient.isServerModded = false;
    }
}
