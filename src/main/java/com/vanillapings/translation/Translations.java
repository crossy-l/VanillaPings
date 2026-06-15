package com.vanillapings.translation;

import com.vanillapings.util.Triple;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Vec3i;

public class Translations {
    public static final String KEY_CATEGORY = "vanillapings:category";
    public static final String KEY_RELOAD = "vanillapings:reload";
    public static final String KEY_ENABLED = "vanillapings:util.enabled";
    public static final String KEY_ALREADY_ENABLED = "vanillapings:util.enabled.already";
    public static final String KEY_SET = "vanillapings:util.set";
    public static final String KEY_SET_ALREADY = "vanillapings:util.set.already";

    public static final String KEY_LANG = "vanillapings:lang";
    public static final String KEY_LANG_NAME = KEY_LANG + ".name";
    public static final String KEY_LANG_ERROR = KEY_LANG + ".fallback";

    public static final String KEY_PING = "vanillapings:ping";
    public static final String KEY_PING_REMOVED_OLD = KEY_PING + ".remove_old";
    public static final String KEY_PING_ITEM = KEY_PING + ".item";
    public static final String KEY_PING_ITEM_COUNT = KEY_PING_ITEM + ".count";
    public static final String KEY_PING_ITEM_COUNT_RANGE = KEY_PING_ITEM_COUNT + ".range";
    public static final String KEY_PING_PLAY_SOUND = KEY_PING + ".play_sound";
    public static final String KEY_PING_GLOWING = KEY_PING + ".glowing";
    public static final String KEY_PING_GLOWING_FLASH = KEY_PING_GLOWING + ".flash";
    public static final String KEY_PING_MESSAGE = KEY_PING + ".message";
    public static final String KEY_PING_RANGE = KEY_PING + ".range";
    public static final String KEY_PING_CHAT_RANGE = KEY_PING_RANGE + ".chat";
    public static final String KEY_PING_DIRECTION_MESSAGE_RANGE = KEY_PING_RANGE + ".direction";
    public static final String KEY_PING_COOLDOWN = KEY_PING + ".cooldown";
    public static final String KEY_PING_DIRECTION_MESSAGE = KEY_PING + ".message_directional";

    public static final Translatable PING = new Translatable(
            translator -> translator.getAsText(KEY_PING)
    );

    public static final Translatable RELOAD = new Translatable(
            translator -> translator.getAsText(KEY_RELOAD)
    );

