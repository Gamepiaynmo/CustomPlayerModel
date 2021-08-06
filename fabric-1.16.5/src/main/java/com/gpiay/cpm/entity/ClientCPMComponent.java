package com.gpiay.cpm.entity;

import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.packet.UpdateKeyStatePacket;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import net.minecraft.entity.LivingEntity;

public class ClientCPMComponent extends ClientCPMAttachment implements ICPMComponent, ClientTickingComponent {
    public ClientCPMComponent(LivingEntity entity) {
        super(entity);
    }

    @Override
    protected void syncKeyState(boolean[] keyStates) {
        NetworkHandler.send(new UpdateKeyStatePacket(keyStates, getPlayerEntity()));
    }

    @Override
    public void clientTick() {
        update();
    }
}
