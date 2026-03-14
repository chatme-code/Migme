/**
 * Copyright (c) 2013 Project Goth
 *
 * MemCacheHandler.java
 * Created Jan 27, 2015, 10:44:53 AM
 */

package com.projectgoth.datastore.datahandler;

import com.projectgoth.common.Logger;
import com.projectgoth.datastore.DataCache;

/**
 * @author warrenbalcos
 * 
 */
public abstract class MemCacheHandler<T> {

    public static final int DEFAULT_CACHE_SIZE   = 5;
    public static final int DEFAULT_CACHE_EXPIRY = 60 * 10;

    private int             size;
    private int             expiry;

    public MemCacheHandler() {
        this(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_EXPIRY);
    }

    /**
     * @param size
     * @param expiry
     */
    public MemCacheHandler(int size, int expiry) {
        super();
        this.size = size;
        this.expiry = expiry;
        initCache(size, expiry);
    }

    public abstract void initCache(int size, int expiry);

    public abstract void setData(String key, T data);

    public abstract T getData(String key);

    public abstract boolean isExpired(String key);

    public abstract T removeData(String key);

    public abstract void clear();

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the expiry
     */
    public int getExpiry() {
        return expiry;
    }

    /**
     * The default {@link MemCacheHandler} implementation based on {@link DataCache}
     * 
     * @author warrenbalcos
     *
     * @param <W>
     */
    public static class DefaultMemCacheHandler<W> extends MemCacheHandler<W> {

        private DataCache<W> cache;

        public DefaultMemCacheHandler() {
            super();
        }

        /**
         * a -1 expiry means cached data doesn't expire
         * 
         * @param size
         * @param expiry
         */
        public DefaultMemCacheHandler(int size, int expiry) {
            super(size, expiry);
        }
        
        @Override
        public void initCache(int size, int expiry) {
            clear();
            cache = new DataCache<W>(size, expiry);
        }

        @Override
        public void setData(String key, W data) {
            cache.cacheData(key, data);
        }

        @Override
        public W getData(String key) {
            return cache.getData(key);
        }

        @Override
        public W removeData(String key) {
            return cache.removeData(key);
        }

        @Override
        public void clear() {
            Logger.debug.log("GIFT_TEST", "clearing mem cache Y_(^_^)_Y ");
            if (cache != null) {
                cache.clear();
            }
        }

        @Override
        public boolean isExpired(String key) {
            return cache.isExpired(key);
        }
    }

}
