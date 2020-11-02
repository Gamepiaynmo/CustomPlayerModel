package com.gpiay.cpm.model.element;

import com.gpiay.cpm.client.particle.CPMParticle;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Quat4d;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class ParticleEmitterInstance {
    public String name;
    public String texture;
    public Vector3d posRange = Vector3d.Zero.cpy();
    public double dirRange;
    public double[] angle;
    public double[] speed;
    public double[] rotSpeed;
    public double[] lifeSpan;
    public double density;
    public int[] animation;
    public double[][] color = new double[4][];
    public double[] size;
    public double gravity;
    public boolean collide;

    public final ModelInstance model;
    public final IModelBone bone;
    private double timer;
    public boolean released = false;
    private static final Random random = new Random();

    public ParticleEmitterInstance(ModelInstance model, IModelBone parent) {
        this.model = model;
        bone = parent;
    }

    public void setTexture(String texture) {
        if (model.getModelPack().getTexture(texture) != null)
            this.texture = texture;
    }

    public void tick(Matrix4d transform) {
        Minecraft client = Minecraft.getInstance();
        ParticleManager manager = client.particles;
        ClientWorld world = client.world;

        if (bone.isVisible()) {
            timer += 1;

            double EPS = 1e-4;
            if (density > EPS && timer > 0) {
                Vector3d center = new Vector3d();
                center.x = transform.val[12] + model.entity.posX;
                center.y = transform.val[13] + model.entity.posY + 1.5;
                center.z = transform.val[14] + model.entity.posZ;

                Vector3d[] axis = new Vector3d[3];
                for (int i = 0; i < 3; i++) {
                    axis[i] = new Vector3d();
                    axis[i].x = transform.val[i * 4];
                    axis[i].y = transform.val[i * 4 + 1];
                    axis[i].z = transform.val[i * 4 + 2];
                }

                ResourceLocation location = model.getModelPack().getTexture(texture);

                while (timer > 0) {
                    timer -= 1 / density;
                    Vector3d pos = center.cpy();
                    pos.add(axis[0].cpy().scl(MathHelper.lerp(random.nextDouble(), -posRange.x, posRange.x)));
                    pos.add(axis[1].cpy().scl(MathHelper.lerp(random.nextDouble(), -posRange.y, posRange.y)));
                    pos.add(axis[2].cpy().scl(MathHelper.lerp(random.nextDouble(), -posRange.z, posRange.z)));

                    Vector3d dir = axis[2].cpy();
                    dir.rotate(axis[0], MathHelper.lerp(random.nextDouble(), -dirRange, dirRange));
                    dir.rotate(axis[1], MathHelper.lerp(random.nextDouble(), -dirRange, dirRange));
                    dir.scl(MathHelper.lerp(random.nextDouble(), speed[0], speed[1]));

                    CPMParticle particle = new CPMParticle(world, this, location, pos, dir);
                    particle.setAngle((float) (Quat4d.degreesToRadians * MathHelper.lerp(random.nextFloat(), angle[0], angle[1])));
                    particle.setRotSpeed((float) MathHelper.lerp(random.nextFloat(), rotSpeed[0], rotSpeed[1]));
                    particle.setMaxAge((int) MathHelper.lerp(random.nextFloat(), lifeSpan[0], lifeSpan[1]));
                    particle.setSize((float) MathHelper.lerp(random.nextFloat(), size[0], size[1]));
                    particle.setColor((float) MathHelper.lerp(random.nextFloat(), color[0][0], color[0][1]),
                            (float) MathHelper.lerp(random.nextFloat(), color[1][0], color[1][1]),
                            (float) MathHelper.lerp(random.nextFloat(), color[2][0], color[2][1]));
                    float alpha = (float) MathHelper.lerp(random.nextFloat(), color[3][0], color[3][1]);
                    particle.setAlpha(model.renderInvisible ? 0.15f * alpha : alpha);
                    particle.setGravity((float) gravity);
                    particle.setCollide(collide);

                    manager.addEffect(particle);
                }
            }
        }
    }

    public void release() {
        released = true;
    }
}
