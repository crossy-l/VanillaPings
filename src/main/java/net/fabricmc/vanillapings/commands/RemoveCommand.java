package net.fabricmc.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.vanillapings.features.ping.PingManager;
import net.fabricmc.vanillapings.translation.Translations;
import net.minecraft.server.command.ServerCommandSource;

public class RemoveCommand {
    public static int removeOldPings(CommandContext<ServerCommandSource> ctx) {
        int removed = PingManager.removeOldPings(ctx.getSource().getServer());
        VanillaPingsCommands.sendCommandFeedBack(Translations.REMOVED_OLD.constructMessage(removed), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
