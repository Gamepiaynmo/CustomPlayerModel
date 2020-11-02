package com.gpiay.cpm.model;

import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.resources.IResourceManager;

import java.io.IOException;
import java.io.InputStream;

public class CustomTexture extends Texture {
    private final NativeImage nativeImage;
    public CustomTexture(NativeImage nativeImage) {
        this.nativeImage = nativeImage;
    }

    public static CustomTexture fromInputStream(InputStream inputStream) throws IOException {
        return new CustomTexture(NativeImage.read(inputStream));
    }

    @Override
    public void loadTexture(IResourceManager manager) throws IOException {
        TextureUtil.prepareImage(getGlTextureId(), nativeImage.getWidth(), nativeImage.getHeight());
        nativeImage.uploadTextureSub(0, 0, 0, false);
    }
}
