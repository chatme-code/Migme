/**
 * Copyright (c) 2013 Project Goth
 *
 * PostListType.java
 * Created Oct 19, 2014, 3:58:33 PM
 */

package com.projectgoth.enums;

import com.projectgoth.common.Constants;
import com.projectgoth.i18n.I18n;


public  enum PostListType {
    HOME_FEEDS(0),
    MENTION_LIST(1),
    TOPIC_POSTS(2),
    WATCHED_POSTS(3),
    GROUP_POSTS(4),
    PROFILE_POSTS(5),
    SEARCH_POSTS(6);

    int id;

    private PostListType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PostListType fromId(int id) {
        for (PostListType type : PostListType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return HOME_FEEDS;
    }

    //@formatter:off
    public String getEmptyViewTitle(boolean isSelf) {
        String ownTitle, friendTitle;
        switch (this) {
            case HOME_FEEDS:
                ownTitle = friendTitle = Constants.BLANKSTR;
                break;
            case MENTION_LIST:
                ownTitle = I18n.tr("Mentions get attention.");
                friendTitle = I18n.tr("Try mentioning someone, add “@” before the user name.");
                break;
            case WATCHED_POSTS:
                ownTitle = friendTitle = I18n.tr("Love it? Add it.");
                break;
            case PROFILE_POSTS:
                ownTitle = I18n.tr("Share your thoughts with your friends!");
                friendTitle = Constants.BLANKSTR;
                break;
            case TOPIC_POSTS: case GROUP_POSTS: case SEARCH_POSTS:
            default:
                ownTitle = friendTitle = Constants.BLANKSTR;
                break;
        }
        if (isSelf) {
            return ownTitle;
        } else {
            return friendTitle;
        }
    }

    public String getSpannableText(boolean isSelf) {
        String ownSpanText, friendSpanText;
        switch (this) {
            case HOME_FEEDS:
                ownSpanText = friendSpanText = I18n.tr("For starters, add %s.");
                break;
            case MENTION_LIST:
                ownSpanText = friendSpanText = I18n.tr("Try mentioning someone, add “@” before the user name.");
                break;
            case WATCHED_POSTS:
                ownSpanText = friendSpanText = I18n.tr("See posts you love here by making them favorites.");
                break;
            case PROFILE_POSTS:
                ownSpanText = I18n.tr("Make a new post now!");
                friendSpanText = I18n.tr("%s hasn't posted anything.");
                break;
            case TOPIC_POSTS:
            case GROUP_POSTS:
            case SEARCH_POSTS:
            default:
                ownSpanText = Constants.BLANKSTR;
                friendSpanText = Constants.BLANKSTR;
                break;
        }
        if (isSelf) {
            return ownSpanText;
        } else {
            return friendSpanText;
        }
    }
    //@formatter:on
}
