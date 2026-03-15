package com.sleepycat.persist;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;
public class PrimaryIndex<PK, E> {
    public E get(PK key) throws DatabaseException { return null; }
    public E get(Transaction txn, PK key, com.sleepycat.je.LockMode lockMode) throws DatabaseException { return null; }
    public void put(E entity) throws DatabaseException {}
    public void put(Transaction txn, E entity) throws DatabaseException {}
    public boolean delete(PK key) throws DatabaseException { return false; }
    public boolean delete(Transaction txn, PK key) throws DatabaseException { return false; }
    public EntityCursor<E> entities() throws DatabaseException { return null; }
    public EntityCursor<E> entities(Transaction txn, com.sleepycat.je.LockMode lockMode) throws DatabaseException { return null; }
    public long count() throws DatabaseException { return 0; }
}
