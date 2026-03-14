package com.projectgoth.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class PropertiesFileConfigurationNamespace {
    private final String filename;
    private Properties properties;

    public PropertiesFileConfigurationNamespace(String filename) {
        this.filename = filename;
    }

    protected abstract String getPath();

    public String getFilename() {
        return filename;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            String path = getPath();
            String fullPath = path + "/" + filename;
            try (FileInputStream fis = new FileInputStream(fullPath)) {
                properties.load(fis);
            } catch (IOException e) {
                // Return empty properties if file not found
            }
        }
        return properties;
    }

    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    public String getIdentifier() {
        return filename;
    }
}
