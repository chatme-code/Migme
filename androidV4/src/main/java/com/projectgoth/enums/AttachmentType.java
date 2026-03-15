/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentType.java
 * Created Jul 14, 2013, 6:05:00 PM
 */

package com.projectgoth.enums;

/**
 * @author mapet
 *
 */
public enum AttachmentType {
    EMOTICON(0), 
    GIFT(1), 
    STICKER(2), 
    PHOTO(3), 
    RECENT_EMOTICON(4), 
    RECENT_STICKER_GIFT(5), 
    STORE_EMOTICON(6),
    STORE_STICKER(7),
    STICKER_ENTRY(8);

    public int value;

    private AttachmentType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static AttachmentType fromValue(int value) {
        for (AttachmentType type : values()) {
            if (type.value == value)
                return type;
        }
        return null;
    }
}
