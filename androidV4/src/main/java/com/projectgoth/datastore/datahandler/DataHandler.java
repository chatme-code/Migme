/**
 * Copyright (c) 2013 Project Goth
 *
 * DataHandler.java
 * Created Jan 23, 2015, 11:58:12 AM
 */

package com.projectgoth.datastore.datahandler;

import android.os.Bundle;

import com.projectgoth.common.Logger;
import com.projectgoth.datastore.DataCache;
import com.projectgoth.datastore.datahandler.MemCacheHandler.DefaultMemCacheHandler;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.util.AndroidLogger;

//@formatter:off
/**
 * 
 * 
 * This class handles the caching to memory / persistence / as well as
 * downloading data from the server.
 * 
 * the different layers needs to be implemented in order for this to work. 
 * Each layers will work independent against each other  
 * 
 * 
 * layers:
 * - memory:  
 * - persistence: 
 * - server:  
 * 
 * @author warrenbalcos
 */
// @formatter:on
public abstract class DataHandler<T> {
    
    private static final String    LOG_TAG = AndroidLogger.makeLogTag(DataHandler.class);

    /**
     * Abstract handler for the memory cache
     */
    private MemCacheHandler<T>    memCache;

    /**
     * Abstract handler for the server data fetching
     */
    private ServerHandler<T>      serverHandler;

    /**
     * Abstract handler for the persistence storage
     */
    private PersistenceHandler<T> persistenceHandler;

    public DataHandler() {
        super();
    }

    /**
     * Only sent if fetching from the server succeeds
     * 
     * @param data
     */
    public abstract void broadcastSuccessEvent(final T data);

    /**
     * Only sent if fetching from the server fails
     * 
     * @param data
     */
    public abstract void broadcastFailEvent(final MigError error);

    /**
     * Implement how the cache key is formed
     * 
     * @param data
     */
    public abstract String getKey(final Bundle params);

    /**
     * Use the default memory cache handler. Uses {@link DataCache}
     * 
     * @param size
     *            - LRU cache size
     * @param expiry
     *            - cache item expiry in seconds
     * @return
     */
    public DataHandler<T> useDefaultMemCache(int size, int expiry) {
        memCache = new DefaultMemCacheHandler<T>(size, expiry);
        return this;
    }

    /**
     * Use the default memory cache handler. Uses {@link DataCache}
     * 
     * defaults: - size: {@link MemCacheHandler#DEFAULT_MEM_CACHE_SIZE} -
     * expiry: {@link MemCacheHandler#DEFAULT_CACHE_EXPIRY}
     * 
     * modify the default by using {@link #useDefaultMemCache(int, int)}
     * 
     * @return
     */
    public DataHandler<T> useDefaultMemCache() {
        Logger.debug.log(LOG_TAG, "useDefaultMemCache Y_(^_^)_Y ");
        memCache = new DefaultMemCacheHandler<T>();
        return this;
    }

    /**
     * Get the data from different sources. Handles caching logic as well as
     * fetching from the server
     * 
     * @param params
     * @param force
     *            - force fetch the data from the server
     * @return
     */
    public T getData(Bundle params, boolean force) {
        final String key = getKey(params);

        T result = null;

        // check and get if there is data in memory
        Logger.debug.log(LOG_TAG, "DataHandler.getData([", key, "],[", force, "])");
        if (memCache != null) {
            result = memCache.getData(key);
        }
        Logger.debug.log(LOG_TAG, "-> (M) handler: ", memCache, " result: ", result);

        // check and get if there is persisted data and cache it in the memory
        // as well
        if (persistenceHandler != null) {
            if (result == null || (memCache != null && memCache.isExpired(key))) {
                result = persistenceHandler.getPersistedData(key);
                if (memCache != null) {
                    memCache.setData(key, result);
                }
            }
        }
        Logger.debug.log(LOG_TAG, "-> (P) handler: ", persistenceHandler, " result: ", result);

        // fetch the data from the server and save it on memory and persistence
        if (serverHandler != null) {
            if (result == null || force || (persistenceHandler != null && persistenceHandler.isExpired(key))) {
                serverHandler.doFetch(params, new ServerResult<T>() {

                    @Override
                    public void onSuccess(Bundle bundle, T data) {
                        String key = getKey(bundle);
                        if (persistenceHandler != null) {
                            persistenceHandler.persistData(key, data);
                        }
                        if (memCache != null) {
                            memCache.setData(key, data);
                        }
                        broadcastSuccessEvent(data);
                    }

                    @Override
                    public void onFail(MigError error) {
                        broadcastFailEvent(error);
                    }
                });
            }
        }
        Logger.debug.log(LOG_TAG, "-> (S) handler: ", serverHandler, " result: ", result);

        return result;
    }

    /**
     * @param memCache
     *            the memCache to set
     */
    public void setMemCache(MemCacheHandler<T> memCache) {
        this.memCache = memCache;
    }

    /**
     * @param serverHandler
     *            the serverHandler to set
     */
    public void setServerHandler(ServerHandler<T> serverHandler) {
        this.serverHandler = serverHandler;

    }

    /**
     * @param persistenceHandler
     *            the persistenceHandler to set
     */
    public void setPersistenceHandler(PersistenceHandler<T> persistenceHandler) {
        this.persistenceHandler = persistenceHandler;
    }

}
