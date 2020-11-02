package com.gpiay.cpm.model.skeleton;

import com.gpiay.cpm.model.EnumAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.model.element.VanillaBone;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.renderer.entity.model.SlimeModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;

public class SlimeSkeleton extends Skeleton<SlimeModel<LivingEntity>> {
    protected VanillaBone body;
    private final float size;

    public float squishAmount;
    public float squishFactor;
    public float prevSquishFactor;
    private boolean wasOnGround;
    private double squishGround;

    public SlimeSkeleton(Map<String, Float> param) {
        super(new SlimeModel<>(16));
        size = param.getOrDefault("size", 4.0f);

        body = registerVanillaBone("body");
    }

    @Override
    public void tick(LivingEntity entity) {
        this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5F;
        this.prevSquishFactor = this.squishFactor;
        double motion = entity.getMotion().mul(1, 0, 1).length() * 4;
        squishGround += motion;
        squishGround = MathHelper.positiveModulo(squishGround, Math.PI * 2);

        if (entity.onGround) {
            if (this.wasOnGround)
                this.squishAmount = MathHelper.sin((float) squishGround) * (float) motion;
            else this.squishAmount = -0.5F;
        } else if (this.wasOnGround) {
            this.squishAmount = 1.0F;
        }

        this.wasOnGround = entity.onGround;
        this.squishAmount *= 0.6F;
    }

    @Override
    protected void adjustBones(LivingEntity entity, double scale, float partialTicks, boolean firstPerson) {
        super.adjustBones(entity, scale, partialTicks, firstPerson);

        float size = this.size;
        if (entity instanceof SlimeEntity)
            size = ((SlimeEntity) entity).getSlimeSize();

        float f2 = MathHelper.lerp(partialTicks, prevSquishFactor, squishFactor) / (size * 0.5F + 1.0F);
        if (entity instanceof SlimeEntity) {
            SlimeEntity slimeEntity = (SlimeEntity) entity;
            f2 = MathHelper.lerp(partialTicks, slimeEntity.prevSquishFactor, slimeEntity.squishFactor) / (size * 0.5F + 1.0F);
        }

        float f3 = 1.0F / (f2 + 1.0F);
        float yScale = 1.0F / f3 * size;
        body.scale(f3 * size, yScale, f3 * size).offset(0, 24 * scale, 0);
        none.scale(f3 * size, yScale, f3 * size).offset(0, 24 * scale, 0);
    }

    @Override
    public void addAttachments(EnumAttachment attachment, ModelInstance instance) {
        switch (attachment) {
            case HELMET:
            case SKULL:
                addBuiltinAttachment(attachment, instance, "body");
                break;
            case CAPE:
                addBuiltinAttachment(attachment, instance, "body", new Vector3d(0, 0, 4),
                        Vector3d.Zero.cpy(), Vector3d.One.cpy().scl(1 / size));
            case ELYTRA:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(0, 6, 3),
                        Vector3d.Zero.cpy(), Vector3d.One.cpy().scl(1 / size));
                break;
            case ITEM_LEFT:
                addBuiltinAttachment(attachment, instance, "body", new Vector3d(5, 2, -3.5),
                        new Vector3d(0, -25, 0), Vector3d.One.cpy().scl(1 / size));
                break;
            case ITEM_RIGHT:
                addBuiltinAttachment(attachment, instance, "body", new Vector3d(-5, 2, -3.5),
                        new Vector3d(0, 25, 0), Vector3d.One.cpy().scl(1 / size));
                break;
            case PARROT_LEFT:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(4, 8, 0),
                        Vector3d.Zero.cpy(), Vector3d.One.cpy().scl(1 / size));
                break;
            case PARROT_RIGHT:
                addBuiltinAttachment(attachment, instance, "none", new Vector3d(-4, 8, 0),
                        Vector3d.Zero.cpy(), Vector3d.One.cpy().scl(1 / size));
                break;
            default:
                super.addAttachments(attachment, instance);
                break;
        }
    }
}
