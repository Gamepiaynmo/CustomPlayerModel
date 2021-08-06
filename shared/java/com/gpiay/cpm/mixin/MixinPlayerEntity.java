package com.gpiay.cpm.mixin;

import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ICPMAttachment;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
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
        Optional<ICPMAttachment> attachment = AttachmentProvider.getEntityAttachment(this);
        if (CPMConfig.customBoundingBox() && attachment.isPresent())
            entitySize = attachment.get().changeEntitySize(pose, entitySize);
        info.setReturnValue(entitySize);
    }
}
