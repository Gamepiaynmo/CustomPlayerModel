package com.gpiay.cpm.network;

import com.gpiay.cpm.CPMMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class QueryModelListPacket {
    public QueryModelListPacket() {}
    public QueryModelListPacket(PacketBuffer buffer) {}

    public void toBytes(PacketBuffer buffer) {}

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new ModelListPacket(CPMMod.cpmServer.modelManager.getModelList(player)));
        });

        ctx.get().setPacketHandled(true);
    }
}
