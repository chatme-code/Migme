/**
 * Copyright (c) 2013 Project Goth
 *
 * CaseInsensitiveHashMap.java
 * Created Aug 6, 2014, 5:13:45 PM
 */

package com.projectgoth.common;

import java.util.HashMap;

/**
 * A {@link HashMap} implementation that simply provides a case-insensitive way to get
 * and put String keys.
 * 
 * @author angelorohit
 * 
 */
@SuppressWarnings("serial")
public class CaseInsensitiveHashMap<T> extends HashMap<String, T> {

    @Override
    public T put(String key, T value) {
        return super.put(key.toLowerCase(), value);
    }

    // not @Override because that would require the key parameter to be of type
    // Object
    public T get(String key) {
        return super.get(key.toLowerCase());
    }
}
