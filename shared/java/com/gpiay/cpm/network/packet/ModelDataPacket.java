package com.gpiay.cpm.network.packet;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.network.PacketEncoder;
import com.gpiay.cpm.network.TaskScheduler;
import com.gpiay.cpm.server.ModelManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ModelDataPacket implements PacketEncoder {
    private byte[] data;

    public ModelDataPacket(File modelFile) {
        try {
            InputStream stream = new FileInputStream(modelFile);
            int len = stream.available();
            data = new byte[len];
            stream.read(data);
            stream.close();
        } catch (IOException e) {
            CPMMod.warn(e);
        }
    }

    public ModelDataPacket(PacketBuffer buffer) {
        data = buffer.readByteArray();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeByteArray(data);
    }

    public void handleClient(TaskScheduler scheduler) {
        scheduler.execute(() -> ModelManager.saveToCache(data));
    }

    public void handleServer(TaskScheduler scheduler, ServerPlayerEntity player) {
        if (CPMConfig.receiveModels())
            scheduler.execute(() -> ModelManager.saveToCache(data));
    }
}
