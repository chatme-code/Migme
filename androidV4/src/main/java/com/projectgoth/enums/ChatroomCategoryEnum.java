/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatroomCategoryEnum.java
 * Created Sep 17, 2014, 11:15:38 AM
 */

package com.projectgoth.enums;

/**
 * @author houdangui
 *
 */
public enum ChatroomCategoryEnum {
    FAVORITES(1),
    RECENT(2),
    GAMES(7),
    RECOMMENDED(8),
    FRIEND_FINDER(4),
    GAME_ZONE(5),
    HELP(6);
    
    private int id;
     
    private ChatroomCategoryEnum(int id) {
        this.id = id;
    }
    
    public static ChatroomCategoryEnum fromValue(int id) {
        for (ChatroomCategoryEnum type : values()) {
            if (type.id == id)
                return type;
        }
        return null;
    }
}
