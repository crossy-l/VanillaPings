package com.vanillapings.networking;

import com.vanillapings.networking.packet.PingC2SPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class CPingPackets {

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(PingC2SPacket.PingPayload.ID, PingC2SPacket.PingPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PingC2SPacket.PingPayload.ID, PingC2SPacket::receive);
    }
}
