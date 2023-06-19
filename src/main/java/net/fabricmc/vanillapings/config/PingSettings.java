package net.fabricmc.vanillapings.config;

import net.minecraft.client.resource.language.LanguageManager;

import java.util.ArrayList;
import java.util.List;

// like normal settings but with a default enable/disable setting
public class PingSettings extends Settings {
    private static final String KEY_DEFAULT_LANGUAGE = "lang";
    private static final String KEY_PING_RANGE = "ping-max-range";
    private final List<SettingsEvent> settingEvents = new ArrayList<>();
    private String defaultLanguage = LanguageManager.DEFAULT_LANGUAGE_CODE;
    private double pingRange = 500;

    public boolean registerSettingsEvent(SettingsEvent event) {
        return settingEvents.add(event);
    }

    public boolean unregisterSettingsEvent(SettingsEvent event) {
        return settingEvents.remove(event);
    }

    @Override
    protected void saveSettings() {
        cfg.put(KEY_DEFAULT_LANGUAGE, defaultLanguage);
        cfg.put(KEY_PING_RANGE, pingRange);
        super.saveSettings();
    }

    @Override
    protected void loadSettings() {
        super.loadSettings();
        if(cfg.containsKey(KEY_DEFAULT_LANGUAGE))
            defaultLanguage = cfg.getString(KEY_DEFAULT_LANGUAGE);
        if(cfg.containsKey(KEY_PING_RANGE))
            pingRange = cfg.getDouble(KEY_PING_RANGE);
        invokeSettingsRefreshed();
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public double getPingRange() {
        return pingRange;
    }

    private void invokeSettingsRefreshed() {
        settingEvents.forEach(SettingsEvent::refresh);
    }

    @FunctionalInterface
    public interface SettingsEvent {
        void refresh();
    }
}
