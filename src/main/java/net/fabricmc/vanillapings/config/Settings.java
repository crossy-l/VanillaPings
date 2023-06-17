package net.fabricmc.vanillapings.config;

import net.fabricmc.vanillapings.VanillaPings;

import java.nio.file.Paths;

import static net.fabricmc.vanillapings.config.FileConfig.CONFIG_FOLDER;


public class Settings {
    private final String configFile;
    protected FileConfig cfg;

    public Settings() {
        this(Paths.get(CONFIG_FOLDER, VanillaPings.MOD_NAME.toLowerCase() + ".properties").toString());
    }

    public Settings(String configFile) {
        this.configFile = configFile;
    }

    public void init() {
        reload();
        saveSettings();
    }

    protected void saveSettings() {
        cfg.saveConfig();
    }

    public void reload() {
        cfg = new FileConfig(configFile);
        loadSettings();
    }

    protected void loadSettings() {}
}
