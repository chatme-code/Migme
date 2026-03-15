package net.spy.memcached;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class MemCachedClient {

    public MemCachedClient() {
    }

    public boolean add(String key, Object value, Date expiry) {
        return false;
    }

    public boolean set(String key, Object value, Date expiry) {
        return false;
    }

    public boolean delete(String key) {
        return false;
    }

    public long incr(String key, long value) {
        return -1L;
    }

    public long decr(String key, long value) {
        return -1L;
    }

    public Object get(String key) {
        return null;
    }

    public long getCounter(String key) {
        return 0L;
    }

    public Map<String, Object> getMulti(String[] keys) {
        return new HashMap<>();
    }

    public boolean storeCounter(String key, long value) {
        return false;
    }

    public boolean storeCounter(String key, long value, Date expiry) {
        return false;
    }

    public boolean addOrDecr(String key, long value) {
        return false;
    }

    public boolean addOrIncr(String key, long value) {
        return false;
    }

    public boolean replace(String key, Object value, Date expiry) {
        return false;
    }

    public boolean keyExists(String key) {
        return false;
    }

    public void setSerializeNull(boolean serializeNull) {
    }

    public void shutdown() {
    }
}
