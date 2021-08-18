package com.gpiay.cpm.model.animation;

import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.function.Function;

public class ScriptEntity {
    private static final Map<Pose, String> poseNames = Maps.newEnumMap(Pose.class);

    private final LivingEntity entity;
    private final float animPos;
    private final float animSpeed;
    private float age;
    private final float headYaw;
    private final float headPitch;
    private final float partial;
    private final double scale;

    public ScriptEntity(LivingEntity entity, float animPos, float animSpeed, float age, float headYaw,
            float headPitch, float partial, double scale) {
        this.entity = entity;
        this.animPos = animPos;
        this.animSpeed = animSpeed;
        this.age = age;
        this.headYaw = headYaw;
        this.headPitch = headPitch;
        this.partial = partial;
        this.scale = scale;
    }

    private <E extends LivingEntity> double as(Class<E> clazz, Function<E, Double> function, double defaultVal) {
        try {
            return function.apply(clazz.cast(entity));
        } catch (ClassCastException e) {
            return defaultVal;
        }
    }

    private <E extends LivingEntity> boolean as(Class<E> clazz, Function<E, Boolean> function, boolean defaultVal) {
        try {
            return function.apply(clazz.cast(entity));
        } catch (ClassCastException e) {
            return defaultVal;
        }
    }

    public double getLimbSwing() { return animPos; }
    public double getLimbSwingAmount() { return animSpeed; }
    public double getAnimPosition() { return animPos; }
    public double getAnimSpeed() { return animSpeed; }
    public double getAge() { return entity.tickCount + partial; }
    public double getHeadYaw() { return headYaw; }
    public double getHeadPitch() { return headPitch; }
    public double getPartial() { return partial; }
    public double getScale() { return scale; }

    public double getHealth() { return entity.getHealth(); }
    public double getMaxHealth() { return entity.getMaxHealth(); }
    public double getFoodLevel() { return as(PlayerEntity.class, entity -> (double) entity.getFoodData().getFoodLevel(), 0); }
    public double getSaturationLevel() { return as(PlayerEntity.class, entity -> (double) entity.getFoodData().getSaturationLevel(), 0); }
    public double getHurtTime() { return entity.hurtTime - partial; }
    public double getPosX() { return MathHelper.lerp(partial, entity.xo, entity.getX()); }
    public double getPosY() { return MathHelper.lerp(partial, entity.yo, entity.getY()); }
    public double getPosZ() { return MathHelper.lerp(partial, entity.zo, entity.getZ()); }
    public double getMotionX() { return entity.getDeltaMovement().x; }
    public double getMotionY() { return entity.getDeltaMovement().y; }
    public double getMotionZ() { return entity.getDeltaMovement().z; }
    public double getBodyYaw() { return MathHelper.lerp(partial, entity.yBodyRotO, entity.yBodyRot); }
    public double getSwingProgress() { return entity.getAttackAnim(partial); }
    public double getAttackAnim() { return entity.getAttackAnim(partial); }

    public String getPose() { return poseNames.get(entity.getPose()); }

    public boolean isAlive() { return entity.isAlive(); }
    public boolean isBurning() { return entity.isOnFire(); }
    public boolean isGlowing() { return entity.isGlowing(); }
    public boolean isHurt() { return entity.hurtTime > 0; }
    public boolean isInLava() { return entity.isInLava(); }
    public boolean isInWater() { return entity.isInWater(); }
    public boolean isInvisible() { return entity.isInvisible(); }
    public boolean isOnGround() { return entity.isOnGround(); }
    public boolean isRiding() { return entity.getVehicle() != null; }
    public boolean isSneaking() { return entity.isCrouching(); }
    public boolean isCrouching() { return entity.isCrouching(); }
    public boolean isSprinting() { return entity.isSprinting(); }
    public boolean isWet() { return entity.isInWaterRainOrBubble(); }

    public ScriptItem getMainHandItem() { return new ScriptItem(entity.getMainHandItem()); }
    public ScriptItem getOffHandItem() { return new ScriptItem(entity.getOffhandItem()); }
    public ScriptItem getRightHandItem() { return entity.getMainArm() == HandSide.RIGHT ? getMainHandItem() : getOffHandItem(); }
    public ScriptItem getLeftHandItem() { return entity.getMainArm() == HandSide.LEFT ? getMainHandItem() : getOffHandItem(); }
    public ScriptItem getHelmetItem() { return new ScriptItem(entity.getItemBySlot(EquipmentSlotType.HEAD)); }
    public ScriptItem getChestplateItem() { return new ScriptItem(entity.getItemBySlot(EquipmentSlotType.CHEST)); }
    public ScriptItem getLeggingsItem() { return new ScriptItem(entity.getItemBySlot(EquipmentSlotType.LEGS)); }
    public ScriptItem getBootsItem() { return new ScriptItem(entity.getItemBySlot(EquipmentSlotType.FEET)); }
    public ScriptItem getInventoryItem(int index) {
        if (entity instanceof PlayerEntity) {
            NonNullList<ItemStack> inventory = ((PlayerEntity) entity).inventory.items;
            if (index >= 0 && index < inventory.size())
                return new ScriptItem(inventory.get(index));
        }

        return ScriptItem.empty;
    }

    public ScriptEffect getPotionEffect(String effectName) {
        Effect effect = Registry.MOB_EFFECT.get(new ResourceLocation(effectName));
        if (effect != null) return new ScriptEffect(entity.getEffect(effect));
        return ScriptEffect.empty;
    }

    public boolean isCustomKeyDown(int index) {
        if (index < 0 || index >= CPMMod.customKeyCount)
            return false;

        return AttachmentProvider.getEntityAttachment(entity).map(cap -> ((ClientCPMAttachment) cap).customKeys[index]).orElse(false);
    }

    static {
        poseNames.put(Pose.STANDING, "standing");
        poseNames.put(Pose.CROUCHING, "crouching");
        poseNames.put(Pose.SLEEPING, "sleeping");
        poseNames.put(Pose.SWIMMING, "swimming");
        poseNames.put(Pose.FALL_FLYING, "elytra_flying");
        poseNames.put(Pose.SPIN_ATTACK, "trident_attacking");
        poseNames.put(Pose.DYING, "dying");
    }
}
