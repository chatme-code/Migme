package com.sleepycat.je;
public class TransactionConfig {
    public static final TransactionConfig DEFAULT = new TransactionConfig();
    public TransactionConfig setReadOnly(boolean readOnly) { return this; }
    public TransactionConfig setReadUncommitted(boolean readUncommitted) { return this; }
}
