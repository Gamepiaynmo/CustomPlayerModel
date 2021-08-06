package com.gpiay.cpm.event;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMCapability;
import com.gpiay.cpm.model.ModelInstance;
import com.gpiay.cpm.util.math.Matrix4d;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
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
                ModelInstance.baseModelView = new Matrix4d(event.getMatrixStack().last().pose()).inv();
            }
        });
    }
}
