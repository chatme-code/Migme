package com.sleepycat.persist;
public class StoreConfig {
    public static final StoreConfig DEFAULT = new StoreConfig();
    public StoreConfig setAllowCreate(boolean allowCreate) { return this; }
    public StoreConfig setTransactional(boolean transactional) { return this; }
    public StoreConfig setReadOnly(boolean readOnly) { return this; }
}
