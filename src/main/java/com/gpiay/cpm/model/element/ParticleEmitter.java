package com.gpiay.cpm.model.element;

import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Vector3d;

import java.util.Arrays;

public class ParticleEmitter {
    public String name;
    public String parentId = "none";
    public String texture;
    public Vector3d posRange = Vector3d.Zero.cpy();
    public double dirRange;
    public double[] angle = new double[2];
    public double[] speed = new double[2];
    public double[] rotSpeed = new double[2];
    public double[] lifeSpan = new double[]{ 1, 1 };
    public double density;
    public int[] animation = new int[] { 1, 1 };
    public double[][] color = new double[4][];
    public double[] size = new double[]{ 1, 1 };
    public double gravity;
    public boolean collide;

    public ParticleEmitter() {
        for (int i = 0; i < 4; i++)
            color[i] = new double[]{ 1, 1 };
    }

    public ParticleEmitterInstance instantiate(ModelInstance model) {
        IModelBone bone = model.getBone(parentId);
        if (bone == null)
            bone = model.getBone("none");
        ParticleEmitterInstance instance = new ParticleEmitterInstance(model, bone);
        if (bone instanceof ModelBone)
            ((ModelBone) bone).setCalculateTransform();

        instance.name = name;
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
}
