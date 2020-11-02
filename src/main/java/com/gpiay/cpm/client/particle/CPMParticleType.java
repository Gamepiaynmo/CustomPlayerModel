package com.gpiay.cpm.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11;

public class CPMParticleType implements IParticleRenderType {
    public static final CPMParticleType TYPE = new CPMParticleType();

    @Override
    public void beginRender(BufferBuilder bufferBuilder, TextureManager textureManager) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 1.0f / 255);
    }

    @Override
    public void finishRender(Tessellator tessellator) {
    }
}
