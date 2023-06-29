package net.fabricmc.vanillapings.networking.packet;

import net.fabricmc.vanillapings.features.ping.PingManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PingC2SPacket {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> PingManager.pingWithCooldown(player));
    }
}
