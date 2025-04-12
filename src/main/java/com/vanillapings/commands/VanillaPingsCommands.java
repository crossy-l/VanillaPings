package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vanillapings.VanillaPings;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.vanillapings.features.ping.PingManager;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VanillaPingsCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("vanillapings")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(literal("reload")
                            .executes(ReloadCommand::Reload))
                        .then(literal("removeOld")
                                .executes(RemoveCommand::removeOldPings))
                        .then(literal("language")
                                .then(literal("en_us")
                                        .executes(ctx -> LanguageCommand.setLanguage(ctx, "en_us")))
                                .then(literal("de_de")
                                    .executes(ctx -> LanguageCommand.setLanguage(ctx, "de_de")))
                                .then(argument("custom", StringArgumentType.word())
                                        .executes(ctx -> LanguageCommand.setLanguage(ctx, StringArgumentType.getString(ctx, "custom"))))
                        )
                        .then(literal("sound")
                                .executes(ctx -> SoundCommand.setSound(ctx, !VanillaPings.SETTINGS.isPlaySound()))
                                .then(argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> SoundCommand.setSound(ctx, BoolArgumentType.getBool(ctx, "value")))))
                        .then(literal("glowing")
                                .executes(ctx -> GlowingCommand.setGlowing(ctx, !VanillaPings.SETTINGS.isPingGlowing()))
                                .then(argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> GlowingCommand.setGlowing(ctx, BoolArgumentType.getBool(ctx, "value"))))
                                .then(literal("flash")
                                        .executes(ctx -> GlowingCommand.setGlowingFlash(ctx, !VanillaPings.SETTINGS.isPingGlowingFlash()))
                                        .then(argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> GlowingCommand.setGlowingFlash(ctx, BoolArgumentType.getBool(ctx, "value")))))
                        )
                        .then(literal("item")
                                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                        .executes(ctx -> ItemCommand.setItem(ctx, ItemStackArgumentType.getItemStackArgument(ctx, "item").getItem())))
                                .then(literal("count")
                                        .executes(ctx -> ItemCommand.setItemCount(ctx, !VanillaPings.SETTINGS.isPingItemCount()))
                                        .then(argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> ItemCommand.setItemCount(ctx, BoolArgumentType.getBool(ctx, "value"))))
                                        .then(literal("range")
                                                .then(argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> ItemCommand.setItemCountRange(ctx, DoubleArgumentType.getDouble(ctx, "value")))))
                                )
                        )
                        .then(literal("range")
                            .then(argument("value", DoubleArgumentType.doubleArg())
                                    .executes(ctx -> RangeCommand.setRange(ctx, DoubleArgumentType.getDouble(ctx, "value"))))
                            .then(literal("chat")
                                .then(argument("value", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> RangeCommand.setChatRange(ctx, DoubleArgumentType.getDouble(ctx, "value")))))
                            .then(literal("direction")
                                    .then(argument("value", DoubleArgumentType.doubleArg())
                                            .executes(ctx -> RangeCommand.setDirectionMessageRange(ctx, DoubleArgumentType.getDouble(ctx, "value")))))
                        )
                        .then(literal("cooldown")
                                .then(argument("ticks", IntegerArgumentType.integer())
                                        .executes(ctx -> CooldownCommand.setPingCooldown(ctx, IntegerArgumentType.getInteger(ctx, "ticks")))))
        ));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("ping")
                        .requires(serverCommandSource -> serverCommandSource.getEntity() != null)
                        .executes(ctx -> {
                            PingManager.pingWithCooldown((ServerPlayerEntity) Objects.requireNonNull(ctx.getSource().getEntity()));
                            return Command.SINGLE_SUCCESS;
                        })
        ));
    }

    public static void broadcastCommandUsageToOperators(Text message, ServerCommandSource source) {
        MinecraftServer server = source.getWorld().getGameInstance().getServer();
        GameRules rules = source.getWorld().getGameRules();
        PlayerManager playerManager = source.getWorld().getGameInstance().getPlayerManager();

        if(!(source.getEntity() instanceof LivingEntity) && !source.getName().equals("Server") && !rules.getBoolean(GameRules.COMMAND_BLOCK_OUTPUT))
            return;


        List<ServerPlayerEntity> operators = playerManager.getPlayerList().stream().filter(player -> player.hasPermissionLevel(server.getOpPermissionLevel())).toList();

        Text mMessage = Text.literal(String.format("[%s: ", source.getName())).append(message).append(Text.literal("]")).formatted(Formatting.GRAY).formatted(Formatting.ITALIC);
        UUID sourceId = source.getEntity() != null ? source.getEntity().getUuid() : null;
        if(rules.getBoolean(GameRules.SEND_COMMAND_FEEDBACK))
            operators.forEach(player -> {
                if(!player.getUuid().equals(sourceId))
                    player.sendMessage(mMessage);
            });
        if(rules.getBoolean(GameRules.LOG_ADMIN_COMMANDS) && !source.getName().equals("Server"))
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
        return source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK) || isServer;
    }
}
