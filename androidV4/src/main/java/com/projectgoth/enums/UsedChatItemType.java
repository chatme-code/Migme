/**
 * Copyright (c) 2013 Project Goth
 *
 * UsedChatItemType.java
 * Created 23 May, 2014, 10:01:52 am
 */

package com.projectgoth.enums;


/**
 * @author Dan
 *
 */
public enum UsedChatItemType {
    STICKER(0),
    GIFT(1);
    
    int value;
    
    private UsedChatItemType(int value) {
        this.value = value;
    }
    
    public int value() {
        return value;
    }

    public static UsedChatItemType fromValue(int value) {
        for (UsedChatItemType type : values()) {
            if (type.value == value)
                return type;
        }
        return null;
    }

}
