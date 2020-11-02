package com.gpiay.cpm.network;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ClientCPMCapability;
import com.gpiay.cpm.server.capability.CPMCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class UpdateKeyStatePacket {
    private boolean[] keyStates;
    private UUID uuid;

    public UpdateKeyStatePacket(boolean[] keyStates, PlayerEntity entity) {
        this.keyStates = keyStates;
        this.uuid = entity.getUniqueID();
    }

    public UpdateKeyStatePacket(PacketBuffer buffer) {
        this.keyStates = new boolean[CPMMod.customKeyCount];
        for (int i = 0; i < CPMMod.customKeyCount; i++)
            keyStates[i] = buffer.readBoolean();
        this.uuid = buffer.readUniqueId();
    }

    public void toBytes(PacketBuffer buffer) {
        for (int i = 0; i < CPMMod.customKeyCount; i++)
            buffer.writeBoolean(keyStates[i]);
        buffer.writeUniqueId(uuid);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER ?
                () -> {
                    Networking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> ctx.get().getSender()), this);
                } :
                () -> {
                    Minecraft.getInstance().world.getPlayerByUuid(uuid).getCapability(CPMCapability.CAPABILITY).ifPresent(cap -> {
                        ((ClientCPMCapability) cap).updateCustomKeyState(keyStates);
                    });
                });

        ctx.get().setPacketHandled(true);
    }
}
