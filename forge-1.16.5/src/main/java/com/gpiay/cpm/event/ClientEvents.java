package com.gpiay.cpm.event;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.entity.ClientCPMCapability;
import com.gpiay.cpm.model.ModelBase;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Matrix4d;
import com.mrcrayfish.obfuscate.client.event.PlayerModelEvent;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CPMMod.cpmClient.onClientTick();
        }
    }

    @SubscribeEvent
    public void onPreRenderLiving(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<?>> event) {
        AttachmentProvider.getEntityAttachment(event.getEntity()).ifPresent(capability -> {
            if (((ClientCPMCapability) capability).getModel() != null) {
                ModelBase.baseModelView = new Matrix4d(event.getMatrixStack().last().pose()).inv();
            }
        });
    }

    @SubscribeEvent
    public void onRenderPlayerModel(PlayerModelEvent.Render.Pre event) {
        ClientCPMAttachment attachment = (ClientCPMAttachment) AttachmentProvider.getEntityAttachment(event.getPlayer()).orElse(null);
        ModelInstance modelInstance = attachment != null ? attachment.getModel() : null;
        if (modelInstance != null && modelInstance.isReady()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(ClientPlayerNetworkEvent.RespawnEvent event) {
        AttachmentProvider.getEntityAttachment(event.getOldPlayer()).ifPresent((oldCap) -> {
            AttachmentProvider.getEntityAttachment(event.getNewPlayer()).ifPresent((newCap) -> {
                CompoundNBT nbt = new CompoundNBT();
                oldCap.writeToNBT(nbt);
                newCap.readFromNBT(nbt);
            });
        });
    }

    @SubscribeEvent
    public void onPlayerLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        CPMMod.cpmClient.isServerModded = false;
    }
}
