package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.translation.Translations;
import net.minecraft.commands.CommandSourceStack;

public class SoundCommand {
    public static int setSound(CommandContext<CommandSourceStack> ctx, boolean value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.SOUND_ENABLED.constructMessage(value, VanillaPings.SETTINGS.setPlaySound(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
