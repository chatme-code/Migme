/**
 * Copyright (c) 2013 Project Goth
 *
 * AlertsDatastore.java
 * Created Jun 6, 2013, 12:03:13 PM
 */

package com.projectgoth.datastore;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Alert;
import com.projectgoth.b.data.MigAlerts;
import com.projectgoth.b.data.MigAlertsUnread;
import com.projectgoth.b.data.MigAlertsUnreadResult;
import com.projectgoth.b.enums.AlertTypeEnum;
import com.projectgoth.common.Logger;
import com.projectgoth.dao.AlertsDAO;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.enums.AlertsFilterTypeEnum;
import com.projectgoth.nemesis.listeners.AlertActionListener;
import com.projectgoth.nemesis.listeners.GetMigAlertsListener;
import com.projectgoth.nemesis.listeners.GetUnreadMentionCountListener;
import com.projectgoth.nemesis.listeners.GetUnreadMigAlertsListener;
import com.projectgoth.nemesis.listeners.SimpleResponseListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigResponse;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.scheduler.JobScheduler.ScheduleListener;
import com.projectgoth.util.scheduler.ScheduledJobsHandler;
import com.projectgoth.util.scheduler.ScheduledJobsHandler.ScheduledJobKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages caching and persistent storage of all data related to mig alerts.
 * 
 * @author angelorohit
 */
public class AlertsDatastore extends BaseDatastore {

    private static final String LOG_TAG = AndroidLogger.makeLogTag(AlertsDatastore.class);

    // A lock that is obtained when working with any of the caches.
    private static final Object sCacheLock = new Object();

    private static final int ALERT_CACHE_EXPIRY = 3 * 60 * 1000;

    // A DAO for saving alerts to persistent storage.
    private AlertsDAO mAlertsDAO = null;

    // A cache of all alerts. The key for this cache is the id of the alert.
    private Map<String, Alert> mAlertsCache;

    // A cache of the unread MigAlerts.
    private MigAlertsUnreadResult mMigAlertsUnreadResult;

    private long mLastAlertsFetchTimestamp;

    // A set containing the integer values of AlertTypeEnum whose accumulated
    // values are to be reset.
    private Set<Integer> resetAccumulatedAlertTypeSet = new HashSet<Integer>();

    // The max number of mig alerts to be fetched at a time.
    public final static int DEFAULT_MIGALERTS_FETCH_LIMIT = 15;

    private final static long UNREAD_ALERTS_POLL_INTERVAL = 60 * 1000;

    private AlertsDatastore() {
        super();

        final Context appCtx = ApplicationEx.getContext();
        if (appCtx != null) {
            mAlertsDAO = new AlertsDAO(appCtx);
        }

        loadFromPersistentStorage();
    }

    private static class AlertsDatastoreHolder {
        static final AlertsDatastore sINSTANCE = new AlertsDatastore();
    }

    /**
     * A singleton point of access for this class.
     * 
     * @return An instance of AlertsDatastore.
     */
    public static AlertsDatastore getInstance() {
        return AlertsDatastoreHolder.sINSTANCE;
    }
    
    @Override
    protected void initData() {
        synchronized (sCacheLock) {
            mAlertsCache = new HashMap<String, Alert>();
            mMigAlertsUnreadResult = null;
            mLastAlertsFetchTimestamp = 0;
            
            ScheduledJobsHandler.getInstance().stopJobWitKey(ScheduledJobKeys.GET_UNREAD_ALERTS);
        }        
    }
    
    @Override
    public void clearData() {
        super.clearData();
        
        if (mAlertsDAO != null) {
            mAlertsDAO.clearTables();
        }
    }

    /**
     * Gets an alert with the given id from cache
     * 
     * @param alertId
     *            The id of the alert to be retrieved.
     * @return The associated alert on success and null if the alert could not
     *         be found in the cache.
     */
    public Alert getAlertWithId(final String alertId) {
            Alert alert = mAlertsCache.get(alertId);
            return alert;
    }

