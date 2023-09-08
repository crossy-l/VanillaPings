package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.translation.Translations;
import com.vanillapings.VanillaPings;
import com.vanillapings.translation.Translator;
import net.minecraft.server.command.ServerCommandSource;

public class ReloadCommand {
    public static int Reload(CommandContext<ServerCommandSource> ctx) {
        VanillaPings.SETTINGS.reload();
        Translator.clearTranslators();
        VanillaPingsCommands.sendCommandFeedBack(Translations.RELOAD.constructMessage(), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
