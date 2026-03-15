/**
 * Copyright (c) 2013 Project Goth
 *
 * ViewPagerType.java
 * Created Mar 13, 2014, 10:06:51 AM
 */

package com.projectgoth.enums;

/**
 * @author mapet
 * 
 */
public enum ViewPagerType {
    CHAT_LIST(0), 
    CONTACT_LIST(1), 
    CHATROOM_LIST(2), 
    FOLLOWERS_LIST(3),
    FOLLOWING_LIST(4),
    BROWSER(5), 
    GIFT_LIST(6), 
    POST_FEEDS_LIST(7),
    POST_MENTIONS_LIST(8),
    POST_WATCHED_LIST(9),
    PROFILE_POST_LIST(10),
    PROFILE_FOLLOWERS_LIST(11),
    PROFILE_FOLLOWING_LIST(12),
    BADGES(13),
    BROWSER_GIFTS(14),
    BROWSER_GROUPS(15),
    BROWSER_GAMES(16),
    BROWSER_CHATROOMS(17),
    BROWSER_PHOTOS(18),
    BROWSER_FOOTPRINTS(19),
    STORE_GIFT(20),
    STORE_STICKER(21),
    STORE_EMOTICON(22),
    STORE_AVATAR(23),
    ATTACHMENT_GRID(24),
    GIFT_CENTER_POPULAR_GIFTS(25),
    GIFT_CENTER_NEW_GIFTS(26),
    GIFT_CENTER_CATEGORY_LIST(27),
    PROFILE_INFO(28),
    MY_GIFTS(29),
    MY_GIFTS_OVERVIEW(30);

    public int value;

    private ViewPagerType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static ViewPagerType fromValue(int value) {
        for (ViewPagerType type : values()) {
            if (type.value == value)
                return type;
        }
        return null;
    }
}
