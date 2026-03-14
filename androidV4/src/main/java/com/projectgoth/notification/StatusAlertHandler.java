/**
 * Copyright (c) 2013 Project Goth
 *
 * StatusAlertHandler.java
 * Created Aug 25, 2013, 10:27:39 PM
 */

package com.projectgoth.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.events.AppEvents;
import com.projectgoth.ui.activity.MainDrawerLayoutActivity;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.LogUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author cherryv
 * 
 */
public abstract class StatusAlertHandler<T extends StatusAlert> extends Thread implements BaseAlertHandler {

    private static final String LOG_TAG                 = AndroidLogger.makeLogTag(StatusAlertHandler.class);
    
    private final Object      lock                      = new Object();
    private final Object      threadLock                = new Object();
    protected ApplicationEx   appEx;
    private boolean           isRunning;
    private long              lastNotificationBroadcast;

    // The notifications is LinkedHashMap instead of a regular HashMap so that
    // we can iterate through the notifications in order.
    private Map<String, T>    notifications             = new LinkedHashMap<String, T>();

    /** ID of the latest chat notification added in the map. */
    private String            lastNotificationKey;
    /** Unique ID to be used to display this alert in the notification manager */
    private final int         statusAlertId;

    protected boolean         newNotificationAvailable;

    /**
     * The time to wait between successive status alert notifications.
     */
    private static final long THREAD_NOTIFICATION_DELAY = 100L;

    protected abstract int getStatusAlertId();
    
    public StatusAlertHandler(ApplicationEx appEx) {
        super();
        this.appEx = appEx;
        this.statusAlertId = getStatusAlertId();

        startThread();
    }

