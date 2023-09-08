package com.vanillapings.config;

import com.vanillapings.VanillaPings;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class FileConfig {
    public static final String CONFIG_FOLDER = Paths.get(System.getProperty("user.dir"), "config/" + VanillaPings.MOD_NAME.toLowerCase()).toString();
    private final String filePath;
    private final File file;
    private final Properties properties = new Properties();
    private final boolean printErrors;

    public FileConfig(String filePath) {
        this(filePath, true);
    }

    public FileConfig(String filePath, boolean printErrors) {
        this.printErrors = printErrors;
        this.file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            if(printErrors)
                VanillaPings.LOGGER.warn("Couldn't create dir: " + parent);
        }

        this.filePath = filePath;
        loadConfig();
    }

    public void put(String key, Object value) {
        properties.put(key, value.toString());
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public @Nullable Object get(String key) {
        return properties.get(key);
    }

    public @Nullable String getString(String key) {
        return (String) properties.get(key);
    }

    public int getInteger(String key) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public float getFloat(String key) {
        try {
            return Float.parseFloat(properties.getProperty(key));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public double getDouble(String key) {
        try {
            return Double.parseDouble(properties.getProperty(key));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public void clear() {
        properties.clear();
    }

    public void loadConfig() {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
        } catch (IOException ex) {
            if(printErrors)
                VanillaPings.LOGGER.warn("Couldn't load " + filePath);
        }
    }

    public void saveConfig() {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, file.getName());
        } catch (IOException ex) {
            if(printErrors)
                VanillaPings.LOGGER.error("Couldn't write to " + file);
        }
    }

    public void deleteConfig() {
        clear();
        try {
            if(!file.delete())
                if(printErrors)
                    VanillaPings.LOGGER.error("Couldn't delete config" + file);
        } catch (SecurityException ex) {
            if(printErrors)
                VanillaPings.LOGGER.error("Couldn't delete config " + file);
        }
    }
}