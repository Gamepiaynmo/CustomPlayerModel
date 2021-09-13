package com.gpiay.cpm.client.particle;

import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.model.element.ParticleEmitter;
import com.gpiay.cpm.util.math.Vector3d;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
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
        this.xd = motion.x;
        this.yd = motion.y;
        this.zd = motion.z;
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
        int cur = Math.min(cnt - 1, age * cnt / lifetime);

        int x = cur % emitter.animation[0];
        int y = cur / emitter.animation[0];
        minU = x * width;
        maxU = minU + width;
        minV = y * height;
        maxV = minV + height;
    }

    public void setAngle(float angle) { this.roll = angle; }
    public void setRotSpeed(float rotSpeed) { this.rotSpeed = rotSpeed; }
    public void setGravity(float gravity) { this.gravity = gravity; }
    public void setCollide(boolean collide) { this.hasPhysics = collide; }
    public void setSize(float size) { this.setSize(size, size); }
    public void setAlphaF(float alpha) { this.setAlpha(alpha); }

    @Override
    public void tick() {
        super.tick();

        this.oRoll = this.roll;
        this.roll += this.rotSpeed;

        calcUV();

        if (emitter.released)
            remove();
    }

    @Override
    public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partial) {
        if (CPMConfig.hideNearParticles() && Minecraft.getInstance().options.getCameraType() == PointOfView.FIRST_PERSON
                && emitter.model.entity.getEyePosition(partial).distanceToSqr(x, y, z) < 1)
            return;

        net.minecraft.util.math.vector.Vector3d vector3d = renderInfo.getPosition();
        float f = (float)(MathHelper.lerp(partial, this.xo, this.x) - vector3d.x());
        float f1 = (float)(MathHelper.lerp(partial, this.yo, this.y) - vector3d.y());
        float f2 = (float)(MathHelper.lerp(partial, this.zo, this.z) - vector3d.z());
        Quaternion quaternion;
        if (this.roll == 0.0F) {
            quaternion = renderInfo.rotation();
        } else {
            quaternion = new Quaternion(renderInfo.rotation());
            float f3 = MathHelper.lerp(partial, this.oRoll, this.roll);
            quaternion.mul(Vector3f.ZP.rotation(f3));
        }

        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f1.transform(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getQuadSize(partial);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.transform(quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        Minecraft.getInstance().getTextureManager().bind(texture);
        ((BufferBuilder) buffer).begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);
        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();
        int j = this.getLightColor(partial);
        buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        Tessellator.getInstance().end();
    }

    @Override
    protected float getU0() {
        return minU;
    }

    @Override
    protected float getU1() {
        return maxU;
    }

    @Override
    protected float getV0() {
        return minV;
    }

    @Override
    protected float getV1() {
        return maxV;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return CPMParticleType.TYPE;
    }
}
