package com.gpiay.cpm.network.packet;

import com.google.common.collect.Lists;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ModelEntry;
import com.gpiay.cpm.model.ModelInfo;
import com.gpiay.cpm.network.PacketEncoder;
import com.gpiay.cpm.network.TaskScheduler;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public class ModelListPacket implements PacketEncoder {
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
            modelList.add(new ModelEntry(info, false));
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(modelList.size());
        for (ModelEntry entry : modelList)
            entry.info.toBuffer(buffer);
    }

    public void handleClient(TaskScheduler scheduler) {
        scheduler.execute(() -> CPMMod.cpmClient.showModelSelectionGui(modelList));
    }
}
