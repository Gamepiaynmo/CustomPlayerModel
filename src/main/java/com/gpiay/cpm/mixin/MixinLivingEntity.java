package com.gpiay.cpm.mixin;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.server.ServerCPMCapability;
import com.gpiay.cpm.server.ServerConfig;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.server.capability.ICPMCapability;
import net.minecraft.entity.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> p_i48580_1_, World p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    @Inject(
            at = @At("RETURN"),
            method = "getEyeHeight(Lnet/minecraft/entity/Pose;Lnet/minecraft/entity/EntitySize;)F",
            cancellable = true
    )
    public void getEyeHeight(Pose pose, EntitySize entitySize, CallbackInfoReturnable<Float> info) {
        LivingEntity entity = (LivingEntity) (Entity) this;
        float eyeHeight = info.getReturnValueF();

        Optional<ICPMCapability> capability = this.getCapability(CPMCapability.CAPABILITY).resolve();
        if (ServerConfig.CUSTOM_EYE_HEIGHT.get() && capability.isPresent()) {
            if (this.level.isClientSide) {
                if (CPMMod.cpmClient.isServerModded) {
                    ModelInstance model = ((ClientCPMCapability) capability.get()).getModel();
                    if (model != null)
                        eyeHeight = model.getModelPack().getEntityEyeHeight(entity, pose, eyeHeight, capability.get().getScale());
                }
            } else {
                ModelInfo model = ((ServerCPMCapability) capability.get()).getModel();
                if (model != null)
                    eyeHeight = model.getEntityEyeHeight(entity, pose, eyeHeight, capability.get().getScale());
            }
        }

        info.setReturnValue(eyeHeight);
    }

    @Inject(
            at = @At("RETURN"),
            method = "getDimensions(Lnet/minecraft/entity/Pose;)Lnet/minecraft/entity/EntitySize;",
            cancellable = true
    )
    public void getDimensions(Pose pose, CallbackInfoReturnable<EntitySize> info) {
        LivingEntity entity = (LivingEntity) (Entity) this;
        EntitySize entitySize = info.getReturnValue();

        Optional<ICPMCapability> capability = this.getCapability(CPMCapability.CAPABILITY).resolve();
        if (ServerConfig.CUSTOM_BOUNDING_BOX.get() && capability.isPresent()) {
            if (this.level.isClientSide) {
                if (CPMMod.cpmClient.isServerModded) {
                    ModelInstance model = ((ClientCPMCapability) capability.get()).getModel();
                    if (model != null)
                        entitySize = model.getModelPack().getEntitySize(entity, pose, entitySize, capability.get().getScale());
                }
            } else {
                ModelInfo model = ((ServerCPMCapability) capability.get()).getModel();
                if (model != null)
                    entitySize = model.getEntitySize(entity, pose, entitySize, capability.get().getScale());
            }
        }

        info.setReturnValue(entitySize);
    }
}