    /**
     * Gets all alerts in the cache. If the cache is empty, a request is sent to
     * fetch the mig alerts from server.
     * 
     * @return A list containing all alerts in the cache.
     */
    public List<Alert> getAllAlerts(final boolean shouldForceFetch) {

        List<Alert> resultList = null;
        //mAlertsCache may be changed in setAlerts which is called by another thread
        //add synchronized to prevent mAlertsCache changed when generating Collection
        synchronized (sCacheLock) {
            resultList = new ArrayList<Alert>(mAlertsCache.values());
        }

        if (!resultList.isEmpty()) {
            Collections.sort(resultList, new Comparator<Alert>() {

                @Override
                public int compare(final Alert lhs, final Alert rhs) {
                    final long lhsTimestamp = lhs.getTimestamp();
                    final long rhsTimestamp = rhs.getTimestamp();

                    if (rhsTimestamp == lhsTimestamp) {
                        return 0;
                    } else {
                        return (rhsTimestamp < lhsTimestamp) ? -1 : 1;
                    }
                }
            });
        }

        if ((System.currentTimeMillis() > mLastAlertsFetchTimestamp + ALERT_CACHE_EXPIRY) ||
                shouldForceFetch) {
            mLastAlertsFetchTimestamp = System.currentTimeMillis();
            requestGetMigAlerts(null, 0, DEFAULT_MIGALERTS_FETCH_LIMIT, AlertsFilterTypeEnum.NONE);
        }

        return resultList;
    }

    /**
     * Returns the last unread migalerts.
     * 
     * @return A List containing {@link MigAlertsUnread} or null if the unread
     *         migalerts were never fetched.
     */
    public List<MigAlertsUnread> getLastUnreadMigAlerts() {
            if (mMigAlertsUnreadResult != null) {
                return new ArrayList<MigAlertsUnread>(Arrays.asList(mMigAlertsUnreadResult.getAlerts()));
            }

            return null;
    }
    
