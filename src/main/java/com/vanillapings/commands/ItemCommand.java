package com.vanillapings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.vanillapings.VanillaPings;
import com.vanillapings.compat.Compat;
import com.vanillapings.translation.Translations;
import net.minecraft.world.item.Item;
import net.minecraft.commands.CommandSourceStack;
public class ItemCommand {
    public static int setItem(CommandContext<CommandSourceStack> ctx, Item item) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.ITEM.constructMessage(Compat.itemId(item).toString(), VanillaPings.SETTINGS.setPingItem(item)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }

    public static int setItemCountRange(CommandContext<CommandSourceStack> ctx, double value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.ITEM_COUNT_RANGE.constructMessage(value, VanillaPings.SETTINGS.setItemCountRange(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }

    public static int setItemCount(CommandContext<CommandSourceStack> ctx, boolean value) {
        VanillaPingsCommands.sendCommandFeedBack(Translations.ITEM_COUNT_ENABLED.constructMessage(value, VanillaPings.SETTINGS.setPingItemCount(value)), ctx.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
