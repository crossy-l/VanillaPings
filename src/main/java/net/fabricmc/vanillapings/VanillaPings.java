package net.fabricmc.vanillapings;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.vanillapings.commands.VanillaPingsCommands;
import net.fabricmc.vanillapings.config.PingSettings;
import net.fabricmc.vanillapings.networking.CPingPackets;
import net.fabricmc.vanillapings.features.ping.PingManager;
import net.fabricmc.vanillapings.translation.Translator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaPings implements ModInitializer {
	public static final String MOD_NAME = "VanillaPings";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	public static final PingSettings SETTINGS = new PingSettings();
	private static VanillaPings instance;
	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		instance = this;
		ServerLifecycleEvents.SERVER_STARTING.register(server1 -> server = server1);
		ServerTickEvents.END_SERVER_TICK.register(PingManager::tick);
		CPingPackets.registerC2SPackets();
		VanillaPingsCommands.registerCommands();
		SETTINGS.init();
		// Initialize default translator now instead of when it's needed
		Translator.getTranslator();
		LOGGER.info(MOD_NAME + " initialized");
	}

	public static VanillaPings getInstance() {
		return instance;
	}

	public static MinecraftServer getServer() {
		return server;
	}
}
