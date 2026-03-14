/**
 * Copyright (c) 2013 Project Goth
 *
 * AccumulatedFollowersStatusHandler.java
 * Created May 29, 2014, 11:17:23 AM
 */

package com.projectgoth.notification;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.events.AppEvents;

/**
 * @author angelorohit
 *
 */
public class AccumulatedFollowersStatusHandler extends MigAlertsStatusHandler {

    /**
     * @param appEx
     */
    public AccumulatedFollowersStatusHandler(ApplicationEx appEx) {
        super(appEx);
    }
    
    /* (non-Javadoc)
     * @see com.projectgoth.notification.StatusAlertHandler#getStatusAlertId()
     */
    @Override
    protected int getStatusAlertId() {
        return Constants.ACCUMULATED_FOLLOWERS_STATUS_NOTIFICATION_ID;
    }
    
    @Override
    protected Intent makeIntent(final String intentAction, Bundle extras) {

        final MigAlertStatus migAlertStatus = getLatestNotification(); 
        if (migAlertStatus != null) {
            final String actionUrl = migAlertStatus.getUrlActionForLatestAlert(); 
            if (!TextUtils.isEmpty(actionUrl)) {
                if (extras == null) {
                    extras = new Bundle();
                }
                extras.putString(AppEvents.Notification.Extra.ACTION_URL, actionUrl);
            }
        }
        
        return super.makeIntent(AppEvents.Notification.ACCUMULATED_FOLLOWERS_SYSTEM_NOTIFICATION, extras);
    }

    @Override
    protected int getNotificationIcon() {
        return R.drawable.icon_notification_friend;
    }
}
