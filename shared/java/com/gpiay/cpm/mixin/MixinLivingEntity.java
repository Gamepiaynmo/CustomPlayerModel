package com.gpiay.cpm.mixin;

import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ICPMAttachment;
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
        float eyeHeight = info.getReturnValueF();
        Optional<ICPMAttachment> attachment = AttachmentProvider.getEntityAttachment((LivingEntity) (Entity) this);
        if (CPMConfig.customEyeHeight() && attachment.isPresent())
            eyeHeight = attachment.get().changeEyeHeight(pose, entitySize, eyeHeight);

        info.setReturnValue(eyeHeight);
    }

    @Inject(
            at = @At("RETURN"),
            method = "getDimensions(Lnet/minecraft/entity/Pose;)Lnet/minecraft/entity/EntitySize;",
            cancellable = true
    )
    public void getDimensions(Pose pose, CallbackInfoReturnable<EntitySize> info) {
        EntitySize entitySize = info.getReturnValue();
        Optional<ICPMAttachment> attachment = AttachmentProvider.getEntityAttachment((LivingEntity) (Entity) this);
        if (CPMConfig.customBoundingBox() && attachment.isPresent())
            entitySize = attachment.get().changeEntitySize(pose, entitySize);

        info.setReturnValue(entitySize);
    }
}
