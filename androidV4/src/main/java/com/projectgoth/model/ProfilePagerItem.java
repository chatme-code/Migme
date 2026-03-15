/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfilePagerItem.java
 * Created Sep 25, 2013, 2:16:36 PM
 */

package com.projectgoth.model;

/**
 * @author dangui
 * 
 */
public class ProfilePagerItem {

    private String label;
    private PagerType type;

    public ProfilePagerItem(String label, PagerType type) {
        super();
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }
    
    public PagerType getType() {
        return type;
    }
    
    public enum PagerType {
        USER_POSTS(0),
        BADGES(1),
        GIFTS(2),
        FOLLOWERS(3),
        FOLLOWING(4),
        GROUPS(5),
        GAMES(6),
        CHATROOMS(7),
        PHOTOS(8),
        FOOTPRINTS(9);

        private int mValue;

        private PagerType(final int value) {
            mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }

        public static PagerType fromValue(int value) {
            for (PagerType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return null;
        }
    }
}
