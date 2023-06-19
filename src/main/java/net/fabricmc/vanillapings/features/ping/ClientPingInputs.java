package net.fabricmc.vanillapings.features.ping;

import net.fabricmc.vanillapings.translation.Translations;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ClientPingInputs {
    private static KeyBinding pingKey;
    private static final InputCooldown inputCooldown = new InputCooldown(5);
    public static void register() {
        pingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                Translations.KEY_PING,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                Translations.KEY_CATEGORY
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
