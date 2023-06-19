package net.fabricmc.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.vanillapings.VanillaPings;
import net.fabricmc.vanillapings.translation.Translations;
import net.fabricmc.vanillapings.translation.Translator;
import net.minecraft.server.command.ServerCommandSource;

public class ReloadCommand {
    public static int Reload(CommandContext<ServerCommandSource> ctx) {
        VanillaPings.SETTINGS.reload();
        Translator.clearTranslators();
        VanillaPingsCommands.sendCommandFeedBack(Translations.RELOAD.constructMessage(), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
