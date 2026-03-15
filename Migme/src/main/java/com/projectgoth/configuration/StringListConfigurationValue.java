package com.projectgoth.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class StringListConfigurationValue extends ConfigurationValue {
    private String rawValue;

    public StringListConfigurationValue(String namespace, String name, String defaultValue) {
        super(namespace, name);
        this.rawValue = defaultValue != null ? defaultValue : "";
    }

    public StringListConfigurationValue(String namespace, String name) {
        super(namespace, name);
        this.rawValue = "";
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public List<String> getValue() {
        if (rawValue == null || rawValue.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(rawValue.split(","));
    }

    public List<String> get() {
        return getValue();
    }
}