    public void startThread() {
        if (!isRunning) {
            Logger.debug.log(LogUtils.TAG_NOTIFICATION, getClass().getSimpleName()
                    + ": Starting Application Notification Thread: " + notifications.size() + ", "
                    + newNotificationAvailable);
            isRunning = true;
            this.start();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addAlert(BaseAlert alert) {
        T newAlert = (T) alert;
        addNotification(newAlert);
    }

    protected void addNotification(T newNotification) {
        synchronized (lock) {
            // remove duplicates
            if (notifications.containsKey(newNotification.getId())) {
                notifications.remove(newNotification.getId());
            }
            notifications.put(newNotification.getId(), newNotification);

            if (newNotification.willTriggerNewEvent()) {
                lastNotificationKey = newNotification.getId();
                newNotificationAvailable = true;
            }

            Logger.debug.log(LogUtils.TAG_NOTIFICATION, getClass().getSimpleName(), ": New notification added: ",
                    newNotification.getId(), ", size ", notifications.size(), "/", newNotificationAvailable);
        }

        if (newNotificationAvailable) {
            synchronized (threadLock) {
                try {
                    threadLock.notify();
                } catch (Exception e) {
                    Logger.error.log(LOG_TAG, "Error notifying lock upon adding new notification");
                }
            }
        }
    }

    @Override
    public void removeAlert(String id) {
        synchronized (lock) {
            notifications.remove(id);

            if (id.equals(lastNotificationKey)) {
                lastNotificationKey = null;
                newNotificationAvailable = false;
            }

            Logger.debug.log(LogUtils.TAG_NOTIFICATION, getClass().getSimpleName(),
                    ": REMOVED notification with ID: ", id,
                    " Remaining messages: ", notifications.size());

        }
    }

    @Override
    public void removeAllAlerts() {
        synchronized (lock) {
            Logger.debug.log(LogUtils.TAG_NOTIFICATION, getClass().getSimpleName(),
                    ".removeAllAlerts: current count: ", notifications.size());
            notifications.clear();
            newNotificationAvailable = false;
        }
    }

    @Override
    public void dismissAlerts() {
        NotificationManager notificationManager = (NotificationManager) appEx
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(statusAlertId);
        newNotificationAvailable = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            // delay broadcast to see if there are other notifications
            // that will be added
            long currentTime = System.currentTimeMillis();
            Logger.debug.log(LogUtils.TAG_NOTIFICATION, getClass().getSimpleName(), ".running thread...",
                    newNotificationAvailable, "/", (currentTime - lastNotificationBroadcast));

            if (newNotificationAvailable && ((currentTime - lastNotificationBroadcast) > 1000)) {
                // get the type and id of the last notification
                T notification = getLatestNotification();
                if (notification != null) {
                    Intent intent = new Intent(AppEvents.Notification.UPDATE_AVAILABLE);
                    intent.putExtra(AppEvents.Notification.Extra.TYPE, notification.getNotificationType().value());
                    intent.putExtra(AppEvents.Notification.Extra.ID, lastNotificationKey);
                    LocalBroadcastManager.getInstance(appEx).sendBroadcast(intent);

                    Logger.debug.log(LogUtils.TAG_NOTIFICATION, "Sending EVENT_STATUS_NOTIFICATION_AVAILABLE event for: ",
                            notification.getNotificationType().value(), ", lastId: ", lastNotificationKey);

                    lastNotificationBroadcast = currentTime;
                }
            }

            try {
                if (newNotificationAvailable) {
                    // while the notification is still not processed or
                    // broadcasted, just delay the thread
                    Thread.sleep(THREAD_NOTIFICATION_DELAY);
                }

                synchronized (threadLock) {
                    // we make the thread sleep while there are no events
                    // that needs to be broadcasted. It will be notified
                    // whenever a new alert is added
                    if (!newNotificationAvailable) {
                        threadLock.wait();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper method to access the most recently added notification in the
     * {@link #notifications} map.
     * 
     * @return Most recently added notification in the map. Can return null if
     *         the most recent one was already removed from the map and no new
     *         notifications were received since.
     */
    protected T getLatestNotification() {
        synchronized (lock) {
            if (!TextUtils.isEmpty(lastNotificationKey)) {
                return notifications.get(lastNotificationKey);
            }
        }

        Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), "NOT FOUND");
        return null;
    }

    /**
     * Helper method to access the latest N notifications.
     * 
     * @param count
     *            The number of latest notifications to be retrieved. If count
     *            is negative, all the notifications are retrieved.
     * @return A List containing the latest N notifications. If there are fewer
     *         notifications than the count specified, then the list will
     *         contain those notifications.
     */
    protected List<T> getLatestNotification(final int count) {
        synchronized (lock) {
            final List<T> result = new ArrayList<T>();
            // Iterate through the LinkedHashMap
            final int size = notifications.size();
            if (size > 0 && count != 0) {
                // Iterate to the point where we can start adding the latest notifications into the result list.
                Iterator<Entry<String, T>> iter = notifications.entrySet().iterator();
                
                if (count > 0) {
                    for (int i = 0; i < notifications.size() - count; ++i) {
                        iter.next();
                    }
                }

                while (iter.hasNext()) {
                    result.add(iter.next().getValue());
                }
            }

            return result;
        }
    }
    
    /**
     * Creates an intent on which an alert will be broadcast.
     * 
     * @param intentAction
     *            The action of the intent.
     * @param extras
     *            A {@link android.os.Bundle} that will contain extra arguments.
     *            Can be null.
     * @return The intent that can be used to broadcast the alert.
     */
    protected Intent makeIntent(final String intentAction, Bundle extras) {
        Intent notifIntent = new Intent(appEx, MainDrawerLayoutActivity.class);
        notifIntent.setAction(intentAction);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        if (extras != null) {
            notifIntent.putExtras(extras);
        }
        
        return notifIntent;
    }
    
    /**
     * Helper method to access all the notifications.
     * @return A List containing all the notifications.
     */
    protected List<T> getAllNotifications() {
        return getLatestNotification(-1);
    }

    /**
     * Helper method to get the number of notifications.
     * @return The number of notifications.
     */
    protected int getNotificationsCount() {
        synchronized (lock) {
            return notifications.size();
        }
    }

    protected int getTotalNotificationsCount() {
        synchronized (lock) {
            int totalNotifications = 0;
            for (T value : notifications.values()) {
                totalNotifications += value.getCount();
            }

            Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), totalNotifications);
            return totalNotifications;
        }
    }

    protected abstract int getNotificationIcon();

    protected void createNotification(String tickerMessage, String title, String message,
                                      Intent notifIntent, boolean displayAsNewNotification) {

        NotificationManager notificationManager = (NotificationManager) appEx
                .getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationCount = getNotificationsCount();
        Logger.debug.log(LogUtils.TAG_NOTIFICATION, "createNotification: count: ", notificationCount,
                ", displayAsNew: ", displayAsNewNotification);

        int notificationIcon = getNotificationIcon();

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(appEx);
        notifBuilder.setTicker(tickerMessage);
        notifBuilder.setContentTitle(title);
        notifBuilder.setContentText(message);
        notifBuilder.setSmallIcon(notificationIcon);
        notifBuilder.setNumber(getTotalNotificationsCount());
        notifBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        notifBuilder.setAutoCancel(true);
        notifBuilder.setOnlyAlertOnce(true);
        
        if (notifIntent != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(appEx, 0, notifIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            notifBuilder.setContentIntent(pendingIntent);
        }

        // play sound and vibrate when this is a new notification
        // otherwise, just update the message
        if (displayAsNewNotification) {
            // need to cancel previous notifications to be triggered as new
            // notification. otherwise, it will just update the message
            notificationManager.cancel(statusAlertId);
        }
        
        try {
            Notification notification = notifBuilder.build();
            notification.contentView.setImageViewResource(android.R.id.icon, notificationIcon);
            notificationManager.notify(statusAlertId, notification);
        }
        catch (Exception ex) {
            Logger.error.log(LogUtils.TAG_NOTIFICATION, ex);
        }
        newNotificationAvailable = false;
    }
}
