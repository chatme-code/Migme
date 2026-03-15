package com.sleepycat.je;
public class EnvironmentConfig {
    public static final EnvironmentConfig DEFAULT = new EnvironmentConfig();
    public EnvironmentConfig setAllowCreate(boolean allow) { return this; }
    public EnvironmentConfig setTransactional(boolean transactional) { return this; }
    public EnvironmentConfig setReadOnly(boolean readOnly) { return this; }
}
