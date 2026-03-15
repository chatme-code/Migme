/**
 * Copyright (c) 2013 Project Goth
 *
 * PersistenceHandler.java
 * Created Jan 28, 2015, 3:46:48 PM
 */

package com.projectgoth.datastore.datahandler;

/**
 * @author warrenbalcos
 * 
 */
public interface PersistenceHandler<T> {

    /**
     * Fetch the persisted data based on the key
     * 
     * @param params
     * @return
     */
    public T getPersistedData(final String key);

    /**
     * Store the data on a persistent storage based on a key
     * 
     * @param key
     * @param data
     */
    public void persistData(final String key, final T data);

    /**
     * Check if the data is expired
     * 
     * @param key
     * @return
     */
    public boolean isExpired(final String key);

}
