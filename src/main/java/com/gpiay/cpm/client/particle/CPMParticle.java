package com.gpiay.cpm.client.particle;

import com.gpiay.cpm.client.ClientConfig;
import com.gpiay.cpm.model.element.ParticleEmitterInstance;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class CPMParticle extends TexturedParticle {
    private final ParticleEmitterInstance emitter;
    private final ResourceLocation texture;

    private float rotSpeed;
    private float minU, maxU, minV, maxV;

    public CPMParticle(World worldIn, ParticleEmitterInstance emitter, ResourceLocation texture, Vector3d position, Vector3d motion) {
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
    public void renderParticle(BufferBuilder buffer, ActiveRenderInfo entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (ClientConfig.HIDE_NEAR_PARTICLES.get() && Minecraft.getInstance().gameSettings.thirdPersonView == 0
                && emitter.model.entity.getEyePosition(partialTicks).squareDistanceTo(posX, posY, posZ) < 1)
            return;

        float f = this.getScale(partialTicks);
        float f1 = this.getMinU();
        float f2 = this.getMaxU();
        float f3 = this.getMinV();
        float f4 = this.getMaxV();
        float f5 = (float)(MathHelper.lerp((double)partialTicks, this.prevPosX, this.posX) - interpPosX);
        float f6 = (float)(MathHelper.lerp((double)partialTicks, this.prevPosY, this.posY) - interpPosY);
        float f7 = (float)(MathHelper.lerp((double)partialTicks, this.prevPosZ, this.posZ) - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & '\uffff';
        int k = i & '\uffff';
        Vec3d[] avec3d = new Vec3d[]{new Vec3d((double)(-rotationX * f - rotationXY * f), (double)(-rotationZ * f), (double)(-rotationYZ * f - rotationXZ * f)), new Vec3d((double)(-rotationX * f + rotationXY * f), (double)(rotationZ * f), (double)(-rotationYZ * f + rotationXZ * f)), new Vec3d((double)(rotationX * f + rotationXY * f), (double)(rotationZ * f), (double)(rotationYZ * f + rotationXZ * f)), new Vec3d((double)(rotationX * f - rotationXY * f), (double)(-rotationZ * f), (double)(rotationYZ * f - rotationXZ * f))};
        if (this.particleAngle != 0.0F) {
            float f8 = MathHelper.lerp(partialTicks, this.prevParticleAngle, this.particleAngle);
            float f9 = MathHelper.cos(f8 * 0.5F);
            float f10 = (float)((double)MathHelper.sin(f8 * 0.5F) * entityIn.getLookDirection().x);
            float f11 = (float)((double)MathHelper.sin(f8 * 0.5F) * entityIn.getLookDirection().y);
            float f12 = (float)((double)MathHelper.sin(f8 * 0.5F) * entityIn.getLookDirection().z);
            Vec3d vec3d = new Vec3d((double)f10, (double)f11, (double)f12);

            for(int l = 0; l < 4; ++l) {
                avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((double)(f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((double)(2.0F * f9)));
            }
        }

        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        buffer.pos((double)f5 + avec3d[0].x, (double)f6 + avec3d[0].y, (double)f7 + avec3d[0].z).tex((double)f2, (double)f4).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)f5 + avec3d[1].x, (double)f6 + avec3d[1].y, (double)f7 + avec3d[1].z).tex((double)f2, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)f5 + avec3d[2].x, (double)f6 + avec3d[2].y, (double)f7 + avec3d[2].z).tex((double)f1, (double)f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos((double)f5 + avec3d[3].x, (double)f6 + avec3d[3].y, (double)f7 + avec3d[3].z).tex((double)f1, (double)f4).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
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
