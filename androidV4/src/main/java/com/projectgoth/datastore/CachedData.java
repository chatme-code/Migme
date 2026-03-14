/**
 * Mig33 Pte. Ltd.
 *
 * Copyright (c) 2012 mig33. All rights reserved.
 */
package com.projectgoth.datastore;

/**
 * CachedData.java
 * 
 * @author warrenbalcos on Jun 4, 2013
 * 
 */
public class CachedData<W> {
	
	private final W		  data;
	private final long	  expiry;
	private final boolean shouldExpire;
	
	/**
	 * @param data
	 *            - the data to cache
	 * @param expiry
	 *            - data expiry in seconds.
	 */
	public CachedData(W data, long expiry, boolean shouldExpire) {
		this.data = data;
		this.expiry = System.currentTimeMillis() + (expiry * 1000);
		this.shouldExpire = shouldExpire;
	}
	
	public W getData() {
		return data;
	}
	
	public boolean isExpired() {
		return (shouldExpire == true && System.currentTimeMillis() >= expiry);
	}
	
}
