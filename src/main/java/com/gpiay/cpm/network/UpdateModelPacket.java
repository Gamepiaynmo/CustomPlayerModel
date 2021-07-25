package com.gpiay.cpm.network;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.server.capability.CPMCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class UpdateModelPacket {
    private CompoundNBT modelNBT;
    private final boolean isPlayer;
    private int entityId;
    private UUID uuid;

    public UpdateModelPacket(LivingEntity entity) {
        entity.getCapability(CPMCapability.CAPABILITY).ifPresent(cap -> modelNBT = cap.serializeNBT());
        isPlayer = entity instanceof PlayerEntity;
        if (isPlayer) uuid = entity.getUUID();
        else entityId = entity.getId();
    }

    public UpdateModelPacket(PacketBuffer buffer) {
        modelNBT = buffer.readNbt();
        isPlayer = buffer.readBoolean();
        if (isPlayer) uuid = buffer.readUUID();
        else entityId = buffer.readInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeNbt(modelNBT);
        buffer.writeBoolean(isPlayer);
        if (isPlayer) buffer.writeUUID(uuid);
        else buffer.writeInt(entityId);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CPMMod.cpmClient.isServerModded = true;
            ClientWorld world = Minecraft.getInstance().level;
            assert world != null;

            Entity entity;
            if (isPlayer) entity = world.getPlayerByUUID(uuid);
            else entity = world.getEntity(entityId);

            if (entity != null)
                entity.getCapability(CPMCapability.CAPABILITY).ifPresent(cap -> cap.deserializeNBT(modelNBT));
        });

        ctx.get().setPacketHandled(true);
    }
}
