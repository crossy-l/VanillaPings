package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.features.ping.PingManager;
import com.vanillapings.translation.Translations;
import net.minecraft.server.command.ServerCommandSource;

public class PlaySoundCommand {
    public static int setPlaySound(CommandContext<ServerCommandSource> ctx, boolean value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.SOUND_ENABLED.constructMessage(value, !VanillaPings.SETTINGS.setPlaySound(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
