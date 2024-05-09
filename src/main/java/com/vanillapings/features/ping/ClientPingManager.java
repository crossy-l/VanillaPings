package com.vanillapings.features.ping;

import com.vanillapings.networking.CPingPackets;
import com.vanillapings.networking.packet.PingC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.impl.networking.payload.PayloadHelper;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public class ClientPingManager {
    /**
     * Sends a packet to the server to ping in the direction the local player is facing in.
     * The server manages a cooldown for this action.
     */
    public static void pingInFrontOfPlayer() {
        ClientPlayNetworking.send(new PingC2SPacket.PingPayload(new BlockPos(0, 0, 0)));
    }
}
