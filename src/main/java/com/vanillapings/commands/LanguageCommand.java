package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.translation.Translations;
import com.vanillapings.translation.Translator;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

public class LanguageCommand {
    public static int setLanguage(CommandContext<ServerCommandSource> ctx, String language) {
        Translator.clearTranslators();
        if(Translator.loadLanguage(language))
            VanillaPingsCommands.sendCommandFeedBack(Translations.LANGUAGE.constructMessage(VanillaPings.SETTINGS.setLangauge(language)), ctx.getSource());
        else {
            Translator.loadLanguage(VanillaPings.SETTINGS.getDefaultLanguage());
            VanillaPingsCommands.sendCommandFeedBack(Translations.LANGUAGE_ERROR.constructMessage().formatted(Formatting.RED), ctx.getSource());
        }
        return Command.SINGLE_SUCCESS;
    }
}
