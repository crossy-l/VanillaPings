package com.vanillapings.translation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.vanillapings.VanillaPings;
import com.vanillapings.config.FileConfig;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Translator {
    public static final String DEFAULT_LANGUAGE = "en_us";
    public static Map<String, Translator> languages = new HashMap<>();
    private Map<String, String> translations;

    public static boolean loadLanguage(String name) {
        if(loadLanguageFromFile(name))
            return true;
        try {
            try (InputStream langStr = VanillaPings.getInstance().getClass().getClassLoader().getResourceAsStream("assets/vanillapings/lang/" + name + ".json")) {
                JsonReader reader = new JsonReader(new InputStreamReader(Objects.requireNonNull(langStr)));
                Translator translator = new Translator();
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Gson gson = new Gson();
                translator.translations = gson.fromJson(reader, type);
                languages.put(name, translator);
            }
        } catch (Exception ex) {
            VanillaPings.LOGGER.error("Failed to load " + VanillaPings.MOD_NAME + " language files: " + ex);
            return false;
        }
        return true;
    }

    private static boolean loadLanguageFromFile(String name) {
        try {
            File langFile = new File(FileConfig.CONFIG_FOLDER + "/lang/" + name + ".json");
            try (InputStream langStr = new FileInputStream(langFile)) {
                JsonReader reader = new JsonReader(new InputStreamReader(langStr, StandardCharsets.UTF_8));
                Translator translator = new Translator();
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Gson gson = new Gson();
                translator.translations = gson.fromJson(reader, type);
                languages.put(name, translator);
                return true;
            }
        } catch (Exception ex) {
            if(ex instanceof FileNotFoundException)
                VanillaPings.LOGGER.info("No custom language file found. Using default");
            else
                VanillaPings.LOGGER.error("Failed to load " + VanillaPings.MOD_NAME + " custom language file for '" + name + "'. Error: " + ex);
        }
        return false;
    }

    public String get(String key) {
        if(!translations.containsKey(key))
            return key;
        return translations.get(key);
    }

    public MutableText getAsText(String key) {
        if(!translations.containsKey(key))
            return Text.literal(key);
        return Text.literal(translations.get(key));
    }

    public static void clearTranslators() {
        languages.clear();
    }

    public static Translator getTranslator(String key) {
        if(!languages.containsKey(key)) {
            loadLanguage(key);
        }
        if(!languages.containsKey(key)) {
            VanillaPings.LOGGER.error("Can't load translator for " + key + " falling back to: " + DEFAULT_LANGUAGE);
            loadLanguage(DEFAULT_LANGUAGE);
            return languages.get(DEFAULT_LANGUAGE);
        }
        return languages.get(key);
    }

    public static Translator getTranslator() {
        return getTranslator(VanillaPings.SETTINGS.getDefaultLanguage());
    }
}
