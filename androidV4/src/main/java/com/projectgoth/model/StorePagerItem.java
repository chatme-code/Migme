/**
 * Copyright (c) 2013 Project Goth
 *
 * StorePagerType.java
 * Created Nov 21, 2013, 2:53:08 PM
 */

package com.projectgoth.model;

/**
 * @author mapet
 * 
 */
public class StorePagerItem {

    private String         label;
    private StorePagerType type;

    public StorePagerItem(String label, StorePagerType type) {
        super();
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public StorePagerType getType() {
        return type;
    }

    public enum StorePagerType {
        GIFTS(1), STICKERS(6), EMOTICONS(3), AVATAR(2);

        private int mValue;

        private StorePagerType(final int value) {
            mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }

        public static StorePagerType fromValue(int value) {
            for (StorePagerType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return null;
        }
    }

}
