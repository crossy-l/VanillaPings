package net.fabricmc.vanillapings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.vanillapings.features.ping.ClientPingInputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.vanillapings.VanillaPings.MOD_NAME;

public class VanillaPingsClient implements ClientModInitializer {
	private static VanillaPingsClient instance;
	public static final Logger LOGGER = LoggerFactory.getLogger("[Client] " + MOD_NAME);

	@Override
	public void onInitializeClient() {
		instance = this;
		ClientPingInputs.register();
	}

	public static VanillaPingsClient getInstance() {
		return instance;
	}

}