    /**
     * Returns an unread mig alert matching the given id and alert destination
     * type.
     * 
     * @param alertId
     *            The id of the alert to be matched.
     * @param alertDestination
     *            The AlertDestinationType enum to be matched.
     * @return The Alert if it was found and null if an appropriate match was
     *         not found.
     */
    public Alert getUnreadMigAlertWithIdAndDestination(final String alertId, final int alertDestination) {
        if (!TextUtils.isEmpty(alertId) && mMigAlertsUnreadResult != null
                && mMigAlertsUnreadResult.getAlerts() != null) {
            final MigAlertsUnread[] unreadMigAlertsArr = mMigAlertsUnreadResult.getAlerts();
            if (unreadMigAlertsArr.length > 0) {
                for (final MigAlertsUnread migAlertsUnread : unreadMigAlertsArr) {
                    if (migAlertsUnread != null && migAlertsUnread.getDestination() != null
                            && migAlertsUnread.getDestination() == alertDestination
                            && migAlertsUnread.getAlerts() != null && migAlertsUnread.getAlerts().length > 0) {

                        for (final Alert alert : migAlertsUnread.getAlerts()) {
                            if (alert != null && alert.getId() != null && alert.getId().equals(alertId)) {
                                return alert;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Sets the last unread mig alerts. NOTE: This routine is to be used
     * internally only.
     * 
     * @param migAlertsUnreadResult
     *            The MigAlertsUnreadResult to be set.
     */
    private void setLastUnreadMigAlertsResult(final MigAlertsUnreadResult migAlertsUnreadResult) {
        synchronized (sCacheLock) {
            mMigAlertsUnreadResult = migAlertsUnreadResult;
        }
    }

    /**
     * Gets the last fetch time stamp from the last unread mig alerts result.
     * 
     * @return The last fetch time stamp or zero if the unread mig alerts were
     *         not fetched.
     */
    private long getLastUnreadMigAlertsTimestamp() {
        long timestamp = 0;
        if (mMigAlertsUnreadResult != null && mMigAlertsUnreadResult.getTimestamp() != null) {
            timestamp = mMigAlertsUnreadResult.getTimestamp();
        }

        return timestamp;
    }

    /**
     * Sets a list of alerts to the cache.
     * 
     * @param alertsList
     *            A list containing the alerts to be cached.
     * @param shouldPersist
     *            Indicates whether the cached alerts should immediately be
     *            persisted to data storage.
     */
    public void setAlerts(final List<Alert> alertsList, final boolean shouldPersist) {
        synchronized (sCacheLock) {
            if (alertsList != null) {
                mAlertsCache.clear();
                for (Alert alert : alertsList) {
                    mAlertsCache.put(alert.getId(), alert);
                }
    
                if (shouldPersist) {
                    if (!saveAlertsToPersistentStorage(alertsList)) {
                        Logger.error.log(LOG_TAG, "Failed to persist with saveAlertsToPersistentStorage");
                    }
                }
            }
        }
    }

    /**
     * Sends a request to fetch MigAlerts from the server.
     * 
     * @param userId
     *            The id of the user whose alerts are to be fetched. Can be
     *            null.
     * @param offset
     *            The paged offset from which the alerts are to be fetched.
     * @param limit
     *            The limit imposed on the number of alerts to be fetched.
     * @param alertsFilterType
     *            A filter to be set on the types of alerts to be fetched. If
     *            this value is null, no filter is set.
     */
    public void requestGetMigAlerts(String userId, final int offset, final int limit,
            final AlertsFilterTypeEnum alertsFilterType) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetMigAlerts(new GetMigAlertsListener() {

                    @Override
                    public void onMigAlertsReceived(MigAlerts migAlerts) {
                        if (migAlerts != null && migAlerts.getAlerts() != null) {
                            setAlerts(Arrays.asList(migAlerts.getAlerts()), true);
                        }

                        checkAndResetAlertType(AlertTypeEnum.VIRTUALGIFT_ALERT);
                        BroadcastHandler.MigAlert.sendFetchAllCompleted(offset, limit);

                        // Inform that the action bar notification counter should be updated
                        Intent intent = new Intent(AppEvents.Notification.FETCH_ALERTS_FROM_SERVER_DONE);
                        LocalBroadcastManager.getInstance(appEx).sendBroadcast(intent);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.MigAlert.sendFetchAllError(error, offset, limit);
                    }

                }, userId, offset, limit, alertsFilterType);

                BroadcastHandler.MigAlert.sendBeginFetchMigAlerts(offset, limit);
            }
        }
    }
    
    public void requestSendAlertAction(String actionUrl, String contentType, String httpMethod) {
        Logger.debug.log("requestSendAlertAction", "actionUrl" +actionUrl);
        Logger.debug.log("requestSendAlertAction", "contentType" +contentType);
        Logger.debug.log("requestSendAlertAction", "httpMethod" +httpMethod);

        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendAlertAction(new AlertActionListener() {

                    @Override
                    public void onSuccess(String message) {
                        BroadcastHandler.MigAlert.sendActionSuccess(message);
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see
                     * com.projectgoth.nemesis.NetworkResponseListener#onError
                     * (com.projectgoth.nemesis.model.MigError)
                     */
                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.MigAlert.sendActionError(error);
                    }

                }, actionUrl, contentType, httpMethod);
            }
        }
    }

    /**
     * Sends a request to fetch unread migalerts from the server.
     * 
     * @param userId
     *            The id of the user whose unread alerts are to be fetched.
     * @param timestamp
     *            The timestamp when the alerts were last fetched.
     * @param limit
     *            The limit imposed on the number of alerts for each alert
     *            destination type.
     */
    public void requestGetUnreadMigAlerts(String userId, final int limit) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                // Find the last fetch timestamp from the last unread migalerts
                // result.
                long lastFetchTimestamp = getLastUnreadMigAlertsTimestamp();

                requestManager.sendGetUnreadMigAlerts(new GetUnreadMigAlertsListener() {

                    @Override
                    public void onUnreadMigAlertsReceived(MigAlertsUnreadResult migAlertsUnreadResult) {
                        setLastUnreadMigAlertsResult(migAlertsUnreadResult);
                        BroadcastHandler.MigAlert.sendFetchUnreadCompleted();

                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.MigAlert.sendFetchUnreadError(error);
                    }

                }, userId, lastFetchTimestamp, limit);
            }
        }
    }
    
    /**
     * Sends a request to reset an accumulated alert count.
     * 
     * @param userId
     *            The id of the user whose accumulated alerts are to be reset.
     * @param alertType
     *            The invalidate alert type. @see
     *            {@link Alert#getInvalidateType}
     */
    public void requestResetAccumulatedAlert(String userId, final AlertTypeEnum alertType) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendResetAccumulatedAlert(new SimpleResponseListener() {

                    @Override
                    public void onSuccess(MigResponse response) {
                        // Don't care.
                    }
                }, userId, alertType);
            }
        }
    }

    /**
     * Creates and starts the poll scheduler for unread alerts.
     */
    public void startUnreadAlertsPollSchedule() {
        // Call once and then schedule the next poll attempt
        requestGetUnreadMigAlerts(Session.getInstance().getUserId(), DEFAULT_MIGALERTS_FETCH_LIMIT);

        ScheduledJobsHandler.getInstance().startJobWithKey(ScheduledJobKeys.GET_UNREAD_ALERTS, new ScheduleListener() {

            @Override
            public void processJob() {
                requestGetUnreadMigAlerts(Session.getInstance().getUserId(), DEFAULT_MIGALERTS_FETCH_LIMIT);
            }
        }, UNREAD_ALERTS_POLL_INTERVAL, true);
    }

    public void requestGetUnreadMentionCount(String userId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetUnreadMentionCount(new GetUnreadMentionCountListener() {

                    @Override
                    public void onUnreadMentionCountReceived(int unreadMentionCount) {
                        ApplicationEx.getInstance().getNotificationHandler().updateUnreadMentionCount(unreadMentionCount, true);
                    }}, userId);
            }
        }
    }
    
    /**
     * Loads all alerts from persistent storage into cache.
     */
    private void loadFromPersistentStorage() {
        if (mAlertsDAO != null) {
            final List<Alert> alertList = mAlertsDAO.loadAlertsFromDatabase();
            if (alertList != null) {
                setAlerts(alertList, false);
            }
        }
    }

    /**
     * Saves a List of Alert to persistent storage. Inserts new alerts or
     * updates as necessary (matched by id) This routine is internally called
     * when new alerts are added to the cache.
     * 
     * @param alertList
     *            A List containing the alerts to be persisted.
     * @return true on success and false otherwise.
     */
    private boolean saveAlertsToPersistentStorage(final List<Alert> alertList) {
        if (mAlertsDAO != null) {
            mAlertsDAO.clearTables();
            return mAlertsDAO.saveAlertsToDatabase(alertList);
        }

        return false;
    }
    
    public void addInvalidateAlertType(final AlertTypeEnum alertType) {
        if (alertType != null) {
            synchronized (sCacheLock) {
                resetAccumulatedAlertTypeSet.add(alertType.value());
            }
        }
    }
    
    public void checkAndResetAlertType(final AlertTypeEnum alertType) {
        if (alertType != null && resetAccumulatedAlertTypeSet.contains(alertType.value())) {
            synchronized (sCacheLock) {
                resetAccumulatedAlertTypeSet.remove(alertType.value());
            }
            
            requestResetAccumulatedAlert(Session.getInstance().getUserId(), alertType);
        }
    }
    
    public int getUnreadNotificationCount(boolean shouldForceFetch) {
        List<Alert> alertList = getAllAlerts(shouldForceFetch);
        int unreadCount = 0;
        
        for (Alert alert : alertList) {
            if (!checkWhetherAlertIsRead(alert)) {
                ++unreadCount;
            }
        }
        
        return unreadCount;
    }

    /**
     * This method is used for checking a certain alert whether it's already read or not
     *
     * @param  alert  The alert to be checked whether it's already read or not
     * @return        true if the alert is already read
     *                false if the alert is not read
     * @throws NullPointerException If input parameter alert is null
     */
    public boolean checkWhetherAlertIsRead(@NonNull Alert alert) {
        if (alert == null) {
            throw new NullPointerException();
        } else {
            return SystemDatastore.getInstance().getAlertReadData(alert.getId());
        }
    }

    /**
     * This method is used for setting the read status of a certain alert
     *
     * @param  alert   The alert to be set whether it's read or not
     * @param  isRead  true if the alert is going to be set as read
     *                 false if the alert is going to be set as still not read
     */
    public void setAlertReadStatus(@NonNull final Alert alert, boolean isRead) {
        if (alert == null) {
            return;
        } else {
            SystemDatastore.getInstance().saveAlertReadData(alert.getId(), isRead);
        }
    }

}
