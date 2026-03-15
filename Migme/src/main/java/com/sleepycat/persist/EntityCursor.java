package com.sleepycat.persist;
import com.sleepycat.je.DatabaseException;
public interface EntityCursor<V> extends AutoCloseable {
    V first() throws DatabaseException;
    V next() throws DatabaseException;
    V last() throws DatabaseException;
    void close() throws DatabaseException;
    int count() throws DatabaseException;
}
