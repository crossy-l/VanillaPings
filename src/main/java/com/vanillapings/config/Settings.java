package com.vanillapings.config;

import com.vanillapings.VanillaPings;

import java.nio.file.Paths;


public class Settings {
    private final String configFile;
    protected FileConfig cfg;

    public Settings() {
        this(Paths.get(FileConfig.CONFIG_FOLDER, VanillaPings.MOD_NAME.toLowerCase() + ".properties").toString());
    }

    public Settings(String configFile) {
        this.configFile = configFile;
    }

    public void init() {
        reload();
    }

    protected void saveSettings() {
        cfg.saveConfig();
    }

    public void reload() {
        cfg = new FileConfig(configFile);
        loadSettings();
        saveSettings();
    }

    protected void loadSettings() {}
}
