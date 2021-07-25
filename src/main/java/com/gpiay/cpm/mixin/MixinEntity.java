package com.gpiay.cpm.mixin;

import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.server.ServerCPMCapability;
import com.gpiay.cpm.server.ServerConfig;
import com.gpiay.cpm.server.capability.CPMCapability;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.INameable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity extends CapabilityProvider<Entity> implements INameable, ICommandSource, IForgeEntity {
    protected MixinEntity(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Shadow
    public World level;

    @Shadow
    private net.minecraft.util.math.vector.Vector3d position;

    @Inject(
            at = @At("RETURN"),
            method = "getEyePosition(F)Lnet/minecraft/util/math/vector/Vector3d;",
            cancellable = true
    )
    public void getEyePosition(float partial, CallbackInfoReturnable<net.minecraft.util.math.vector.Vector3d> info) {
        if (ServerConfig.CUSTOM_EYE_POSITION.get()) {
            this.getCapability(CPMCapability.CAPABILITY).ifPresent(capability -> {
                net.minecraft.util.math.vector.Vector3d eyePosition = null;
                if (level.isClientSide) {
                    ModelInstance model = ((ClientCPMCapability) capability).getModel();
                    if (model != null && model.isReady()) {
                        Vector3d pos = model.getEyePosition(partial);
                        if (pos != null)
                            eyePosition = new net.minecraft.util.math.vector.Vector3d(pos.x, pos.y, pos.z);
                    }
                } else {
                    eyePosition = ((ServerCPMCapability) capability).getEyePosition();
                }

                if (eyePosition != null)
                    info.setReturnValue(position.add(eyePosition));
            });
        }
    }
}
