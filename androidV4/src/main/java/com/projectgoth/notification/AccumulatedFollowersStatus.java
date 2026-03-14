/**
 * Copyright (c) 2013 Project Goth
 *
 * AccumulatedFollowersStatus.java
 * Created May 29, 2014, 1:57:00 PM
 */

package com.projectgoth.notification;

import com.projectgoth.b.data.Alert;


/**
 * @author angelorohit
 *
 */
public class AccumulatedFollowersStatus extends MigAlertStatus {

    /**
     * @param alertArr
     */
    public AccumulatedFollowersStatus(Alert[] alertArr) {
        super(alertArr);
    }
    

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.ACCUMULATED_FOLLOWERS_NOTIFICATION;
    }
}