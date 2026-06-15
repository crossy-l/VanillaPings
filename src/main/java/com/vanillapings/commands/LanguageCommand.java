package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.translation.Translations;
import com.vanillapings.translation.Translator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.ChatFormatting;

public class LanguageCommand {
    public static int setLanguage(CommandContext<CommandSourceStack> ctx, String language) {
        Translator.clearTranslators();
        if(Translator.loadLanguage(language))
            VanillaPingsCommands.sendCommandFeedBack(Translations.LANGUAGE.constructMessage(VanillaPings.SETTINGS.setLangauge(language)), ctx.getSource());
        else {
            Translator.loadLanguage(VanillaPings.SETTINGS.getDefaultLanguage());
            VanillaPingsCommands.sendCommandFeedBack(Translations.LANGUAGE_ERROR.constructMessage().withStyle(ChatFormatting.RED), ctx.getSource());
        }
        return Command.SINGLE_SUCCESS;
    }
}
