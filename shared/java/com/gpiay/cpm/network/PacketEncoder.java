package com.gpiay.cpm.network;

import net.minecraft.network.PacketBuffer;

public interface PacketEncoder {
    void toBytes(PacketBuffer buffer);
}
