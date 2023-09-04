package net.fabricmc.vanillapings.config;

import net.minecraft.client.resource.language.LanguageManager;

import java.util.ArrayList;
import java.util.List;

// like normal settings but with a default enable/disable setting
public class PingSettings extends Settings {
    private static final String KEY_DEFAULT_LANGUAGE = "lang";
    private static final String KEY_PING_RANGE = "ping-max-range";
    private static final String KEY_PING_CHAT_MESSAGE_RANGE = "ping-chat-message-range";
    private static final String KEY_PING_DIRECTION_MESSAGE_RANGE = "ping-direction-message-range";
    private static final String KEY_PING_COOLDOWN = "ping-cooldown";
    private static final String KEY_PING_ITEM_COUNT = "ping-item-count";
    private static final String KEY_PING_ITEM_COUNT_RANGE = "ping-item-count-range";
    private static final String KEY_PING_REMOVE_OLD_PINGS = "ping-remove-old";
    private final List<SettingsEvent> settingEvents = new ArrayList<>();
    private String defaultLanguage = LanguageManager.DEFAULT_LANGUAGE_CODE;
    private double pingRange = 500;
    private double pingChatMessageRange = 160;
    private double pingDirectionMessageRange = 160;
    private int pingCooldown = 5;
    private boolean pingItemCount = true;
    private double pingItemCountRange = 1;
    private boolean pingRemoveOld = true;

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
        cfg.put(KEY_PING_CHAT_MESSAGE_RANGE, pingChatMessageRange);
        cfg.put(KEY_PING_DIRECTION_MESSAGE_RANGE, pingDirectionMessageRange);
        cfg.put(KEY_PING_COOLDOWN, pingCooldown);
        cfg.put(KEY_PING_ITEM_COUNT, pingItemCount);
        cfg.put(KEY_PING_ITEM_COUNT_RANGE, pingItemCountRange);
        cfg.put(KEY_PING_REMOVE_OLD_PINGS, pingRemoveOld);
        super.saveSettings();
    }

    @Override
    protected void loadSettings() {
        super.loadSettings();
        if(cfg.containsKey(KEY_DEFAULT_LANGUAGE))
            defaultLanguage = cfg.getString(KEY_DEFAULT_LANGUAGE);
        if(cfg.containsKey(KEY_PING_RANGE))
            pingRange = cfg.getDouble(KEY_PING_RANGE);
        if(cfg.containsKey(KEY_PING_CHAT_MESSAGE_RANGE))
            pingChatMessageRange = cfg.getDouble(KEY_PING_CHAT_MESSAGE_RANGE);
        if(cfg.containsKey(KEY_PING_DIRECTION_MESSAGE_RANGE))
            pingDirectionMessageRange = cfg.getDouble(KEY_PING_DIRECTION_MESSAGE_RANGE);
        if(cfg.containsKey(KEY_PING_COOLDOWN))
            pingCooldown = cfg.getInteger(KEY_PING_COOLDOWN);
        if(cfg.containsKey(KEY_PING_ITEM_COUNT))
            pingItemCount = cfg.getBoolean(KEY_PING_ITEM_COUNT);
        if(cfg.containsKey(KEY_PING_ITEM_COUNT_RANGE))
            pingItemCountRange = cfg.getDouble(KEY_PING_ITEM_COUNT_RANGE);
        if(cfg.containsKey(KEY_PING_REMOVE_OLD_PINGS))
            pingRemoveOld = cfg.getBoolean(KEY_PING_REMOVE_OLD_PINGS);

        if(pingItemCountRange < 0)
            pingItemCountRange = 0;
        if(pingRange < 0 && pingRange != -1)
            pingRange = 0;
        if(pingChatMessageRange < 0 && pingChatMessageRange != -1)
            pingChatMessageRange = 0;
        if(pingDirectionMessageRange < 0 && pingDirectionMessageRange != -1)
            pingDirectionMessageRange = 0;

        invokeSettingsRefreshed();
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public double getPingRange() {
        return pingRange == -1 ? 5000 : pingRange;
    }

    public double getPingChatMessageRange() {
        return pingChatMessageRange;
    }

    public boolean hasInfinitePingChatMessageRange() {
        return pingChatMessageRange == -1;
    }

    public double getPingDirectionMessageRange() {
        return pingDirectionMessageRange;
    }

    public boolean hasInfinitePingDirectionMessageRange() {
        return pingDirectionMessageRange == -1;
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

    public boolean isPingRemoveOld() {
        return pingRemoveOld;
    }

    private void invokeSettingsRefreshed() {
        settingEvents.forEach(SettingsEvent::refresh);
    }

    @FunctionalInterface
    public interface SettingsEvent {
        void refresh();
    }
}
