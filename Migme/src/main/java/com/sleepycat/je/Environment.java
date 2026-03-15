package com.sleepycat.je;
import java.io.File;
public class Environment implements AutoCloseable {
    public Environment(File envHome, EnvironmentConfig config) throws DatabaseException {}
    public Transaction beginTransaction(Transaction parent, TransactionConfig config) throws DatabaseException { return null; }
    public EnvironmentStats getStats(StatsConfig config) throws DatabaseException { return null; }
    public void close() throws DatabaseException {}
}
