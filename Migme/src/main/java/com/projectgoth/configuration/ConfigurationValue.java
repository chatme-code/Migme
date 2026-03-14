package com.projectgoth.configuration;

public abstract class ConfigurationValue {
    protected final String namespace;
    protected final String name;

    public ConfigurationValue(String namespace) {
        this.namespace = namespace;
        this.name = null;
    }

    public ConfigurationValue(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }
}
