package com.gpiay.cpm.network;

import com.gpiay.cpm.CPMMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    public static final String VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CPMMod.MOD_ID, "main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );

    private static int ID = 0;
    private static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE.registerMessage(
                nextID(),
                UpdateModelPacket.class,
                UpdateModelPacket::toBytes,
                UpdateModelPacket::new,
                UpdateModelPacket::handler
        );
        INSTANCE.registerMessage(
                nextID(),
                QueryModelPacket.class,
                QueryModelPacket::toBytes,
                QueryModelPacket::new,
                QueryModelPacket::handler
        );
        INSTANCE.registerMessage(
                nextID(),
                ModelDataPacket.class,
                ModelDataPacket::toBytes,
                ModelDataPacket::new,
                ModelDataPacket::handler
        );
        INSTANCE.registerMessage(
                nextID(),
                QueryModelListPacket.class,
                QueryModelListPacket::toBytes,
                QueryModelListPacket::new,
                QueryModelListPacket::handler
        );
        INSTANCE.registerMessage(
                nextID(),
                ModelListPacket.class,
                ModelListPacket::toBytes,
                ModelListPacket::new,
                ModelListPacket::handler
        );
        INSTANCE.registerMessage(
                nextID(),
                UpdateKeyStatePacket.class,
                UpdateKeyStatePacket::toBytes,
                UpdateKeyStatePacket::new,
                UpdateKeyStatePacket::handler
        );
    }
}
