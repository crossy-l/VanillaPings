package com.vanillapings.config;

import com.vanillapings.translation.Translator;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private static final String KEY_PING_GLOWING = "ping-glowing";
    private static final String KEY_PING_GLOWING_FLASH = "ping-glowing-flash";
    private static final String KEY_PING_ITEM = "ping-item";
    private static final String KEY_PING_PLAY_SOUND = "ping-sound";
    private final List<SettingsEvent> settingEvents = new ArrayList<>();
    private String defaultLanguage = Translator.DEFAULT_LANGUAGE; //LanguageManager.DEFAULT_LANGUAGE_CODE;
    private double pingRange = 500;
    private double pingChatMessageRange = 160;
    private double pingDirectionMessageRange = 160;
    private int pingCooldown = 5;
    private boolean pingItemCount = true;
    private boolean playSound = true;
    private double pingItemCountRange = 1;
    private boolean pingRemoveOld = true;
    private boolean pingGlowing = true;
    private boolean pingGlowingFlash = true;
    private Item pingItem = Items.BLUE_STAINED_GLASS;

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
        cfg.put(KEY_PING_GLOWING, pingGlowing);
        cfg.put(KEY_PING_GLOWING_FLASH, pingGlowingFlash);
        cfg.put(KEY_PING_ITEM, Registry.ITEM.getId(pingItem).toString());
        cfg.put(KEY_PING_PLAY_SOUND, playSound);
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
        if(cfg.containsKey(KEY_PING_GLOWING))
            pingGlowing = cfg.getBoolean(KEY_PING_GLOWING);
        if(cfg.containsKey(KEY_PING_GLOWING_FLASH))
            pingGlowingFlash = cfg.getBoolean(KEY_PING_GLOWING_FLASH);
        if(cfg.containsKey(KEY_PING_PLAY_SOUND)) {
            playSound = cfg.getBoolean(KEY_PING_PLAY_SOUND);
        }
        if(cfg.containsKey(KEY_PING_ITEM)) {
            String itemIdentifier = Objects.requireNonNull(cfg.getString(KEY_PING_ITEM));
            Identifier pingItemIdentifier = new Identifier(itemIdentifier);
            if(!Registry.ITEM.containsId(pingItemIdentifier))
                pingItem = Items.BLUE_STAINED_GLASS;
            else
                pingItem = Registry.ITEM.get(pingItemIdentifier);
        }

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

    public boolean setLangauge(String language) {
        if(language.equals(this.defaultLanguage)) return false;

        this.defaultLanguage = language;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setPlaySound(boolean playSound) {
        if(playSound == this.playSound) return false;

        this.playSound = playSound;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setGlowing(boolean glowing) {
        if(glowing == this.pingGlowing) return false;

        this.pingGlowing = glowing;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setPingItem(Item item) {
        if(item == this.pingItem) return false;

        this.pingItem = item;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setGlowingFlash(boolean glowingFlash) {
        if(glowingFlash == this.pingGlowingFlash) return false;

        this.pingGlowingFlash = glowingFlash;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setItemCountRange(double range) {
        if(range == this.pingItemCountRange) return false;

        this.pingItemCountRange = range;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setPingRange(double range) {
        if(range == this.pingRange) return false;

        this.pingRange = range;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setPingChatMessageRange(double range) {
        if(range == this.pingChatMessageRange) return false;

        this.pingChatMessageRange = range;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setPingDirectionMessageRange(double range) {
        if(range == this.pingDirectionMessageRange) return false;

        this.pingDirectionMessageRange = range;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setPingCooldown(int ticks) {
        if(ticks == this.pingCooldown) return false;

        this.pingCooldown = ticks;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean setPingItemCount(boolean pingItemCount) {
        if(pingItemCount == this.pingItemCount) return false;

        this.pingItemCount = pingItemCount;
        saveSettings();
        invokeSettingsRefreshed();

        return true;
    }

    public boolean isPlaySound() {
        return playSound;
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

    public boolean isPingGlowing() {
        return pingGlowing;
    }

    public boolean isPingGlowingFlash() {
        return pingGlowingFlash;
    }

    public Item getPingItem() {
        return pingItem;
    }

    private void invokeSettingsRefreshed() {
        settingEvents.forEach(SettingsEvent::refresh);
    }

    @FunctionalInterface
    public interface SettingsEvent {
        void refresh();
    }
}
