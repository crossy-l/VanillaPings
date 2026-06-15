package com.vanillapings;

import com.vanillapings.commands.VanillaPingsCommands;
import com.vanillapings.config.PingSettings;
import com.vanillapings.features.ping.PingManager;
import com.vanillapings.networking.PingNetworking;
import com.vanillapings.translation.Translator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaPings implements ModInitializer {
	public static final String MOD_ID = "vanillapings";
	public static final String MOD_NAME = "VanillaPings";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	public static final PingSettings SETTINGS = new PingSettings();
	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(startedServer -> server = startedServer);
		ServerTickEvents.END_SERVER_TICK.register(PingManager::tick);
		PingNetworking.register();
		VanillaPingsCommands.registerCommands();
		SETTINGS.init();
		// Initialize default translator now instead of when it's needed
		Translator.getTranslator();
		LOGGER.info("{} initialized", MOD_NAME);
	}

	public static MinecraftServer getServer() {
		return server;
	}
}
