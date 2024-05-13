package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.translation.Translations;
import net.minecraft.server.command.ServerCommandSource;

public class RangeCommand {
    public static int setRange(CommandContext<ServerCommandSource> ctx, double value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.RANGE.constructMessage(value, VanillaPings.SETTINGS.setPingRange(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }

    public static int setChatRange(CommandContext<ServerCommandSource> ctx, double value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.CHAT_RANGE.constructMessage(value, VanillaPings.SETTINGS.setPingChatMessageRange(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }

    public static int setDirectionMessageRange(CommandContext<ServerCommandSource> ctx, double value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.DIRECTION_MESSAGE_RANGE.constructMessage(value, VanillaPings.SETTINGS.setPingDirectionMessageRange(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
