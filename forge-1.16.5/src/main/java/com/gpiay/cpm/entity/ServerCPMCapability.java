package com.gpiay.cpm.entity;

import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.packet.UpdateModelPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class ServerCPMCapability extends ServerCPMAttachment implements ICPMCapability {
    public ServerCPMCapability(LivingEntity entity) {
        super(entity);
    }

    @Override
    protected void syncAttachment() {
        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getEntity),
                new UpdateModelPacket(getEntity()));
    }
}
