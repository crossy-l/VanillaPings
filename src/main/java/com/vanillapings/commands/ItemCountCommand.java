package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.translation.Translations;
import net.minecraft.server.command.ServerCommandSource;
public class ItemCountCommand {
    public static int setItemCount(CommandContext<ServerCommandSource> ctx, boolean value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.ITEM_COUNT_ENABLED.constructMessage(value, !VanillaPings.SETTINGS.setPingItemCount(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
