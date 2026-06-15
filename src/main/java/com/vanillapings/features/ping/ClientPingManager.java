package com.vanillapings.features.ping;

import com.vanillapings.networking.PingNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class ClientPingManager {
    /**
     * Sends a packet to the server to ping in the direction the local player is facing in.
     * The server manages a cooldown for this action.
     */
    public static void pingInFrontOfPlayer() {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new PingNetworking.PingPayload(BlockPos.ORIGIN));
        //?} else {
        /*ClientPlayNetworking.send(PingNetworking.ID_PING, net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create());*/
        //?}
    }
}
