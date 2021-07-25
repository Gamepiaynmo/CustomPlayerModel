package com.gpiay.cpm.network;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.client.ClientConfig;
import com.gpiay.cpm.server.CommonConfig;
import com.gpiay.cpm.server.ModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.io.File;
import java.util.function.Supplier;

public class QueryModelPacket {
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

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        File modelFile = ModelManager.findModelFileFromCache(modelId);
        if (modelFile != null) {
            ctx.get().enqueueWork(ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER ?
                    () -> Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()),
                            new ModelDataPacket(modelFile)) :
                    () -> {
                        if (ClientConfig.SEND_MODELS.get() && CPMMod.cpmClient.isServerModded) {
                            Networking.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ModelDataPacket(modelFile));
                            Minecraft.getInstance().gui.getChat().addMessage(
                                    new TranslationTextComponent("text.cpm.modelUploaded"));
                        }
                    });
        }

        ctx.get().setPacketHandled(true);
    }
}
