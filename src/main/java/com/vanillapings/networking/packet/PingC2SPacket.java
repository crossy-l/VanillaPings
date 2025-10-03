package com.vanillapings.networking.packet;

import com.vanillapings.VanillaPings;
import com.vanillapings.features.ping.PingManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PingC2SPacket {
    public static final Identifier ID_PING = Identifier.of(VanillaPings.MOD_NAME.toLowerCase(), "ping");

    public record PingPayload(BlockPos blockPos) implements CustomPayload {
        public static final Id<PingPayload> ID = CustomPayload.id(ID_PING.getPath());
        public static final PacketCodec<PacketByteBuf, PingPayload> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, PingPayload::blockPos, PingPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void receive(PingPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> PingManager.pingWithCooldown(context.player()));
    }
}
