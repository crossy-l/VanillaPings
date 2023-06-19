package net.fabricmc.vanillapings.features.ping;

import net.fabricmc.vanillapings.networking.CPingPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class ClientPingManager {
    public static void pingInFrontOfPlayer() {
        ClientPlayNetworking.send(CPingPackets.ID_PING, PacketByteBufs.empty());
    }
}
