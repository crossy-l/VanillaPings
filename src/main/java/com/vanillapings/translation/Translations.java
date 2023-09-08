package com.vanillapings.translation;

import com.vanillapings.util.Triple;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3i;

public class Translations {
    public static final String KEY_CATEGORY = "vanillapings.category";
    public static final String KEY_RELOAD = "vanillapings.reload";
    public static final String KEY_PING = "vanillapings.ping";
    public static final String KEY_PING_REMOVED_OLD = KEY_PING + ".remove_old";
    public static final String KEY_PING_MESSAGE = KEY_PING + ".message";
    public static final String KEY_PING_DIRECTION_MESSAGE = KEY_PING + ".message_directional";

    public static final Translatable PING = new Translatable(
            translator -> translator.getAsText(KEY_PING)
    );

    public static final Translatable RELOAD = new Translatable(
            translator -> translator.getAsText(KEY_RELOAD)
    );

    public static final TranslatableSingle<Integer> REMOVED_OLD = new TranslatableSingle<>(
            (translator, count) -> Text.literal(String.format(translator.get(KEY_PING_REMOVED_OLD), count)),
            () -> 0
    );

    public static final TranslatableSingle<Triple<String, Text, Vec3i>> PING_MESSAGE = new TranslatableSingle<>(
      (translator, extra) -> {
          String msg = translator.get(KEY_PING_MESSAGE);
          if(msg.split("%2s").length != 2)
              return Text.literal(KEY_PING_MESSAGE);
          String msg2 = msg.split("%2s")[1];
          msg = msg.split("%2s")[0];

          MutableText text = (MutableText) Text.of(String.format(msg, extra.first()));
          text.append(extra.second());
          text.append(Text.of(String.format(msg2,  extra.third().getX(), extra.third().getY(), extra.third().getZ())));
          text.formatted(Formatting.GRAY, Formatting.ITALIC);

          return text;
      }
    );

    public static final TranslatableSingle<Triple<Integer, String, String>> PING_DIRECTION_MESSAGE = new TranslatableSingle<>(
            (translator, extra) -> {
                String msg = translator.get(KEY_PING_DIRECTION_MESSAGE);
                try {
                    Style style = Text.empty().getStyle().withColor(Formatting.RED);
                    if(extra.first() > 20 && extra.first() < 50)
                        style = style.withColor(Formatting.YELLOW);
                    else if(extra.first() >= 50)
                        style = style.withColor(Formatting.GREEN);
                    return ((MutableText) Text.of(String.format(msg, extra.second(), extra.first(), extra.third()))).fillStyle(style);
                } catch (Exception error) {
                    return Text.literal(KEY_PING_DIRECTION_MESSAGE);
                }
            }
    );
}
