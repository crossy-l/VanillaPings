package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vanillapings.VanillaPings;
import com.vanillapings.compat.Compat;
import com.vanillapings.features.ping.PingManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class VanillaPingsCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("vanillapings")
                        .requires(Compat::isAdmin)
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
                                .then(argument("item", ItemArgument.item(registryAccess))
                                        //? if >=26.1 {
                                        /*.executes(ctx -> ItemCommand.setItem(ctx, ItemArgument.getItem(ctx, "item").item().value())))
                                        *///?} else {
                                        .executes(ctx -> ItemCommand.setItem(ctx, ItemArgument.getItem(ctx, "item").getItem())))
                                        //?}
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
                            PingManager.pingWithCooldown((ServerPlayer) Objects.requireNonNull(ctx.getSource().getEntity()));
                            return Command.SINGLE_SUCCESS;
                        })
        ));
    }

    public static void broadcastCommandUsageToOperators(Component message, CommandSourceStack source) {
        MinecraftServer server = source.getServer();

        if(!(source.getEntity() instanceof LivingEntity) && !source.getTextName().equals("Server") && !Compat.commandBlockOutput(source))
            return;


        List<ServerPlayer> operators = server.getPlayerList().getPlayers().stream().filter(player -> Compat.isAdmin(player, server)).toList();

        Component mMessage = Component.literal(String.format("[%s: ", source.getTextName())).append(message).append(Component.literal("]")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC);
        UUID sourceId = source.getEntity() != null ? source.getEntity().getUUID() : null;
        if(Compat.sendCommandFeedback(source))
            operators.forEach(player -> {
                if(!player.getUUID().equals(sourceId))
                    Compat.sendChatMessage(player, mMessage);
            });
        if(Compat.logAdminCommands(source) && !source.getTextName().equals("Server"))
            server.sendSystemMessage(mMessage);
    }

    public static void sendCommandFeedBack(Component message, CommandSourceStack source) {
        sendCommandFeedBack(message, message, source);
    }

    public static void sendCommandFeedBack(Component message, Component operatorText, CommandSourceStack source) {
        if(isCommandFeedbackAllowed(source))
            source.sendSystemMessage(message);
        broadcastCommandUsageToOperators(operatorText, source);
    }

    public static boolean isCommandFeedbackAllowed(CommandSourceStack source) {
        boolean isServer = !(source.getEntity() instanceof LivingEntity) && source.getTextName().equals("Server");
        return Compat.sendCommandFeedback(source) || isServer;
    }
}
