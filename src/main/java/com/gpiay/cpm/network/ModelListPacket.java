package com.gpiay.cpm.network;

import com.google.common.collect.Lists;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ModelEntry;
import com.gpiay.cpm.model.ModelInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class ModelListPacket {
    List<ModelEntry> modelList;

    public ModelListPacket(List<ModelEntry> modelList) {
        this.modelList = modelList;
    }

    public ModelListPacket(PacketBuffer buffer) {
        modelList = Lists.newArrayList();
        int length = buffer.readInt();
        for (int i = 0; i < length; i++) {
            ModelInfo info = new ModelInfo();
            info.fromBuffer(buffer);
            modelList.add(new ModelEntry(buffer.readUtf(), info, false));
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(modelList.size());
        for (ModelEntry entry : modelList) {
            entry.info.toBuffer(buffer);
            buffer.writeUtf(entry.id);
        }
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CPMMod.cpmClient.showModelSelectionGui(modelList);
        });

        ctx.get().setPacketHandled(true);
    }
}
