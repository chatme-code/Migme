/**
 * Mig33 Pte. Ltd.
 *
 * Copyright (c) 2012 mig33. All rights reserved.
 */

package com.projectgoth.datastore;

import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;

import com.projectgoth.common.Logger;

import java.util.Map;

/**
 * DataCache.java
 * 
 * @author warrenbalcos on May 2, 2013
 * 
 */
public class DataCache<T> {

    private static final String             TAG                          = "DataCache";

    private static final int                DAFAULT_CACHE_MAX_SIZE       = 200;

    private static final long               DEFAULT_RESPONSE_DATA_EXPIRY = 60;

    /** Used to denote that cached data should not expire */
    private static final long               NO_EXPIRY                    = -1;

    /** The time within which this cache should expire */
    private long                            expiry                       = DEFAULT_RESPONSE_DATA_EXPIRY;

    private LruCache<String, CachedData<T>> cache;

    /**
     * defaults to a cache size of {@value #DAFAULT_CACHE_MAX_SIZE}
     */
    public DataCache() throws Exception {
        this(DAFAULT_CACHE_MAX_SIZE);
    }

    /**
     * @param cacheSize
     *            - size of the cache map, must be greater the zero
     */
    public DataCache(int cacheSize) {
        if (cacheSize <= 0) {
            throw new RuntimeException("cacheSize must be greater than zero");
        }
        cache = new LruCache<String, CachedData<T>>(cacheSize);
    }

    /**
     * Constructor
     * 
     * @param cacheSize
     *            Size of the cache map, must be greater than zero.
     * @param expiry
     *            Time (seconds) within which the cache will expire. A negative
     *            expiry time indicates that the cache should not expire.
     * @throws Exception
     */
    public DataCache(final int cacheSize, final long expiry) {
        this(cacheSize);
        this.expiry = expiry;
    }

    /**
     * Constructor
     * 
     * @param shouldExpire
     *            Indicates whether the cache map should expire or not.
     * @throws Exception
     */
    public DataCache(final boolean shouldExpire) {
        this(DAFAULT_CACHE_MAX_SIZE, shouldExpire);
    }

    /**
     * Constructor.
     * 
     * @param cacheSize
     *            Size of the cache map, must be greater than zero.
     * @param shouldExpire
     *            Indicates whether the cache map should expire or not.
     * @throws Exception
     */
    public DataCache(final int cacheSize, final boolean shouldExpire) {
        this(cacheSize);

        if (!shouldExpire) {
            this.expiry = NO_EXPIRY;
        }
    }

    /**
     * Store the data in cache
     * 
     * @param key
     * @param data
     */
    public void cacheData(@NonNull String key, T data) {
        cacheData(key, data, this.expiry);
    }

    /**
     * Store the data in cache
     * 
     * @param key
     * @param data
     * @param expiry
     */
    public void cacheData(@NonNull String key, T data, long expiry) {
        if (data == null) {
            return;
        }

        Logger.debug.log(TAG, "set cache key: " + key + " data: " + data);
        cache.put(key, new CachedData<T>(data, expiry, (expiry <= NO_EXPIRY) ? false : true));
    }

    public boolean isExpired(String key) {
        CachedData<T> data = cache.get(key);
        return data == null || data.isExpired();
    }

    public T getData(@NonNull String key) {
        T result = null;
        CachedData<T> data = getCachedData(key);
        if (data != null) {
            result = data.getData();
        }
        return result;
    }

    /**
     * Removes the entry for key if it exists
     * 
     * @param key
     *            Unique key for value to remove
     * @return Previous value mapped to key
     */
    public T removeData(@NonNull String key) {
        CachedData<T> cachedData = cache.remove(key);
        if (cachedData != null) {
            return cachedData.getData();
        }

        return null;
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        cache.evictAll();
    }

    public CachedData<T> getCachedData(@NonNull String key) {
        if (key == null) {
           Logger.error.logWithTrace(TAG, getClass(), "unexpected error trying to get null key");
           return null;
        }
        
       CachedData<T> data = cache.get(key);
       Logger.debug.log(TAG, "get cache key: " + key + " data: " + (data != null ? data.getData() : "<no data>"));
       return data;
    }

    public final Map<String, CachedData<T>> snapshot() {
        return cache.snapshot();
    }
}
