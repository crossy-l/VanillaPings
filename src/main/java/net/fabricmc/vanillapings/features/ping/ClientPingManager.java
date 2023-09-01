package net.fabricmc.vanillapings.features.ping;

import net.fabricmc.vanillapings.networking.CPingPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class ClientPingManager {
    /**
     * Sends a packet to the server to ping in the direction the local player is facing in.
     * The server manages a cooldown for this action.
     */
    public static void pingInFrontOfPlayer() {
        ClientPlayNetworking.send(CPingPackets.ID_PING, PacketByteBufs.empty());
    }
}
