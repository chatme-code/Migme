/**
 * Copyright (c) 2013 Project Goth
 *
 * NotificationHandler.java
 * Created Aug 23, 2013, 12:36:26 PM
 */

package com.projectgoth.notification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.mig33.diggle.events.Events;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Action;
import com.projectgoth.b.data.Alert;
import com.projectgoth.b.data.MigAlertsUnread;
import com.projectgoth.b.data.Variable;
import com.projectgoth.b.data.VariableLabel;
import com.projectgoth.b.data.ViewURL;
import com.projectgoth.b.enums.AlertDestinationEnum;
import com.projectgoth.b.enums.AlertTypeEnum;
import com.projectgoth.b.enums.ObjectTypeEnum;
import com.projectgoth.b.enums.ViewTypeEnum;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.SharedPrefsManager;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.ui.fragment.SettingsFragment;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.LogUtils;

/**
 * @author cherryv
 * 
 */
public class NotificationHandler extends BroadcastReceiver {
    
    private static final String               LOG_TAG = AndroidLogger.makeLogTag(NotificationHandler.class);

    private ApplicationEx                     appEx;
    private LocalBroadcastManager             mLocalBroadcastManager;
    private ChatStatusAlertHandler            chatAlertsHandler;
    private MigAlertsStatusHandler            migAlertsHandler;
    private AccumulatedFollowersStatusHandler accumulatedFollowersHandler;

    /**
     * Contains a list of conversation IDs with unread messages. We're
     * piggy-backing on the modifications of the StatusAlerts data to modify
     * this data.
     */
    private Set<String>                       unreadConversations                       = new HashSet<String>();
    private final Object                      lock                                      = new Object();

    private int                               unreadMentionCount;

    private final static String               ACCUMULATED_FOLLOWERS_ALERT_VARNAME_COUNT = "count";
    
    private static final int                  DEFAULT_MENTIONS_LOAD_LIMIT = 15;

    private LinkedList<Long> mChatNotificationTimeList = new LinkedList<>();
    private final static int CHAT_NOTIFICATION_VIBRATION_MILSEC = 500;
    private final static int CHAT_NOTIFICATION_VIBRATION_MINIMUM_INTERVAL_MILSECS = 3000;

    private long                              mLastSoundOrVibrateTime;

