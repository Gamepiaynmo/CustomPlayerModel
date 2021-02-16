package com.gpiay.cpm.client.particle;

import com.gpiay.cpm.client.ClientConfig;
import com.gpiay.cpm.model.element.ParticleEmitter;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

public class CPMParticle extends TexturedParticle {
    private final ParticleEmitter.Instance emitter;
    private final ResourceLocation texture;

    private float rotSpeed;
    private float minU, maxU, minV, maxV;

    public CPMParticle(ClientWorld worldIn, ParticleEmitter.Instance emitter, ResourceLocation texture, Vector3d position, Vector3d motion) {
        super(worldIn, position.x, position.y, position.z);
        this.emitter = emitter;
        this.texture = texture;
        this.motionX = motion.x;
        this.motionY = motion.y;
        this.motionZ = motion.z;
        calcUV();
    }

    private void calcUV() {
        int cnt = emitter.animation[0] * emitter.animation[1];
        if (cnt <= 1) {
            minU = minV = 0;
            maxU = maxV = 1;
            return;
        }

        float width = 1.0f / emitter.animation[0];
        float height = 1.0f / emitter.animation[1];
        int cur = Math.min(cnt - 1, age * cnt / maxAge);

        int x = cur % emitter.animation[0];
        int y = cur / emitter.animation[0];
        minU = x * width;
        maxU = minU + width;
        minV = y * height;
        maxV = minV + height;
    }

    public void setAngle(float angle) { this.particleAngle = angle; }
    public void setRotSpeed(float rotSpeed) { this.rotSpeed = rotSpeed; }
    public void setGravity(float gravity) { this.particleGravity = gravity; }
    public void setCollide(boolean collide) { this.canCollide = collide; }
    public void setSize(float size) { this.particleScale = size; }
    public void setAlpha(float alpha) { this.setAlphaF(alpha); }

    @Override
    public void tick() {
        super.tick();

        this.prevParticleAngle = this.particleAngle;
        this.particleAngle += this.rotSpeed;

        calcUV();

        if (emitter.released)
            setExpired();
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        if (ClientConfig.HIDE_NEAR_PARTICLES.get() && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON
                && emitter.model.entity.getEyePosition(partialTicks).squareDistanceTo(posX, posY, posZ) < 1)
            return;

        net.minecraft.util.math.vector.Vector3d vector3d = renderInfo.getProjectedView();
        float f = (float)(MathHelper.lerp(partialTicks, this.prevPosX, this.posX) - vector3d.getX());
        float f1 = (float)(MathHelper.lerp(partialTicks, this.prevPosY, this.posY) - vector3d.getY());
        float f2 = (float)(MathHelper.lerp(partialTicks, this.prevPosZ, this.posZ) - vector3d.getZ());
        Quaternion quaternion;
        if (this.particleAngle == 0.0F) {
            quaternion = renderInfo.getRotation();
        } else {
            quaternion = new Quaternion(renderInfo.getRotation());
            float f3 = MathHelper.lerp(partialTicks, this.prevParticleAngle, this.particleAngle);
            quaternion.multiply(Vector3f.ZP.rotation(f3));
        }

        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f1.transform(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getScale(partialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.transform(quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        ((BufferBuilder) buffer).begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        float f7 = this.getMinU();
        float f8 = this.getMaxU();
        float f5 = this.getMinV();
        float f6 = this.getMaxV();
        int j = this.getBrightnessForRender(partialTicks);
        buffer.pos((double)avector3f[0].getX(), (double)avector3f[0].getY(), (double)avector3f[0].getZ()).tex(f8, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
        buffer.pos((double)avector3f[1].getX(), (double)avector3f[1].getY(), (double)avector3f[1].getZ()).tex(f8, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
        buffer.pos((double)avector3f[2].getX(), (double)avector3f[2].getY(), (double)avector3f[2].getZ()).tex(f7, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
        buffer.pos((double)avector3f[3].getX(), (double)avector3f[3].getY(), (double)avector3f[3].getZ()).tex(f7, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
        Tessellator.getInstance().draw();
    }

    @Override
    protected float getMinU() {
        return minU;
    }

    @Override
    protected float getMaxU() {
        return maxU;
    }

    @Override
    protected float getMinV() {
        return minV;
    }

    @Override
    protected float getMaxV() {
        return maxV;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return CPMParticleType.TYPE;
    }
}
