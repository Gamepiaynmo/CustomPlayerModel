package com.gpiay.cpm.network;

import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.network.packet.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class NetworkHandler {
    public static final String VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CPMMod.MOD_ID, "main"),
            () -> VERSION,
            s -> true,
            s -> true
    );

    private static int ID = 0;
    private static int nextID() {
        return ID++;
    }

    public static <P extends PacketEncoder> void registerMessage(Class<P> messageType, Function<PacketBuffer, P> decoder,
            BiConsumer<P, TaskScheduler> clientHandler) {
        INSTANCE.registerMessage(nextID(),
                messageType,
                PacketEncoder::toBytes,
                decoder,
                (packet, ctx) -> {
                    clientHandler.accept(packet, runnable -> ctx.get().enqueueWork(runnable));
                    ctx.get().setPacketHandled(true);
                }
                );
    }

    public static <P extends PacketEncoder> void registerMessage(Class<P> messageType, Function<PacketBuffer, P> decoder,
            BiConsumer<P, TaskScheduler> clientHandler, TriConsumer<P, TaskScheduler, ServerPlayerEntity> serverHandler) {
        INSTANCE.registerMessage(nextID(),
                messageType,
                PacketEncoder::toBytes,
                decoder,
                (packet, ctx) -> {
                    TaskScheduler scheduler = runnable -> ctx.get().enqueueWork(runnable);
                    if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER)
                        serverHandler.accept(packet, scheduler, ctx.get().getSender());
                    else clientHandler.accept(packet, scheduler);
                    ctx.get().setPacketHandled(true);
                }
                );
    }

    public static <P extends PacketEncoder> void registerMessage(Class<P> messageType, Function<PacketBuffer, P> decoder,
            TriConsumer<P, TaskScheduler, ServerPlayerEntity> serverHandler) {
        INSTANCE.registerMessage(nextID(),
                messageType,
                PacketEncoder::toBytes,
                decoder,
                (packet, ctx) -> {
                    serverHandler.accept(packet, runnable -> ctx.get().enqueueWork(runnable), ctx.get().getSender());
                    ctx.get().setPacketHandled(true);
                }
        );
    }

    public static void send(PacketEncoder packet) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static void send(ServerPlayerEntity player, PacketEncoder packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void registerMessages() {
        registerMessage(
                UpdateModelPacket.class,
                UpdateModelPacket::new,
                UpdateModelPacket::handleClient
        );
        registerMessage(
                QueryModelPacket.class,
                QueryModelPacket::new,
                QueryModelPacket::handleClient,
                QueryModelPacket::handleServer
        );
        registerMessage(
                ModelDataPacket.class,
                ModelDataPacket::new,
                ModelDataPacket::handleClient,
                ModelDataPacket::handleServer
        );
        registerMessage(
                QueryModelListPacket.class,
                QueryModelListPacket::new,
                QueryModelListPacket::handleServer
        );
        registerMessage(
                ModelListPacket.class,
                ModelListPacket::new,
                ModelListPacket::handleClient
        );
        registerMessage(
                UpdateKeyStatePacket.class,
                UpdateKeyStatePacket::new,
                UpdateKeyStatePacket::handleClient,
                UpdateKeyStatePacket::handleServer
        );
        registerMessage(
                EyePositionPacket.class,
                EyePositionPacket::new,
                EyePositionPacket::handleServer
        );
    }
}
