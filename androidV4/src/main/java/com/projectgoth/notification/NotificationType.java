/**
 * Copyright (c) 2013 Project Goth
 *
 * NotificationType.java
 * Created Aug 28, 2013, 2:41:00 PM
 */

package com.projectgoth.notification;


/**
 * @author cherryv
 * 
 */
public enum NotificationType {

    CHAT_NOTIFICATION(0), MIG_ALERT_NOTIFICATION(1), ACCUMULATED_FOLLOWERS_NOTIFICATION(2);

    private int value;

    NotificationType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static NotificationType fromValue(int value) {
        for (NotificationType e : NotificationType.values()) {
            if (e.value() == value) {
                return e;
            }
        }
        return CHAT_NOTIFICATION;
    }

}
