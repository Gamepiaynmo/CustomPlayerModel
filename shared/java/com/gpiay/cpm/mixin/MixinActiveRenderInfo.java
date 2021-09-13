package com.gpiay.cpm.mixin;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.entity.ICPMAttachment;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Vector3d;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ActiveRenderInfo.class)
public abstract class MixinActiveRenderInfo {
    @Shadow
    protected abstract void setPosition(net.minecraft.util.math.vector.Vector3d p_216774_1_);

    @Redirect(
            method = "setup(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/entity/Entity;ZZF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;setPosition(DDD)V"
            )
    )
    public void setup(ActiveRenderInfo renderInfo, double x, double y, double z, IBlockReader level, Entity camera, boolean firstPerson, boolean mirror, float partial) {
        net.minecraft.util.math.vector.Vector3d pos = new net.minecraft.util.math.vector.Vector3d(x, y, z);
        Optional<ICPMAttachment> optional = AttachmentProvider.getEntityAttachment(camera);
        if (optional.isPresent()) {
            ModelInstance model = ((ClientCPMAttachment) optional.get()).getModel();
            if (model != null && model.isReady()) {
                Vector3d eyePosition = model.getEyePosition(partial);
                if (eyePosition != null)
                    pos = camera.position().add(eyePosition.x, eyePosition.y, eyePosition.z);
            }
        }

        setPosition(pos);
    }
}
