package com.gpiay.cpm.network;

import com.google.common.collect.Maps;
import com.gpiay.cpm.CPMMod;
import com.gpiay.cpm.network.packet.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NetworkHandler {
    public static final ResourceLocation SERVER_INIT_ID = new ResourceLocation(CPMMod.MOD_ID, "server_init_packet");
    public static final ResourceLocation MODEL_DATA_ID = new ResourceLocation(CPMMod.MOD_ID, "model_data_packet");
    public static final ResourceLocation MODEL_LIST_ID = new ResourceLocation(CPMMod.MOD_ID, "model_list_packet");
    public static final ResourceLocation QUERY_DATA_ID = new ResourceLocation(CPMMod.MOD_ID, "query_data_packet");
    public static final ResourceLocation QUERY_LIST_ID = new ResourceLocation(CPMMod.MOD_ID, "query_list_packet");
    public static final ResourceLocation KEY_STATE_ID = new ResourceLocation(CPMMod.MOD_ID, "key_state_packet");
    public static final ResourceLocation EYE_POSITION_ID = new ResourceLocation(CPMMod.MOD_ID, "eye_position_packet");

    private static final Map<Class<?>, ResourceLocation> packetIdentifiers = Maps.newHashMap();

    private static <P> void registerPacket(ResourceLocation identifier, Class<P> type, Function<PacketBuffer, P> decoder,
            BiConsumer<P, TaskScheduler> consumer) {
        packetIdentifiers.put(type, identifier);
        ClientPlayNetworking.registerGlobalReceiver(identifier, (client, handler, buf, responseSender) -> {
            P packet = decoder.apply(buf);
            consumer.accept(packet, client::execute);
        });
    }

    private static <P> void registerPacket(ResourceLocation identifier, Class<P> type, Function<PacketBuffer, P> decoder,
            TriConsumer<P, TaskScheduler, ServerPlayerEntity> consumer) {
        packetIdentifiers.put(type, identifier);
        ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, handler, buf, responseSender) -> {
            P packet = decoder.apply(buf);
            consumer.accept(packet, server::execute, player);
        });
    }

    public static void registerPackets() {
        registerPacket(SERVER_INIT_ID, ServerInitPacket.class, ServerInitPacket::new, ServerInitPacket::handleClient);
        registerPacket(MODEL_DATA_ID, ModelDataPacket.class, ModelDataPacket::new, ModelDataPacket::handleClient);
        registerPacket(MODEL_DATA_ID, ModelDataPacket.class, ModelDataPacket::new, ModelDataPacket::handleServer);
        registerPacket(MODEL_LIST_ID, ModelListPacket.class, ModelListPacket::new, ModelListPacket::handleClient);
        registerPacket(QUERY_DATA_ID, QueryModelPacket.class, QueryModelPacket::new, QueryModelPacket::handleClient);
        registerPacket(QUERY_DATA_ID, QueryModelPacket.class, QueryModelPacket::new, QueryModelPacket::handleServer);
        registerPacket(QUERY_LIST_ID, QueryModelListPacket.class, QueryModelListPacket::new, QueryModelListPacket::handleServer);
        registerPacket(KEY_STATE_ID, UpdateKeyStatePacket.class, UpdateKeyStatePacket::new, UpdateKeyStatePacket::handleClient);
        registerPacket(KEY_STATE_ID, UpdateKeyStatePacket.class, UpdateKeyStatePacket::new, UpdateKeyStatePacket::handleServer);
        registerPacket(EYE_POSITION_ID, EyePositionPacket.class, EyePositionPacket::new, EyePositionPacket::handleServer);
    }

    public static void send(PacketEncoder packet) {
        ClientPlayNetworking.send(packetIdentifiers.get(packet.getClass()), formPacket(packet));
    }

    public static void send(ServerPlayerEntity player, PacketEncoder packet) {
        ServerPlayNetworking.send(player, packetIdentifiers.get(packet.getClass()), formPacket(packet));
    }

    public static PacketBuffer formPacket(PacketEncoder packet) {
        PacketBuffer buf = PacketByteBufs.create();
        packet.toBytes(buf);
        return buf;
    }
}
