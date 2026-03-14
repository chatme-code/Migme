/**
 * Copyright (c) 2013 Project Goth
 *
 * DirtyData.java
 * Created Jul 30, 2013, 7:15:05 PM
 */

package com.projectgoth.datastore;

/**
 * The purpose of this class is to hold data that can be marked as altered or
 * removed.
 * 
 * @author angelorohit
 */
public class VersionedData<T> {

    private T       data      = null;

    // Indicates that the data was simply altered
    // and needs to be refreshed.
    private boolean isAltered = false;

    // Indicates that the data was removed and should
    // not exist any more.
    private boolean isRemoved = false;

    private long    lastUpdateTimestamp;

    public VersionedData() {
    }

    /**
     * Constructor
     * 
     * @param data
     */
    public VersionedData(final T data) {
        setData(data);
    }

    /**
     * Returns the data within.
     * 
     * @return The data to be returned. Can be null.
     */
    public T getData() {
        return data;
    }

    public void setData(final T data) {
        this.data = data;
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }

    public boolean isAltered() {
        return isAltered;
    }

    public void setIsAltered(final boolean isAltered) {
        this.isAltered = isAltered;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(final boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    /**
     * @return the lastUpdateTimestamp
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * @param lastUpdateTimestamp
     *            the lastUpdateTimestamp to set
     */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
    
    /**
     * Calculates the remaining time left for this {@link VersionedData} to
     * expire.
     * 
     * @param expiryTime
     *            The time (ms) within which this {@link VersionedData} is supposed
     *            to expire.
     * @return The remaining time left for this {@link VersionedData} to expire.
     *         If the {@link VersionedData} is already expired, then zero is
     *         returned.
     */
    public long getRemainingExpiryTime(long expiryTime) {
        final long remainingExpiryTime = expiryTime - (System.currentTimeMillis() - lastUpdateTimestamp);
        return (remainingExpiryTime < 0) ? 0 : remainingExpiryTime;
    }

    /**
     * Checks whether this {@link VersionedData} has expired.
     * 
     * @param expiryTime
     *            The time (ms) within which this {@link VersionedData} is supposed
     *            to have expired.
     * @return true if the data has expired and false otherwise.
     * @see #getRemainingExpiryTime(long)
     */
    public boolean isExpired(long expiryTime) {
        return getRemainingExpiryTime(expiryTime) == 0;
    }
}
