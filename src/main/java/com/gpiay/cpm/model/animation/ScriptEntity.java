package com.gpiay.cpm.model.animation;

import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.server.capability.CPMCapability;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Function;

public class ScriptEntity {
    private static final Map<Pose, String> poseNames = Maps.newEnumMap(Pose.class);

    private final LivingEntity entity;
    private final float limbSwing;
    private final float limbSwingAmount;
    private float ageInTicks;
    private final float netHeadYaw;
    private final float headPitch;
    private final float partialTicks;
    private final double scale;

    public ScriptEntity(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                        float headPitch, float scaleFactor, float partialTicks, double scale) {
        this.entity = entity;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.partialTicks = partialTicks;
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

    public double getLimbSwing() { return limbSwing; }
    public double getLimbSwingAmount() { return limbSwingAmount; }
    public double getAge() { return entity.ticksExisted + partialTicks; }
    public double getHeadYaw() { return netHeadYaw; }
    public double getHeadPitch() { return headPitch; }
    public double getPartial() { return partialTicks; }
    public double getScale() { return scale; }

    public double getHealth() { return entity.getHealth(); }
    public double getFoodLevel() { return as(PlayerEntity.class, entity -> (double) entity.getFoodStats().getFoodLevel(), 0); }
    public double getHurtTime() { return entity.hurtTime - partialTicks; }
    public double getPosX() { return MathHelper.lerp(entity.prevPosX, entity.posX, partialTicks); }
    public double getPosY() { return MathHelper.lerp(entity.prevPosY, entity.posY, partialTicks); }
    public double getPosZ() { return MathHelper.lerp(entity.prevPosZ, entity.posZ, partialTicks); }
    public double getMotionX() { return entity.getMotion().x; }
    public double getMotionY() { return entity.getMotion().y; }
    public double getMotionZ() { return entity.getMotion().z; }
    public double getBodyYaw() { return MathHelper.lerp(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks); }
    public double getSwingProgress() { return entity.getSwingProgress(partialTicks); }

    public String getPose() { return poseNames.get(entity.getPose()); }

    public boolean isAlive() { return entity.isAlive(); }
    public boolean isBurning() { return entity.isBurning(); }
    public boolean isGlowing() { return entity.isGlowing(); }
    public boolean isHurt() { return entity.hurtTime > 0; }
    public boolean isInLava() { return entity.isInLava(); }
    public boolean isInWater() { return entity.isInWater(); }
    public boolean isInvisible() { return entity.isInvisible(); }
    public boolean isOnGround() { return entity.onGround; }
    public boolean isRiding() { return entity.getRidingEntity() != null; }
    public boolean isSneaking() { return entity.shouldRenderSneaking(); }
    public boolean isSprinting() { return entity.isSprinting(); }
    public boolean isWet() { return entity.isWet(); }

    public ScriptItem getMainHandItem() { return new ScriptItem(entity.getHeldItemMainhand()); }
    public ScriptItem getOffHandItem() { return new ScriptItem(entity.getHeldItemOffhand()); }
    public ScriptItem getRightHandItem() { return entity.getPrimaryHand() == HandSide.RIGHT ? getMainHandItem() : getOffHandItem(); }
    public ScriptItem getLeftHandItem() { return entity.getPrimaryHand() == HandSide.LEFT ? getMainHandItem() : getOffHandItem(); }
    public ScriptItem getHelmetItem() { return new ScriptItem(entity.getItemStackFromSlot(EquipmentSlotType.HEAD)); }
    public ScriptItem getChestplateItem() { return new ScriptItem(entity.getItemStackFromSlot(EquipmentSlotType.CHEST)); }
    public ScriptItem getLeggingsItem() { return new ScriptItem(entity.getItemStackFromSlot(EquipmentSlotType.LEGS)); }
    public ScriptItem getBootsItem() { return new ScriptItem(entity.getItemStackFromSlot(EquipmentSlotType.FEET)); }
    public ScriptItem getInventoryItem(int index) {
        if (entity instanceof PlayerEntity) {
            NonNullList<ItemStack> inventory = ((PlayerEntity) entity).inventory.mainInventory;
            if (index >= 0 && index < inventory.size())
                return new ScriptItem(inventory.get(index));
        }

        return ScriptItem.empty;
    }

    public ScriptEffect getPotionEffect(String effectName) {
        Effect effect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(effectName));
        if (effect != null) return new ScriptEffect(entity.getActivePotionEffect(effect));
        return ScriptEffect.empty;
    }

    public boolean isCustomKeyDown(int index) {
        if (index < 0 || index >= CPMMod.customKeyCount)
            return false;

        return entity.getCapability(CPMCapability.CAPABILITY).map(cap -> ((ClientCPMCapability) cap).customKeys[index]).orElse(false);
    }

    static {
        poseNames.put(Pose.STANDING, "standing");
        poseNames.put(Pose.SNEAKING, "sneaking");
        poseNames.put(Pose.SLEEPING, "sleeping");
        poseNames.put(Pose.SWIMMING, "swimming");
        poseNames.put(Pose.FALL_FLYING, "elytra_flying");
        poseNames.put(Pose.SPIN_ATTACK, "trident_attacking");
        poseNames.put(Pose.DYING, "dying");
    }
}
