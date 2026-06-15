package com.vanillapings.networking;

import com.vanillapings.VanillaPings;
import com.vanillapings.compat.Compat;
import com.vanillapings.features.ping.PingManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

/**
 * Registration + payload for the ping trigger packet.
 *
 * <p>Three networking eras, all behind {@code //?}:
 * <ul>
 *   <li>{@code >=1.21}: CustomPacketPayload + {@code context.server()}</li>
 *   <li>{@code 1.20.5-1.20.6}: CustomPacketPayload + {@code context.player().getServer()}</li>
 *   <li>{@code <1.20.5}: legacy {@code RegistryFriendlyByteBuf} channel registration (no CustomPacketPayload)</li>
 * </ul>
 * The payload carries an (unused) {@link BlockPos}; the packet is just a "trigger a ping"
 * signal — the server raycasts from the sending player.
 */
public final class PingNetworking {
    private PingNetworking() {
    }

    //? if >=1.20.5 {
    public record PingPayload(BlockPos blockPos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<PingPayload> ID =
                new CustomPacketPayload.Type<>(Compat.id(VanillaPings.MOD_ID, "ping"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PingPayload> CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, PingPayload::blockPos, PingPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public static void register() {
        //? if >=26.1 {
        /*PayloadTypeRegistry.serverboundPlay().register(PingPayload.ID, PingPayload.CODEC);*/
        //?} else {
        PayloadTypeRegistry.playC2S().register(PingPayload.ID, PingPayload.CODEC);
        //?}
        //? if >=1.21 {
        ServerPlayNetworking.registerGlobalReceiver(PingPayload.ID, (payload, context) ->
                context.server().execute(() -> PingManager.pingWithCooldown(context.player())));
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(PingPayload.ID, (payload, context) ->
                context.player().getServer().execute(() -> PingManager.pingWithCooldown(context.player())));
        *///?}
    }
    //?} else {
    /*public static final net.minecraft.resources.ResourceLocation ID_PING = Compat.id(VanillaPings.MOD_ID, "ping");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID_PING,
                (server, player, handler, buf, responseSender) -> server.execute(() -> PingManager.pingWithCooldown(player)));
    }
    *///?}
}
