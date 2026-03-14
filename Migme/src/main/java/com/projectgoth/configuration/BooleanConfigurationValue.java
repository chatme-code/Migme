package com.projectgoth.configuration;

public abstract class BooleanConfigurationValue extends ConfigurationValue {
    private boolean value;

    public BooleanConfigurationValue(String namespace, String name, boolean defaultValue) {
        super(namespace, name);
        this.value = defaultValue;
    }

    public BooleanConfigurationValue(String namespace, String name, BooleanConfigurationValue defaultValue) {
        super(namespace, name);
        this.value = defaultValue != null && defaultValue.getValue();
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
