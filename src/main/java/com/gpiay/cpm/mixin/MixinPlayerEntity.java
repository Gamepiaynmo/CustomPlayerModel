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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }

    @Inject(
            at = @At("RETURN"),
            method = "getDimensions(Lnet/minecraft/entity/Pose;)Lnet/minecraft/entity/EntitySize;",
            cancellable = true
    )
    public void getDimensions(Pose pose, CallbackInfoReturnable<EntitySize> info) {
        EntitySize entitySize = info.getReturnValue();

        Optional<ICPMCapability> capability = this.getCapability(CPMCapability.CAPABILITY).resolve();
        if (ServerConfig.CUSTOM_BOUNDING_BOX.get() && capability.isPresent()) {
            if (this.level.isClientSide) {
                if (CPMMod.cpmClient.isServerModded) {
                    ModelInstance model = ((ClientCPMCapability) capability.get()).getModel();
                    if (model != null)
                        entitySize = model.getModelPack().getEntitySize(this, pose, entitySize, capability.get().getScale());
                }
            } else {
                ModelInfo model = ((ServerCPMCapability) capability.get()).getModel();
                if (model != null)
                    entitySize = model.getEntitySize(this, pose, entitySize, capability.get().getScale());
            }
        }

        info.setReturnValue(entitySize);
    }
}
