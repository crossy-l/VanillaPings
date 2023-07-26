package net.fabricmc.vanillapings.config;

import net.minecraft.client.resource.language.LanguageManager;

import java.util.ArrayList;
import java.util.List;

// like normal settings but with a default enable/disable setting
public class PingSettings extends Settings {
    private static final String KEY_DEFAULT_LANGUAGE = "lang";
    private static final String KEY_PING_RANGE = "ping-max-range";
    private static final String KEY_PING_COOLDOWN = "ping-cooldown";
    private static final String KEY_PING_ITEM_COUNT = "ping-item-count";
    private static final String KEY_PING_ITEM_COUNT_RANGE = "ping-item-count-range";
    private final List<SettingsEvent> settingEvents = new ArrayList<>();
    private String defaultLanguage = LanguageManager.DEFAULT_LANGUAGE_CODE;
    private double pingRange = 500;
    private int pingCooldown = 5;
    private boolean pingItemCount = true;
    private double pingItemCountRange = 1;

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
        cfg.put(KEY_PING_COOLDOWN, pingCooldown);
        cfg.put(KEY_PING_ITEM_COUNT, pingItemCount);
        cfg.put(KEY_PING_ITEM_COUNT_RANGE, pingItemCountRange);
        super.saveSettings();
    }

    @Override
    protected void loadSettings() {
        super.loadSettings();
        if(cfg.containsKey(KEY_DEFAULT_LANGUAGE))
            defaultLanguage = cfg.getString(KEY_DEFAULT_LANGUAGE);
        if(cfg.containsKey(KEY_PING_RANGE))
            pingRange = cfg.getDouble(KEY_PING_RANGE);
        if(cfg.containsKey(KEY_PING_COOLDOWN))
            pingCooldown = cfg.getInteger(KEY_PING_COOLDOWN);
        if(cfg.containsKey(KEY_PING_ITEM_COUNT))
            pingItemCount = cfg.getBoolean(KEY_PING_ITEM_COUNT);
        if(cfg.containsKey(KEY_PING_ITEM_COUNT_RANGE))
            pingItemCountRange = cfg.getDouble(KEY_PING_ITEM_COUNT_RANGE);

        if(pingItemCountRange < 0)
            pingItemCountRange = 0;
        if(pingRange < 0)
            pingRange = 0;

        invokeSettingsRefreshed();
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public double getPingRange() {
        return pingRange;
    }

    public int getPingCooldown() {
        return pingCooldown;
    }

    public boolean isPingItemCount() {
        return pingItemCount;
    }

    public double getPingItemCountRange() {
        return pingItemCountRange;
    }

    private void invokeSettingsRefreshed() {
        settingEvents.forEach(SettingsEvent::refresh);
    }

    @FunctionalInterface
    public interface SettingsEvent {
        void refresh();
    }
}
