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
    private final boolean[] keyStates;
    private final UUID uuid;

    public UpdateKeyStatePacket(boolean[] keyStates, PlayerEntity entity) {
        this.keyStates = keyStates;
        this.uuid = entity.getUUID();
    }

    public UpdateKeyStatePacket(PacketBuffer buffer) {
        this.keyStates = new boolean[CPMMod.customKeyCount];
        for (int i = 0; i < CPMMod.customKeyCount; i++)
            keyStates[i] = buffer.readBoolean();
        this.uuid = buffer.readUUID();
    }

    public void toBytes(PacketBuffer buffer) {
        for (int i = 0; i < CPMMod.customKeyCount; i++)
            buffer.writeBoolean(keyStates[i]);
        buffer.writeUUID(uuid);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER ?
                () -> Networking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ctx.get().getSender()), this) :
                () -> {
                    PlayerEntity player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
                    if (player != null)
                        player.getCapability(CPMCapability.CAPABILITY).ifPresent(cap ->
                                ((ClientCPMCapability) cap).updateCustomKeyState(keyStates));
                });

        ctx.get().setPacketHandled(true);
    }
}
