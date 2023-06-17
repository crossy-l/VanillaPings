package net.fabricmc.vanillapings.networking;

import net.fabricmc.vanillapings.VanillaPings;
import net.fabricmc.vanillapings.networking.packet.PingC2SPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class CPingPackets {
    public static final Identifier ID_PING = new Identifier(VanillaPings.MOD_NAME.toLowerCase(), "ping");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ID_PING, PingC2SPacket::receive);
    }
}
