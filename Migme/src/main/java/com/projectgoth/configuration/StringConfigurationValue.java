package com.projectgoth.configuration;

public abstract class StringConfigurationValue extends ConfigurationValue {
    private String value;

    public StringConfigurationValue(String namespace) {
        super(namespace);
        this.value = "";
    }

    public StringConfigurationValue(String namespace, String name, String defaultValue) {
        super(namespace, name);
        this.value = defaultValue;
    }

    public StringConfigurationValue(String namespace, String name) {
        super(namespace, name);
        this.value = "";
    }

    public String getValue() {
        return value;
    }

    public String get() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
