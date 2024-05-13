package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.translation.Translations;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.registry.Registry;

public class ItemCommand {
    public static int setItem(CommandContext<ServerCommandSource> ctx, Item item) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.ITEM.constructMessage(Registry.ITEM.getId(item).toString(), VanillaPings.SETTINGS.setPingItem(item)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }

    public static int setItemCountRange(CommandContext<ServerCommandSource> ctx, double value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.ITEM_COUNT_RANGE.constructMessage(value, VanillaPings.SETTINGS.setItemCountRange(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }

    public static int setItemCount(CommandContext<ServerCommandSource> ctx, boolean value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.ITEM_COUNT_ENABLED.constructMessage(value, VanillaPings.SETTINGS.setPingItemCount(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
