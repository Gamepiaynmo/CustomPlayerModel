package com.gpiay.cpm.model.element;

import com.gpiay.cpm.client.particle.CPMParticle;
import com.gpiay.cpm.model.IComponent;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Matrix4d;
import com.gpiay.cpm.util.math.Quat4d;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Random;

public class ParticleEmitter extends ModelElement {
    public String parentName = "none";
    public String texture;
    public Vector3d posRange = Vector3d.Zero.cpy();
    public double dirRange;
    public double[] angle = new double[2];
    public double[] speed = new double[2];
    public double[] rotSpeed = new double[2];
    public double[] lifeSpan = new double[] { 1, 1 };
    public double density;
    public int[] animation = new int[] { 1, 1 };
    public double[][] color = new double[4][];
    public double[] size = new double[] { 1, 1 };
    public double gravity;
    public boolean collide;

    public ParticleEmitter() {
        for (int i = 0; i < 4; i++)
            color[i] = new double[] { 1, 1 };
    }

    @Override
    public Instance instantiate(ModelInstance model) {
        IModelBone parent = findParent(model, parentName);
        Instance instance = new Instance(name, model, parent);
        parent.setCalculateTransform();

        instance.texture = texture;
        instance.posRange = posRange.cpy();
        instance.dirRange = dirRange;
        instance.angle = Arrays.copyOf(angle, 2);
        instance.speed = Arrays.copyOf(speed, 2);
        instance.rotSpeed = Arrays.copyOf(rotSpeed, 2);
        instance.lifeSpan = Arrays.copyOf(lifeSpan, 2);
        instance.density = density;
        instance.animation = animation;
        for (int i = 0; i < 4; i++)
            instance.color[i] = Arrays.copyOf(color[i], 2);
        instance.size = Arrays.copyOf(size, 2);
        instance.gravity = gravity;
        instance.collide = collide;

        return instance;
    }

    public static class Instance extends ModelElement.Instance implements ModelElement.IParented, ModelElement.ITextured, IComponent {
        private static final Random random = new Random();
        public final IModelBone parent;
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

        private double timer;
        public boolean released = false;

        public Instance(String name, ModelInstance model, IModelBone parent) {
            super(name, model);
            this.parent = parent;
        }

        @Override
        public IModelBone getParent() {
            return parent;
        }

        @Override
        public String getTexture() {
            return texture;
        }

        @Override
        public void setTexture(String texture) {
            if (model.getModelPack().hasTexture(texture))
                this.texture = texture;
        }

        @Override
        public void update() {
            if (parent.isVisible()) {
                Matrix4d transform = model.getBoneCurrentMatrix(parent);
                Minecraft client = Minecraft.getInstance();
                ParticleManager manager = client.particleEngine;
                ClientWorld world = client.level;
                boolean renderInvisible = model.entity.isInvisible() && !model.entity.isInvisibleTo(Minecraft.getInstance().player);
                float baseAlpha = renderInvisible ? 0.15F : 1.0F;

                timer += 1;

                double EPS = 1e-4;
                if (density > EPS && timer > 0) {
                    Vector3d center = new Vector3d();
                    center.x = transform.val[12] + model.entity.getX();
                    center.y = transform.val[13] + model.entity.getY() + 1.5;
                    center.z = transform.val[14] + model.entity.getZ();

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
                        particle.setLifetime((int) MathHelper.lerp(random.nextFloat(), lifeSpan[0], lifeSpan[1]));
                        particle.setSize((float) MathHelper.lerp(random.nextFloat(), size[0], size[1]));
                        particle.setColor((float) MathHelper.lerp(random.nextFloat(), color[0][0], color[0][1]),
                                (float) MathHelper.lerp(random.nextFloat(), color[1][0], color[1][1]),
                                (float) MathHelper.lerp(random.nextFloat(), color[2][0], color[2][1]));
                        float alpha = (float) MathHelper.lerp(random.nextFloat(), color[3][0], color[3][1]);
                        particle.setAlphaF(baseAlpha * alpha);
                        particle.setGravity((float) gravity);
                        particle.setCollide(collide);

                        manager.add(particle);
                    }
                }
            }
        }

        public void release() {
            released = true;
        }
    }
}
