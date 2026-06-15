package com.vanillapings.features.ping.input;

import com.vanillapings.compat.Compat;
import com.vanillapings.features.ping.ClientPingManager;
import com.vanillapings.translation.Translations;
import com.vanillapings.util.InputCooldown;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?}
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ClientPingInputs {
    private static KeyMapping pingKey;
    private static final InputCooldown inputCooldown = new InputCooldown(5);

    public static void register() {
        //? if >=26.1 {
        /*pingKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        *///?} else {
        pingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //?}
                // The bind label is a translation key; the lang file maps "vanillapings.ping"
                // (dot form), so convert the "vanillapings:ping" id rather than passing it raw.
                Compat.id(Translations.KEY_PING).toLanguageKey(),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                // Version-sensitive (client-only): KeyMapping.Category.register(...) on 1.21.9+,
                // a translation-key String on older versions.
                //? if >=1.21.9 {
                KeyMapping.Category.register(Compat.id(Translations.KEY_CATEGORY))
                //?} else {
                /*Compat.id(Translations.KEY_CATEGORY).toLanguageKey("key.category")
                *///?}
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pingKey.isDown() && inputCooldown.isReady()) {
                inputCooldown.triggerCooldown();
                ClientPingManager.pingInFrontOfPlayer();
            } else {
                inputCooldown.tick();
            }
        });
    }
}
