/**
 * Copyright (c) 2013 Project Goth
 *
 * MigAlertsStatusHandler.java
 * Created Aug 27, 2013, 2:39:11 PM
 */

package com.projectgoth.notification;

import android.content.Intent;
import android.text.TextUtils;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.events.AppEvents;
import com.projectgoth.i18n.I18n;
import com.projectgoth.util.LogUtils;
import com.projectgoth.util.StringUtils;

import java.util.List;
import java.util.ListIterator;


/**
 * @author cherryv
 *
 */
public class MigAlertsStatusHandler extends StatusAlertHandler<MigAlertStatus> {

    /**
     * The maximum number of notifications that will be shown in the system
     * notification at one time.
     */
    private static final int MIGALERTNOTIFICATION_MAX_LATEST_TO_VIEW = 2;
    
    /**
     * The maximum length of the chat message in the system notification. 
     */
    private static final int MIGALERTNOTIFICATION_MAX_MESSAGE_LENGTH = 35;
    
    /**
     * @param appEx
     */
    public MigAlertsStatusHandler(ApplicationEx appEx) {
        super(appEx);
    }

    @Override
    public void showAlerts() {
        int notificationCount = getNotificationsCount();
        Logger.debug.log(LogUtils.TAG_NOTIFICATION, "showMigAlertsNotification: notif count: " + notificationCount);
        if (notificationCount == 0) {
            dismissAlerts();
            return;
        }

        boolean displayAsNewNotification = newNotificationAvailable;

        String tickerMessage = null;
        String title = null;
        String message = null;
        final Intent notifIntent = makeIntent(AppEvents.Notification.MIGALERT_SYSTEM_NOTIFICATION, null);

        // prepare ticker message
        StatusAlert lastNotification = getLatestNotification();
        if (lastNotification != null) {
            tickerMessage = lastNotification.getTickerMessage();
        }
        
        // Construct title and message.
        title = Constants.BLANKSTR;
        message = constructNotificationMessage();

        if (TextUtils.isEmpty(tickerMessage)) {
            tickerMessage = I18n.tr("New notifications");
            displayAsNewNotification = false;
        }
        if (TextUtils.isEmpty(title)) {
            title = appEx.getResources().getString(R.string.app_name);
            displayAsNewNotification = false;
        }
        if (TextUtils.isEmpty(message)) {
            message = I18n.tr("New notifications");
            displayAsNewNotification = false;
        }
        
        createNotification(tickerMessage, title, message, notifIntent, displayAsNewNotification);
    }
    
    /**
     * Constructs the message to be displayed for the latest
     * {@link MigAlertStatus}.
     * 
     * @return A String message to be used in the system notification.
     */
    private String constructNotificationMessage() {
        StringBuffer buf = new StringBuffer();
        final List<MigAlertStatus> latestNotifs = getLatestNotification(MIGALERTNOTIFICATION_MAX_LATEST_TO_VIEW);
        
        // The notifications should be shown latest first. Hence, we iterate from last to first.
        ListIterator<MigAlertStatus> iter = latestNotifs.listIterator(latestNotifs.size());
        
        while (iter.hasPrevious()) {
            final MigAlertStatus status = iter.previous();
            buf.append(StringUtils.truncate(status.getMessage(), MIGALERTNOTIFICATION_MAX_MESSAGE_LENGTH, true));

            // Append a newline at the end if this is not the last
            // notification.
            if (status != latestNotifs.get(0)) {
                buf.append(Constants.NEWLINE);
            }
        }
        return buf.toString();
    }

    /* (non-Javadoc)
     * @see com.projectgoth.notification.StatusAlertHandler#getStatusAlertId()
     */
    @Override
    protected int getStatusAlertId() {
        return Constants.MIG_ALERTS_STATUS_NOTIFICATION_ID;
    }

    @Override
    protected int getNotificationIcon() {
        return R.drawable.icon_application;
    }
}
