package net.fabricmc.vanillapings.config;

import net.minecraft.client.resource.language.LanguageManager;

import java.util.ArrayList;
import java.util.List;

// like normal settings but with a default enable/disable setting
public class FeatureSettings extends Settings {
    private static final String KEY_DEFAULT_LANGUAGE = "lang";
    private final List<SettingsEvent> settingEvents = new ArrayList<>();
    private String defaultLanguage = LanguageManager.DEFAULT_LANGUAGE_CODE;

    public boolean registerSettingsEvent(SettingsEvent event) {
        return settingEvents.add(event);
    }

    public boolean unregisterSettingsEvent(SettingsEvent event) {
        return settingEvents.remove(event);
    }

    @Override
    protected void saveSettings() {
        cfg.put(KEY_DEFAULT_LANGUAGE, defaultLanguage);
        super.saveSettings();
    }

    @Override
    protected void loadSettings() {
        super.loadSettings();
        if(cfg.containsKey(KEY_DEFAULT_LANGUAGE))
            defaultLanguage = cfg.getString(KEY_DEFAULT_LANGUAGE);
        invokeSettingsRefreshed();
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    private void invokeSettingsRefreshed() {
        settingEvents.forEach(SettingsEvent::refresh);
    }

    @FunctionalInterface
    public interface SettingsEvent {
        void refresh();
    }
}
