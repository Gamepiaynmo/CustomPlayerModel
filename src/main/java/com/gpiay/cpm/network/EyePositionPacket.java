package com.gpiay.cpm.network;

import com.gpiay.cpm.server.ServerCPMCapability;
import com.gpiay.cpm.server.capability.CPMCapability;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EyePositionPacket {
    private Vector3d position;

    public EyePositionPacket(Vector3d position) {
        this.position = position;
    }

    public EyePositionPacket(PacketBuffer buffer) {
        position = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeDouble(position.x);
        buffer.writeDouble(position.y);
        buffer.writeDouble(position.z);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ctx.get().getSender().getCapability(CPMCapability.CAPABILITY).ifPresent(cap
                -> ((ServerCPMCapability) cap).setEyePosition(position)));

        ctx.get().setPacketHandled(true);
    }
}
