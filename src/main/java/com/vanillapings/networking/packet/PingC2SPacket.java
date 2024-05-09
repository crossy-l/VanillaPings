package com.vanillapings.networking.packet;

import com.vanillapings.VanillaPings;
import com.vanillapings.features.ping.PingManager;
import com.vanillapings.networking.CPingPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PingC2SPacket {
    public static final Identifier ID_PING = new Identifier(VanillaPings.MOD_NAME.toLowerCase(), "ping");

    public record PingPayload(BlockPos blockPos) implements CustomPayload {
        public static final Id<PingPayload> ID = CustomPayload.id(ID_PING.getPath());
        public static final PacketCodec<PacketByteBuf, PingPayload> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, PingPayload::blockPos, PingPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void receive(PingPayload payload, ServerPlayNetworking.Context context) {
        context.player().getServer().execute(() -> PingManager.pingWithCooldown(context.player()));
    }
}
