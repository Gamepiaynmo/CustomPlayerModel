package com.gpiay.cpm.network.packet;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.config.CPMConfig;
import com.gpiay.cpm.network.NetworkHandler;
import com.gpiay.cpm.network.PacketEncoder;
import com.gpiay.cpm.network.TaskScheduler;
import com.gpiay.cpm.server.ModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.File;

public class QueryModelPacket implements PacketEncoder {
    private final String modelId;

    public QueryModelPacket(String modelId) {
        this.modelId = modelId;
    }

    public QueryModelPacket(PacketBuffer buffer) {
        modelId = buffer.readUtf();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUtf(modelId);
    }

    public void handleClient(TaskScheduler scheduler) {
        File modelFile = ModelManager.findModelFileFromCache(modelId);
        if (modelFile != null && CPMConfig.sendModels() && CPMMod.cpmClient.isServerModded) {
            scheduler.execute(() -> {
                NetworkHandler.send(new ModelDataPacket(modelFile));
                Minecraft.getInstance().gui.getChat().addMessage(new TranslationTextComponent("text.cpm.modelUploaded"));
            });
        }
    }

    public void handleServer(TaskScheduler scheduler, ServerPlayerEntity player) {
        File modelFile = ModelManager.findModelFileFromCache(modelId);
        if (modelFile != null) {
            scheduler.execute(() -> NetworkHandler.send(player, new ModelDataPacket(modelFile)));
        }
    }
}
