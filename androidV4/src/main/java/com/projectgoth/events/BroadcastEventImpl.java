/**
 * Copyright (c) 2013 Project Goth
 *
 * BroadcastEventImpl.java
 * Created Feb 4, 2014, 6:25:49 PM
 */

package com.projectgoth.events;

import android.content.Intent;
import android.os.Bundle;
import com.mig33.diggle.events.BroadcastEvent;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author angelorohit
 *
 */
public class BroadcastEventImpl implements BroadcastEvent {

    // The intent that is internally wrapped.
    private Intent intent = null;
    
    /**
     * Constructor
     * @param name
     */
    public BroadcastEventImpl(final String name) {
        intent = new Intent(name);
    }
    
    /**
     * @return the intent wrapped by this {@link BroadcastEventImpl} 
     */
    public Intent getIntent() {
        return intent;
    }
    
    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getName()
     */
    @Override
    public String getName() {
        return intent.getAction();
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        intent.setAction(name);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, int)
     */
    @Override
    public void putExtra(String key, int data) {
        intent.putExtra(key, data);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, boolean)
     */
    @Override
    public void putExtra(String key, boolean data) {
        intent.putExtra(key, data);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, char)
     */
    @Override
    public void putExtra(String key, char data) {
        intent.putExtra(key, data);        
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, float)
     */
    @Override
    public void putExtra(String key, float data) {
        intent.putExtra(key, data);        
    }
    
    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, short)
     */
    @Override
    public void putExtra(String key, short data) {
        intent.putExtra(key, data);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, long)
     */
    @Override
    public void putExtra(String key, long data) {
        intent.putExtra(key, data);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, byte)
     */
    @Override
    public void putExtra(String key, byte data) {
        intent.putExtra(key, data);
    }
    
    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, java.lang.String)
     */
    @Override
    public void putExtra(String key, String data) {
        intent.putExtra(key, data);
    }
    
    /**
     * @see com.mig33.diggle.events.BroadcastEvent#putExtra(java.lang.String, String[])
     */
    @Override
    public void putExtra(String key, String[] data) {
        if (data != null) {
            intent.putStringArrayListExtra(key, new ArrayList<String>(Arrays.asList(data)));
        }
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getIntExtra(java.lang.String, int)
     */
    @Override
    public int getIntExtra(String key, int defaultValue) {
        return intent.getIntExtra(key, defaultValue);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getBooleanExtra(java.lang.String, boolean)
     */
    @Override
    public boolean getBooleanExtra(String key, boolean defaultValue) {
        return intent.getBooleanExtra(key, defaultValue);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getCharacterExtra(java.lang.String, char)
     */
    @Override
    public char getCharacterExtra(String key, char defaultValue) {
        return intent.getCharExtra(key, defaultValue);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getFloatExtra(java.lang.String, float)
     */
    @Override
    public float getFloatExtra(String key, float defaultValue) {
        return intent.getFloatExtra(key, defaultValue);
    }
    
    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getShortExtra(java.lang.String, short)
     */
    @Override
    public short getShortExtra(String key, short defaultValue) {
        return intent.getShortExtra(key, defaultValue);
    }
    
    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getByteExtra(java.lang.String, byte)
     */
    @Override
    public byte getByteExtra(String key, byte defaultValue) {
        return intent.getByteExtra(key, defaultValue);
    }

    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getStringExtra(java.lang.String)
     */
    @Override
    public String getStringExtra(String key) {
        return intent.getStringExtra(key);
    }
    
    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getStringArrayExtra(java.lang.String)
     */
    @Override
    public String[] getStringArrayExtra(String key) {
        ArrayList<String> arrList = intent.getStringArrayListExtra(key);
        if (arrList != null) {
            return arrList.toArray(new String[arrList.size()]);
        }
        
        return null;
    }
    
    /**
     * @see com.mig33.diggle.events.BroadcastEvent#getExtra(String)
     */
    @Override
    public Object getExtra(String key) {
        Bundle bundle = intent.getExtras();
        return bundle.get(key);
    }
}
