package com.gpiay.cpm.network.packet;

import com.gpiay.cpm.entity.AttachmentProvider;
import com.gpiay.cpm.entity.ServerCPMAttachment;
import com.gpiay.cpm.network.PacketEncoder;
import com.gpiay.cpm.network.TaskScheduler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

public class EyePositionPacket implements PacketEncoder {
    private final Vector3d position;

    public EyePositionPacket(double x, double y, double z) {
        this.position = new Vector3d(x, y, z);
    }

    public EyePositionPacket(PacketBuffer buffer) {
        position = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeDouble(position.x);
        buffer.writeDouble(position.y);
        buffer.writeDouble(position.z);
    }

    public void handleServer(TaskScheduler scheduler, ServerPlayerEntity player) {
        AttachmentProvider.getEntityAttachment(player).ifPresent(attachment ->
                ((ServerCPMAttachment) attachment).setEyePosition(position));
    }
}
