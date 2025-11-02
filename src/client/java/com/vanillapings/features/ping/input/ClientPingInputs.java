package com.vanillapings.features.ping.input;

import com.vanillapings.VanillaPings;
import com.vanillapings.features.ping.ClientPingManager;
import com.vanillapings.translation.Translations;
import com.vanillapings.util.InputCooldown;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientPingInputs {
    private static KeyBinding pingKey;
    private static final InputCooldown inputCooldown = new InputCooldown(5);
    public static void register() {
        pingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                Identifier.of(Translations.KEY_PING).toTranslationKey(),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                KeyBinding.Category.create(Identifier.of(Translations.KEY_CATEGORY))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(pingKey.isPressed() && inputCooldown.isReady()) {
                inputCooldown.triggerCooldown();
                ClientPingManager.pingInFrontOfPlayer();
            } else
                inputCooldown.tick();
        });
    }
}
