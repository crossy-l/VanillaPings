package com.vanillapings.translation;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.vanillapings.VanillaPings;
import com.vanillapings.compat.Compat;
import com.vanillapings.config.FileConfig;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Translator {
    public static final String DEFAULT_LANGUAGE = "en_us";
    public static Map<String, Translator> languages = new HashMap<>();
    private static final String RESOURCE_BASE = "assets/vanillapings/lang";
    private Map<String, String> translations;

    public static boolean loadLanguage(String name) {
        if (tryLoadFromFile(name))
            return true;

        return tryLoadFromClasspath(name);
    }

    private static boolean tryLoadFromClasspath(String name) {
        String resource = RESOURCE_BASE + "/" + name + ".json";

        try (InputStream in = VanillaPings.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                VanillaPings.LOGGER.error(
                        "Failed to load {} language '{}': resource '{}' not found on classpath.",
                        VanillaPings.MOD_NAME, name, resource
                );
                return false;
            }

            return loadIntoRegistry(name, in);
        } catch (IOException | JsonParseException ex) {
            VanillaPings.LOGGER.error(
                    "Failed to load {} language '{}' from classpath.",
                    VanillaPings.MOD_NAME, name, ex
            );
            return false;
        }
    }

    private static boolean tryLoadFromFile(String name) {
        Path file = Paths.get(FileConfig.CONFIG_FOLDER, "lang", name + ".json");

        if (!Files.exists(file)) {
            VanillaPings.LOGGER.info("No custom language file found for '{}'", name);
            return false;
        }

        try (InputStream in = Files.newInputStream(file)) {
            return loadIntoRegistry(name, in);
        } catch (IOException | JsonParseException ex) {
            VanillaPings.LOGGER.error(
                    "Failed to load {} custom language file for '{}'",
                    VanillaPings.MOD_NAME, name, ex
            );
            return false;
        }
    }

    private static boolean loadIntoRegistry(String languageName, InputStream in) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             JsonReader reader = new JsonReader(isr)) {

            Translator translator = new Translator();
            Gson gson = new Gson();
            translator.translations = gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
            languages.put(languageName, translator);
            return true;
        }
    }

    public String get(String key) {
        key = parseKey(key);

        if(!translations.containsKey(key))
            return key;
        return translations.get(key);
    }

    public MutableComponent getAsText(String key) {
        return Component.literal(get(key));
    }

    private String parseKey(String key) {
        if(key.contains(":")) {
            var id = Compat.id(key);
            return id.getNamespace() + "." + id.getPath();
        }
        return key;
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
