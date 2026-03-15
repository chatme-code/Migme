package com.sleepycat.je;
public class Transaction {
    public void commit() throws DatabaseException {}
    public void abort() throws DatabaseException {}
    public long getId() { return 0; }
}