    public NotificationHandler(ApplicationEx application) {
        this.appEx = application;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(appEx);
        if (this.appEx != null && mLocalBroadcastManager != null) {
            mLocalBroadcastManager.registerReceiver(this, new IntentFilter(Events.ChatMessage.RECEIVED));
            mLocalBroadcastManager.registerReceiver(this, new IntentFilter(Events.MigAlert.FETCH_UNREAD_COMPLETED));
            chatAlertsHandler = new ChatStatusAlertHandler(appEx);
            migAlertsHandler = new MigAlertsStatusHandler(appEx);
            accumulatedFollowersHandler = new AccumulatedFollowersStatusHandler(appEx);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.ChatMessage.RECEIVED)) {
            String conversationId = intent.getStringExtra(Events.ChatConversation.Extra.ID);
            String messageId = intent.getStringExtra(Events.ChatMessage.Extra.ID);
            createChatNotification(context, conversationId, messageId);
            addToUnreadConversations(conversationId, messageId);
        } else if (action.equals(Events.MigAlert.FETCH_UNREAD_COMPLETED)) {
            processMigAlertsUnreadResult();
        }
    }

    private void createChatNotification(Context context, String conversationId, String messageId) {
        Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(),
                "conversationId: ", conversationId, ", messageId: ", messageId);

        if (!TextUtils.isEmpty(conversationId) && !TextUtils.isEmpty(messageId)) {
            if (ChatNotificationStatus.isValidChatNotification(conversationId, messageId)) {
                ChatConversation chatConversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);

                //do not notify user when conversation is mute or coming from chatroom
                if (!chatConversation.isMuted() && chatConversation.getChatType() != ChatTypeEnum.CHATROOM) {

                    ChatNotificationStatus newMessageNotification = new ChatNotificationStatus(conversationId, messageId);
                    chatAlertsHandler.addAlert(newMessageNotification);

                    SharedPreferences mSharedPref = SharedPrefsManager.getGlobalSharedPreference();
                    boolean enableSound = mSharedPref.getBoolean(SettingsFragment.SHARED_PREFS_CHAT_NOTIFICATION_SOUND,
                            SettingsFragment.CHAT_NOTIFICATION_SOUND_DEFAULT);
                    boolean enableVibrate = mSharedPref.getBoolean(SettingsFragment.SHARED_PREFS_CHAT_NOTIFICATION_VIBRATE,
                            SettingsFragment.CHAT_NOTIFICATION_VIBRATE_DEFAULT);

                    if (enableSound || enableVibrate) {
                        long currentSoundOrVibrateTime = System.currentTimeMillis();

                        //not ring or vibrate if received time of two message is too close
                        if (currentSoundOrVibrateTime - mLastSoundOrVibrateTime < CHAT_NOTIFICATION_VIBRATION_MINIMUM_INTERVAL_MILSECS) {
                            return;
                        } else {
                            mLastSoundOrVibrateTime = currentSoundOrVibrateTime;
                        }
                    }

                    if (enableSound) {
                        try {
                            MediaPlayer mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI);

                            if (mediaPlayer != null) {
                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        mp.release();
                                    }
                                });

                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp.start();
                                    }
                                });
                            }
                        } catch (IllegalStateException e) {
                            Logger.error.log(LOG_TAG, "Media player state is incorrect, ignore this call : ", e.getMessage());
                        }
                    }

                    if (enableVibrate) {
                        Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                        vibrator.vibrate(CHAT_NOTIFICATION_VIBRATION_MILSEC);
                    }
                }
            }
        }
    }

    private void createAlertNotification(MigAlertsUnread unreadAlert) {
        if (MigAlertStatus.isValidAlertNotification(unreadAlert)) {
            Alert accumulatedFollowersAlert = null;
            List<Alert> otherUnreadAlerts = new ArrayList<Alert>();
            for (Alert alert : unreadAlert.getAlerts()) {
                if (alert != null) {
                    if (alert.getType() == AlertTypeEnum.ACCUMULATED_NEW_FOLLOWER_ALERT) {
                        accumulatedFollowersAlert = alert;
                    } else {
                        otherUnreadAlerts.add(alert);
                    }
                    AlertsDatastore.getInstance().addInvalidateAlertType(alert.getInvalidateType());
                }
            }
            
            if (accumulatedFollowersAlert != null) {
                AccumulatedFollowersStatus accumulatedFollowersStatus = new AccumulatedFollowersStatus(new Alert[] { accumulatedFollowersAlert});
                accumulatedFollowersHandler.addAlert(accumulatedFollowersStatus);
                
                // Get the number of new followers from the alert.
                final Variable[] varArr = accumulatedFollowersAlert.getVariables();
                if (varArr != null) {
                    for (final Variable var : varArr) {
                        if (var != null && 
                            var.getName() != null && 
                            var.getName().equals(ACCUMULATED_FOLLOWERS_ALERT_VARNAME_COUNT) && 
                            var.getLabel() != null) {
                            final String countStr = var.getLabel().getText();
                            if (!TextUtils.isEmpty(countStr)) {
                                try {
                                    UserDatastore.getInstance().setNewFollowersCount(Integer.parseInt(countStr));
                                    break;
                                } catch (Exception ex) {
                                    Logger.error.log(LOG_TAG, ex);
                                }
                            }
                        }
                    }
                }
            }
            
            if (!otherUnreadAlerts.isEmpty()) {
                Alert[] otherUnreadAlertsArr = new Alert[otherUnreadAlerts.size()];
                otherUnreadAlerts.toArray(otherUnreadAlertsArr);
                MigAlertStatus migAlertStatus = new MigAlertStatus(otherUnreadAlertsArr);
                migAlertsHandler.addAlert(migAlertStatus);
            }
        }
    }

    private void addToUnreadConversations(String conversationId, String messageId) {
        synchronized (lock) {
            if (!TextUtils.isEmpty(conversationId) && !TextUtils.isEmpty(messageId)) {
                if (ChatNotificationStatus.isValidChatNotification(conversationId, messageId)) {
                    Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(),
                            "conversationId: ", conversationId);
                    unreadConversations.add(conversationId);
                    Intent intent = new Intent(AppEvents.Notification.ACTION_BAR_UPDATE_AVAILABLE);
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            }
        }
    }

    private void removeUnreadConversation(String conversationId) {
        synchronized (lock) {
            Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), "conversationId: ", conversationId);
            unreadConversations.remove(conversationId);
            Intent intent = new Intent(AppEvents.Notification.ACTION_BAR_UPDATE_AVAILABLE);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

    public int getUnreadConversationsCount() {
        synchronized (lock) {
            return unreadConversations.size();
        }
    }

    public int getAllUnreadMessagesCount() {
        synchronized (lock) {
            int unReadMessageCounter = 0;
            for (String conversationId: unreadConversations) {
                ChatConversation chatConversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
                if (chatConversation != null) {
                    unReadMessageCounter += chatConversation.getUnreadMessageCounter();
                }
            }
            if (unReadMessageCounter > Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS) {
                return Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS;
            }
            return  unReadMessageCounter;
        }
    }
    
    private void updateUnreadAlertsCount() {
        Intent intent = new Intent(AppEvents.Notification.ACTION_BAR_UPDATE_AVAILABLE);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public void showStatusNotification() {

        chatAlertsHandler.showAlerts();
        migAlertsHandler.showAlerts();
        accumulatedFollowersHandler.showAlerts();
    }

    public void removeNotification(String notificationId) {
        chatAlertsHandler.removeAlert(notificationId);
        migAlertsHandler.removeAlert(notificationId);
        accumulatedFollowersHandler.removeAlert(notificationId);
        removeUnreadConversation(notificationId);
    }

    public void removeAllNotifications(NotificationType type, boolean clearAlerts) {
        switch (type) {
            case CHAT_NOTIFICATION:
                if (clearAlerts) {
                    chatAlertsHandler.removeAllAlerts();
                    synchronized (lock) {
                        unreadConversations.clear();
                        Intent intent = new Intent(AppEvents.Notification.ACTION_BAR_UPDATE_AVAILABLE);
                        mLocalBroadcastManager.sendBroadcast(intent);
                    }
                }
                chatAlertsHandler.dismissAlerts();
                break;
            case MIG_ALERT_NOTIFICATION:
                if (clearAlerts) {
                    migAlertsHandler.removeAllAlerts();
                    updateUnreadAlertsCount();
                }
                migAlertsHandler.dismissAlerts();
            case ACCUMULATED_FOLLOWERS_NOTIFICATION:
                if (clearAlerts) {
                    accumulatedFollowersHandler.removeAllAlerts();
                }
                accumulatedFollowersHandler.dismissAlerts();
                break;
        }
    }

    /*
     * Processes the latest unread mig alerts
     */
    private final void processMigAlertsUnreadResult() {
        List<MigAlertsUnread> unreadAlerts = AlertsDatastore.getInstance().getLastUnreadMigAlerts();
        if (unreadAlerts != null && !unreadAlerts.isEmpty()) {
            for (MigAlertsUnread migAlertsUnread : unreadAlerts) {
                if (migAlertsUnread != null) {
                    int unreadCount = 0;
                    if (migAlertsUnread.getCount() != null) {
                        unreadCount = migAlertsUnread.getCount();
                    }
                    final Integer alertDestination = migAlertsUnread.getDestination();
                    switch (alertDestination) {
                        case AlertDestinationEnum.ACTION_ALERT:
                            if (unreadCount != 0) {
                                updateUnreadAlertsCount();
                            }
                            createAlertNotification(migAlertsUnread);
                            break;
                        case AlertDestinationEnum.POSITIVE_ALERT:
                            handleUnreadPositiveAlerts(migAlertsUnread);
                            break;
                        case AlertDestinationEnum.INCIDENTAL_GROWL_ALERT:
                            break;
                        case AlertDestinationEnum.INTERSTITIAL_ALERT:
                            handleUnreadInterstitialAlerts(migAlertsUnread);
                            break;
                        case AlertDestinationEnum.IM_ALERT:
                            break;
                        case AlertDestinationEnum.MIGALERT:
                            if (unreadCount != 0) {
                                updateUnreadAlertsCount();
                            }
                            break;
                        default:
    
                    }
                }
            }
        }
    }
    
    private void handleUnreadPositiveAlerts(final MigAlertsUnread migAlertsUnread) {
        // Get the last unread positive alert.
        final Alert[] alertsArr = migAlertsUnread.getAlerts();
        final String[] alertIds = new String[alertsArr.length];
        for (int i = 0; i < alertsArr.length; i++) {
            alertIds[i] = alertsArr[i].getId();
        }
        BroadcastHandler.MigAlert.sendUnreadPostiveAlertReceived(alertIds);
    }

    /**
     * Handles interstitial alerts
     * The current requirement is to display an interstitial banner.
     * @param migAlertsUnread The {@link MigAlertsUnread} that contains the alerts to be displayed.
     */
    private void handleUnreadInterstitialAlerts(final MigAlertsUnread migAlertsUnread) {
        // Get the last unread interstitial alert.
        final Alert[] alertsArr = migAlertsUnread.getAlerts();
        if (alertsArr.length > 0) {
            final Alert alert = alertsArr[0];
            if (alert != null) {
                final String message = getMessageFromAlert(alert);
                final String actionUrl = getActionUrlFromAlert(alert);
                BroadcastHandler.MigAlert.sendUnreadInterstitialReceived(message, actionUrl);
            }
        }
    }
    
    /** 
     * Constructs the full alert message by including variables in the alert.
     * @param alert The {@link Alert} whose message is to be extracted.
     * @return The alert message or null if no message was found for the alert.
     */
    private final String getMessageFromAlert(final Alert alert) {
        String message = null;
        
        if (alert != null) {
            message = alert.getMessage();
            if (message != null) {          
                final Variable[] variableArr = alert.getVariables();
                if (variableArr != null) {
                    for (final Variable variable : variableArr) {
                        final String matchStr = "%{" + variable.getName() + "}";                    
                        if (message.indexOf(matchStr) > -1) {
                            final VariableLabel varLabel = variable.getLabel();
                            if (varLabel != null && varLabel.getText() != null) {                           
                                message = message.replace(matchStr, varLabel.getText());
                            }                       
                        }
                    }
                }
            }
        }
        
        return message;
    }
    
    /**
     * Gets the action url for an alert (if present).
     * @param alert The {@link Alert} whose action url is to be extracted.
     * @return  The action url or null if it could not be extracted.
     */
    private final String getActionUrlFromAlert(final Alert alert) {
        String actionUrl = null;
        
        if (alert != null) {
            final Action[] actionArr = alert.getActions();
            
            if (actionArr != null) {
                for (final Action action : actionArr) {
                    if (action != null && action.getType() == ObjectTypeEnum.URL) {
                        final ViewURL[] viewUrlArr = action.getUrl();
                        
                        if (viewUrlArr != null) {
                            for (final ViewURL viewUrl : viewUrlArr) {
                                if (viewUrl != null && viewUrl.getView() == ViewTypeEnum.TOUCH) {
                                    actionUrl = viewUrl.getUrl();                                            
                                }
                            }
                        }
                    }
                }
            }               
        }   
        
        return actionUrl;
    }

    /**
     * @return the unreadMentionCount
     */
    public int getUnreadMentionCount() {
        return unreadMentionCount;
    }
    
    //if we have new mention we fetch it and update the count
    public void updateUnreadMentionCount(int count, boolean shouldFetchMentions) {
        Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), "existing count:", unreadMentionCount, ", new count:", count);
        this.unreadMentionCount = count + unreadMentionCount;
        if (shouldFetchMentions) {
            PostsDatastore.getInstance().getMentionsPostsList(0, DEFAULT_MENTIONS_LOAD_LIMIT, true, false);
        }
        BroadcastHandler.MigAlert.sendUnreadMentionCountReceived(count);
        updateUnreadAlertsCount();
    }
    
    //reset the count only when showing the mention list or switching to a different account
    public void resetUnreadMentionCount() {
        this.unreadMentionCount = 0;
    }
}
