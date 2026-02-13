package config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "urlshortener.properties";
    private static final String DEFAULT_CONFIG_PATH = "config/" + CONFIG_FILE;
    private Properties properties;

    public static final String LINK_LIFETIME_HOURS = "link.lifetime.hours";
    public static final String BASE_URL = "base.url";
    public static final String SHORT_CODE_LENGTH = "short.code.length";

    private static final int DEFAULT_LIFETIME_HOURS = 24;
    private static final String DEFAULT_BASE_URL = "clck.ru/";
    private static final int DEFAULT_CODE_LENGTH = 6;

    public AppConfig() {
        properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        Path externalPath = Paths.get(CONFIG_FILE);
        Path configPath = Paths.get(DEFAULT_CONFIG_PATH);

        try {
            if (Files.exists(externalPath)) {
                try (InputStream input = Files.newInputStream(externalPath)) {
                    properties.load(input);
                }
            }
            else if (Files.exists(configPath)) {
                try (InputStream input = Files.newInputStream(configPath)) {
                    properties.load(input);
                }
            }
            else {
                try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                    if (input != null) {
                        properties.load(input);
                    } else {
                        setDefaultProperties();
                        createDefaultConfig();
                    }
                }
            }
        } catch (IOException e) {
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        properties.setProperty(LINK_LIFETIME_HOURS, String.valueOf(DEFAULT_LIFETIME_HOURS));
        properties.setProperty(BASE_URL, DEFAULT_BASE_URL);
        properties.setProperty(SHORT_CODE_LENGTH, String.valueOf(DEFAULT_CODE_LENGTH));
    }

    private void createDefaultConfig() {
        Path configDir = Paths.get("config");
        Path configFile = Paths.get(DEFAULT_CONFIG_PATH);

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectory(configDir);
            }

            try (OutputStream output = Files.newOutputStream(configFile)) {
                properties.store(output, "URL Shortener Configuration File");
            }
        } catch (IOException e) {
            System.err.println("Не удалось создать базовый конфигурационный файл: " + e.getMessage());
        }
    }

    public int getLinkLifetimeHours() {
        return Integer.parseInt(properties.getProperty(LINK_LIFETIME_HOURS, String.valueOf(DEFAULT_LIFETIME_HOURS)));
    }

    public String getBaseUrl() {
        return properties.getProperty(BASE_URL, DEFAULT_BASE_URL);
    }

    public int getShortCodeLength() {
        return Integer.parseInt(properties.getProperty(SHORT_CODE_LENGTH, String.valueOf(DEFAULT_CODE_LENGTH)));
    }
}