/**
 * Copyright (c) 2013 Project Goth
 *
 * StatusAlert.java
 * Created Aug 23, 2013, 11:42:22 AM
 */

package com.projectgoth.notification;


/**
 * @author cherryv
 * 
 */
public abstract class StatusAlert implements BaseAlert {

    public StatusAlert() {
    }

    public abstract String getTickerMessage();

    public abstract String getTitle();
    
    public abstract String getMessage();

    public abstract int getCount();

    public abstract NotificationType getNotificationType();

    public abstract boolean willTriggerNewEvent();

}
