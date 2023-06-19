package net.fabricmc.vanillapings.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.vanillapings.VanillaPings;
import net.fabricmc.vanillapings.features.ping.PingManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class VanillaPingsCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("vanillapings")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                                .then(literal("reload")
                                        .executes(ReloadCommand::Reload))
        ));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("ping")
                        .requires(serverCommandSource -> serverCommandSource.getEntity() != null)
                        .executes(ctx -> PingManager.pingInFrontOfEntity((ServerPlayerEntity) Objects.requireNonNull(ctx.getSource().getEntity())))
        ));
    }

    public static void broadcastCommandUsageToOperators(Text message, ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        GameRules rules = server.getGameRules();

        if(!(source.getEntity() instanceof LivingEntity) && !source.getName().equals("Server") && !rules.getBoolean(GameRules.COMMAND_BLOCK_OUTPUT))
            return;

        List<ServerPlayerEntity> operators = server.getPlayerManager().getPlayerList().stream().filter(player -> player.hasPermissionLevel(server.getOpPermissionLevel())).toList();

        Text mMessage = Text.literal(String.format("[%s: ", source.getName())).append(message).append(Text.literal("]")).formatted(Formatting.GRAY).formatted(Formatting.ITALIC);
        UUID sourceId = source.getEntity() != null ? source.getEntity().getUuid() : null;
        if(server.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK))
            operators.forEach(player -> {
                if(!player.getUuid().equals(sourceId))
                    player.sendMessage(mMessage);
            });
        if(server.getGameRules().getBoolean(GameRules.LOG_ADMIN_COMMANDS) && !source.getName().equals("Server"))
            server.sendMessage(mMessage);
    }

    public static void sendCommandFeedBack(Text message, ServerCommandSource source) {
        sendCommandFeedBack(message, message, source);
    }

    public static void sendCommandFeedBack(Text message, Text operatorText, ServerCommandSource source) {
        if(isCommandFeedbackAllowed(source))
            source.sendMessage(message);
        broadcastCommandUsageToOperators(operatorText, source);
    }

    public static boolean isCommandFeedbackAllowed(ServerCommandSource source) {
        boolean isServer = !(source.getEntity() instanceof LivingEntity) && source.getName().equals("Server");
        return source.getServer().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK) || isServer;
    }
}
