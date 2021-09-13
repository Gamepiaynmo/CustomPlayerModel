package com.gpiay.cpm.mixin;

import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.entity.AttachmentProvider;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.INameable;
import net.minecraft.world.World;
#if FORGE
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeEntity;
#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
#if FORGE
public abstract class MixinEntity extends CapabilityProvider<Entity> implements INameable, ICommandSource, IForgeEntity {
    protected MixinEntity(Class<Entity> baseClass) {
        super(baseClass);
    }
#elif FABRIC
public abstract class MixinEntity implements INameable, ICommandSource {
#endif
    @Shadow
    public World level;

    @Shadow
    private net.minecraft.util.math.vector.Vector3d position;

    @Inject(
            at = @At("RETURN"),
            method = "Lnet/minecraft/entity/Entity;getEyePosition(F)Lnet/minecraft/util/math/vector/Vector3d;",
            cancellable = true
    )
    public void getEyePosition(float partial, CallbackInfoReturnable<net.minecraft.util.math.vector.Vector3d> info) {
        if (CPMConfig.customEyePosition()) {
            AttachmentProvider.getEntityAttachment((Entity) (Object) this).ifPresent(attachment -> {
                net.minecraft.util.math.vector.Vector3d eyePosition = attachment.changeEyePosition(partial);

                if (eyePosition != null)
                    info.setReturnValue(position.add(eyePosition));
            });
        }
    }
}
