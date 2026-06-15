package com.vanillapings.features.ping.input;

import com.vanillapings.compat.Compat;
import com.vanillapings.features.ping.ClientPingManager;
import com.vanillapings.translation.Translations;
import com.vanillapings.util.InputCooldown;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ClientPingInputs {
    private static KeyMapping pingKey;
    private static final InputCooldown inputCooldown = new InputCooldown(5);

    public static void register() {
        pingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                //? if >=1.21.9 {
                Compat.id(Translations.KEY_PING).toTranslationKey(),
                //?} else {
                /*Translations.KEY_PING,*/
                //?}
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                // Version-sensitive (client-only): KeyMapping.Category.create(...) on 1.21.9+,
                // a translation-key String on older versions.
                //? if >=1.21.9 {
                KeyMapping.Category.create(Compat.id(Translations.KEY_CATEGORY))
                //?} else {
                /*Translations.KEY_CATEGORY*/
                //?}
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pingKey.isPressed() && inputCooldown.isReady()) {
                inputCooldown.triggerCooldown();
                ClientPingManager.pingInFrontOfPlayer();
            } else {
                inputCooldown.tick();
            }
        });
    }
}
