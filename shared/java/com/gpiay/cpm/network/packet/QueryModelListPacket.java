package com.gpiay.cpm.network.packet;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.PacketEncoder;
import com.gpiay.cpm.network.TaskScheduler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public class QueryModelListPacket implements PacketEncoder {
    public QueryModelListPacket() {}
    public QueryModelListPacket(PacketBuffer buffer) {}

    public void toBytes(PacketBuffer buffer) {}

    public void handleServer(TaskScheduler scheduler, ServerPlayerEntity player) {
        scheduler.execute(() -> NetworkHandler.send(player,
                new ModelListPacket(CPMMod.cpmServer.modelManager.getModelList(player))));
    }
}
