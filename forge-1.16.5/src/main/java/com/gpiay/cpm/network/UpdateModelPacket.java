package com.gpiay.cpm.network;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.UUID;

public class UpdateModelPacket implements PacketEncoder {
    private CompoundNBT modelNBT;
    private final boolean isPlayer;
    private int entityId;
    private UUID uuid;

    public UpdateModelPacket(LivingEntity entity) {
        AttachmentProvider.getEntityAttachment(entity).ifPresent(cap -> {
            modelNBT = new CompoundNBT();
            cap.writeToNBT(modelNBT);
        });
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

    public void handleClient(TaskScheduler scheduler) {
        scheduler.execute(() -> {
            CPMMod.cpmClient.isServerModded = true;
            ClientWorld world = Minecraft.getInstance().level;
            assert world != null;

            Entity entity;
            if (isPlayer) entity = world.getPlayerByUUID(uuid);
            else entity = world.getEntity(entityId);

            if (entity != null)
                AttachmentProvider.getEntityAttachment(entity).ifPresent(cap -> cap.readFromNBT(modelNBT));
        });
    }
}
