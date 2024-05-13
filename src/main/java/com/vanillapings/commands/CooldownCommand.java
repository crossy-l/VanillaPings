package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.translation.Translations;
import net.minecraft.server.command.ServerCommandSource;

public class CooldownCommand {
    public static int setPingCooldown(CommandContext<ServerCommandSource> ctx, int ticks) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.COOLDOWN.constructMessage(ticks, VanillaPings.SETTINGS.setPingCooldown(ticks)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