    public static final TranslatableSingle<Boolean> LANGUAGE = new TranslatableSingle<>(
            (translator, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_SET), translator.get(KEY_LANG), translator.get(KEY_LANG_NAME))) :
                    Component.literal(String.format(translator.get(KEY_SET_ALREADY), translator.get(KEY_LANG), translator.get(KEY_LANG_NAME)))
    );

    public static final Translatable LANGUAGE_ERROR = new Translatable(
            translator -> translator.getAsText(KEY_LANG_ERROR)
    );

    public static final TranslatableSingle<Integer> REMOVED_OLD = new TranslatableSingle<>(
            (translator, count) -> Component.literal(String.format(translator.get(KEY_PING_REMOVED_OLD), count)),
            () -> 0
    );

    public static final TranslatableDouble<Boolean, Boolean> SOUND_ENABLED = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_ENABLED + "." + value), translator.get(KEY_PING_PLAY_SOUND) )) :
                    Component.literal(String.format(translator.get(KEY_ALREADY_ENABLED + "." + value), translator.get(KEY_PING_PLAY_SOUND)))
    );

    public static final TranslatableDouble<Boolean, Boolean> GLOWING_ENABLED = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_ENABLED + "." + value), translator.get(KEY_PING_GLOWING) )) :
                    Component.literal(String.format(translator.get(KEY_ALREADY_ENABLED + "." + value), translator.get(KEY_PING_GLOWING)))
    );

    public static final TranslatableDouble<Boolean, Boolean> GLOWING_FLASH_ENABLED = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_ENABLED + "." + value), translator.get(KEY_PING_GLOWING_FLASH) )) :
                    Component.literal(String.format(translator.get(KEY_ALREADY_ENABLED + "." + value), translator.get(KEY_PING_GLOWING_FLASH)))
    );

    public static final TranslatableDouble<String, Boolean> ITEM = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_SET), translator.get(KEY_PING_ITEM), value)) :
                    Component.literal(String.format(translator.get(KEY_SET_ALREADY), translator.get(KEY_PING_ITEM), value))
    );

    public static final TranslatableDouble<Double, Boolean> RANGE = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_SET), translator.get(KEY_PING_RANGE), value)) :
                    Component.literal(String.format(translator.get(KEY_SET_ALREADY), translator.get(KEY_PING_RANGE), value))
    );

    public static final TranslatableDouble<Double, Boolean> ITEM_COUNT_RANGE = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_SET), translator.get(KEY_PING_ITEM_COUNT_RANGE), value)) :
                    Component.literal(String.format(translator.get(KEY_SET_ALREADY), translator.get(KEY_PING_ITEM_COUNT_RANGE), value))
    );

    public static final TranslatableDouble<Boolean, Boolean> ITEM_COUNT_ENABLED = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_ENABLED + "." + value), translator.get(KEY_PING_ITEM_COUNT) )) :
                    Component.literal(String.format(translator.get(KEY_ALREADY_ENABLED + "." + value), translator.get(KEY_PING_ITEM_COUNT)))
    );

    public static final TranslatableDouble<Double, Boolean> CHAT_RANGE = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_SET), translator.get(KEY_PING_CHAT_RANGE), value)) :
                    Component.literal(String.format(translator.get(KEY_SET_ALREADY), translator.get(KEY_PING_CHAT_RANGE), value))
    );

    public static final TranslatableDouble<Double, Boolean> DIRECTION_MESSAGE_RANGE = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_SET), translator.get(KEY_PING_DIRECTION_MESSAGE_RANGE), value)) :
                    Component.literal(String.format(translator.get(KEY_SET_ALREADY), translator.get(KEY_PING_DIRECTION_MESSAGE_RANGE), value))
    );

    public static final TranslatableDouble<Integer, Boolean> COOLDOWN = new TranslatableDouble<>(
            (translator, value, valueChanged) -> valueChanged ?
                    Component.literal(String.format(translator.get(KEY_SET), translator.get(KEY_PING_COOLDOWN), value)) :
                    Component.literal(String.format(translator.get(KEY_SET_ALREADY), translator.get(KEY_PING_COOLDOWN), value))
    );

    public static final TranslatableSingle<Triple<String, Component, Vec3i>> PING_MESSAGE = new TranslatableSingle<>(
      (translator, extra) -> {
          String msg = translator.get(KEY_PING_MESSAGE);
          if(msg.split("%2s").length != 2)
              return Component.literal(KEY_PING_MESSAGE);
          String msg2 = msg.split("%2s")[1];
          msg = msg.split("%2s")[0];

          MutableComponent text = Component.literal(String.format(msg, extra.first()));
          text.append(extra.second());
          text.append(Component.literal(String.format(msg2,  extra.third().getX(), extra.third().getY(), extra.third().getZ())));
          text.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

          return text;
      }
    );

    public static final TranslatableSingle<Triple<Integer, String, String>> PING_DIRECTION_MESSAGE = new TranslatableSingle<>(
            (translator, extra) -> {
                String msg = translator.get(KEY_PING_DIRECTION_MESSAGE);
                try {
                    Style style = Component.empty().getStyle().withColor(ChatFormatting.RED);
                    if(extra.first() > 20 && extra.first() < 50)
                        style = style.withColor(ChatFormatting.YELLOW);
                    else if(extra.first() >= 50)
                        style = style.withColor(ChatFormatting.GREEN);
                    return Component.literal(String.format(msg, extra.second(), extra.first(), extra.third())).withStyle(style);
                } catch (Exception error) {
                    return Component.literal(KEY_PING_DIRECTION_MESSAGE);
                }
            }
    );
}
