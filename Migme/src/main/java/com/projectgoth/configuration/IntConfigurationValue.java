package com.projectgoth.configuration;

public abstract class IntConfigurationValue extends ConfigurationValue {
    private int value;

    public IntConfigurationValue(String namespace, String name, int defaultValue) {
        super(namespace, name);
        this.value = defaultValue;
    }

    public IntConfigurationValue(String namespace, String name, ConfigurationValue defaultValue) {
        super(namespace, name);
        this.value = 0;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
