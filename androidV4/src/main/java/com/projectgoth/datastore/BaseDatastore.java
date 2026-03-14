/**
 * Copyright (c) 2013 Project Goth
 * DatabaseHelper.java
 * 
 * Jun 3, 2013 11:29:07 AM
 */

package com.projectgoth.datastore;


/**
 * Serves as the base class for all datastore implementations.
 * 
 * @author angelorohit
 */
public abstract class BaseDatastore {

    // Constructor.
    protected BaseDatastore() {
        initData();
    }

    /**
     * Initialize the data caches for the datastore. NOTE: This function will be
     * called from clearData().
     */
    protected abstract void initData();

    /**
     * Clear the caches and persistent storage for the datastore.
     */
    public void clearData() {
        initData();
    }

}
