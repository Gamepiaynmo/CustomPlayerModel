package com.gpiay.cpm.network;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.server.CommonConfig;
import com.gpiay.cpm.server.ModelManager;
import com.gpiay.cpm.server.ServerConfig;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class ModelDataPacket {
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

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() != LogicalSide.SERVER || ServerConfig.RECEIVE_MODELS.get())
                ModelManager.saveToCache(data);
        });

        ctx.get().setPacketHandled(true);
    }
}
