package com.gpiay.cpm.network.packet;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ClientCPMAttachment;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.PacketEncoder;
import com.gpiay.cpm.network.TaskScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;

public class UpdateKeyStatePacket implements PacketEncoder {
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

    public void handleClient(TaskScheduler scheduler) {
        scheduler.execute(() -> {
            PlayerEntity player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
            if (player != null)
                AttachmentProvider.getEntityAttachment(player).ifPresent(attachment ->
                        ((ClientCPMAttachment) attachment).updateCustomKeyState(keyStates));
        });
    }

    public void handleServer(TaskScheduler scheduler, ServerPlayerEntity player) {
        scheduler.execute(() -> {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), this);
        });
    }
}
