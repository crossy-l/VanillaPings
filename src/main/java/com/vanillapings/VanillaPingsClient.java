package com.vanillapings;

import com.vanillapings.features.ping.input.ClientPingInputs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vanillapings.VanillaPings.MOD_NAME;

@Environment(EnvType.CLIENT)
public class VanillaPingsClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("[Client] " + MOD_NAME);

	@Override
	public void onInitializeClient() {
		ClientPingInputs.register();
	}
}
