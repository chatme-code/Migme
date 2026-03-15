package com.sleepycat.persist;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
public class EntityStore implements AutoCloseable {
    public EntityStore(Environment env, String storeName, StoreConfig config) throws DatabaseException {}
    public <PK, E> PrimaryIndex<PK, E> getPrimaryIndex(Class<PK> keyClass, Class<E> entityClass) throws DatabaseException { return null; }
    public void close() throws DatabaseException {}
    public com.sleepycat.persist.evolve.Mutations getMutations() { return null; }
}
