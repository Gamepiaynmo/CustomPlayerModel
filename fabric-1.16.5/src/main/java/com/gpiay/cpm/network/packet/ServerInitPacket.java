package com.gpiay.cpm.network.packet;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.config.Configs;
import com.gpiay.cpm.network.PacketEncoder;
import com.gpiay.cpm.network.TaskScheduler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

public class ServerInitPacket implements PacketEncoder {
    public ServerInitPacket() {}

    public ServerInitPacket(PacketBuffer buffer) {
        CPMConfig.serverConfigs.customEyeHeight = buffer.readBoolean();
        CPMConfig.serverConfigs.customBoundingBox = buffer.readBoolean();
        CPMConfig.serverConfigs.customEyePosition = buffer.readBoolean();
        CPMConfig.serverConfigs.receiveModels = buffer.readBoolean();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(CPMConfig.customEyeHeight());
        buffer.writeBoolean(CPMConfig.customBoundingBox());
        buffer.writeBoolean(CPMConfig.customEyePosition());
        buffer.writeBoolean(CPMConfig.receiveModels());
    }

    public void handleClient(TaskScheduler scheduler) {
        CPMMod.cpmClient.isServerModded = true;
    }
}
