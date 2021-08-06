package com.gpiay.cpm.mixin;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TextureManager.class)
public class TextureManagerFix {
    @Final
    @Shadow
    private Map<ResourceLocation, Texture> byPath;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureUtil;releaseTextureId(I)V"
            ),
            method = "release(Lnet/minecraft/util/ResourceLocation;)V"
    )
    private void releaseFix(ResourceLocation resourceLocation, CallbackInfo ci) {
        byPath.remove(resourceLocation);
    }
}
