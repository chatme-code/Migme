/**
 * Copyright (c) 2013 Project Goth
 *
 * UsedChatItem.java
 * Created 23 May, 2014, 9:28:54 am
 */

package com.projectgoth.model;

import com.projectgoth.enums.UsedChatItemType;


/**
 * @author Dangui
 *
 */
public class UsedChatItem {

    private UsedChatItemType type;
    private String hotkey;
    private String storeItemId;
    
    public UsedChatItem(UsedChatItemType type, String hotkey) {
        this.type = type;
        this.hotkey = hotkey;
    }
    
    /**
     * @return the hotkey
     */
    public String getHotkey() {
        return hotkey;
    }

    /**
     * @param hotkey the hotkey to set
     */
    public void setHotkey(String hotkey) {
        this.hotkey = hotkey;
    }

    /**
     * @return the storeItemId
     */
    public String getStoreItemId() {
        return storeItemId;
    }

    /**
     * @param storeItemId the storeItemId to set
     */
    public void setStoreItemId(String storeItemId) {
        this.storeItemId = storeItemId;
    }

    /**
     * @return
     */
    public UsedChatItemType getType() {
        return type;
    }
}
