package com.vanillapings.networking;

import com.vanillapings.VanillaPings;
import com.vanillapings.compat.Compat;
import com.vanillapings.features.ping.PingManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.BlockPos;
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
//?}

/**
 * Registration + payload for the ping trigger packet.
 *
 * <p>Three networking eras, all behind {@code //?}:
 * <ul>
 *   <li>{@code >=1.21}: CustomPayload + {@code context.server()}</li>
 *   <li>{@code 1.20.5-1.20.6}: CustomPayload + {@code context.player().getServer()}</li>
 *   <li>{@code <1.20.5}: legacy {@code PacketByteBuf} channel registration (no CustomPayload)</li>
 * </ul>
 * The payload carries an (unused) {@link BlockPos}; the packet is just a "trigger a ping"
 * signal — the server raycasts from the sending player.
 */
public final class PingNetworking {
    private PingNetworking() {
    }

    //? if >=1.20.5 {
    public record PingPayload(BlockPos blockPos) implements CustomPayload {
        public static final CustomPayload.Id<PingPayload> ID =
                new CustomPayload.Id<>(Compat.id(VanillaPings.MOD_ID, "ping"));
        public static final PacketCodec<PacketByteBuf, PingPayload> CODEC =
                PacketCodec.tuple(BlockPos.PACKET_CODEC, PingPayload::blockPos, PingPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(PingPayload.ID, PingPayload.CODEC);
        //? if >=1.21 {
        ServerPlayNetworking.registerGlobalReceiver(PingPayload.ID, (payload, context) ->
                context.server().execute(() -> PingManager.pingWithCooldown(context.player())));
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(PingPayload.ID, (payload, context) ->
                context.player().getServer().execute(() -> PingManager.pingWithCooldown(context.player())));*/
        //?}
    }
    //?} else {
    /*public static final net.minecraft.util.Identifier ID_PING = Compat.id(VanillaPings.MOD_ID, "ping");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID_PING,
                (server, player, handler, buf, responseSender) -> server.execute(() -> PingManager.pingWithCooldown(player)));
    }
    *///?}
}
