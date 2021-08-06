package com.gpiay.cpm.entity;

import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.packet.UpdateKeyStatePacket;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class ClientCPMCapability extends ClientCPMAttachment implements ICPMCapability {
    public ClientCPMCapability(LivingEntity entity) {
        super(entity);
    }

    @Override
    protected void syncKeyState(boolean[] keyStates) {
        NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new UpdateKeyStatePacket(keyStates, getPlayerEntity()));
    }
}
