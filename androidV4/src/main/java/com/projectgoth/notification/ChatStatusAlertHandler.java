/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatStatusAlertHandler.java
 * Created Aug 27, 2013, 2:30:44 PM
 */

package com.projectgoth.notification;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.util.LogUtils;

import java.util.List;

/**
 * @author cherryv
 * 
 */
public class ChatStatusAlertHandler extends StatusAlertHandler<ChatNotificationStatus> {
   
    /**
     * @param appEx
     */
    public ChatStatusAlertHandler(ApplicationEx appEx) {
        super(appEx);
    }
    
    @Override
    public void showAlerts() {
        int notificationCount = getNotificationsCount();
        Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), "count: ", notificationCount);
        if (notificationCount == 0) {
            dismissAlerts();
            return;
        }

        boolean displayAsNewNotification = newNotificationAvailable;

        String tickerMessage = null;
        String conversationId = null;

        // prepare ticker message
        StatusAlert lastNotification = getLatestNotification();
        if (lastNotification != null) {
            tickerMessage = lastNotification.getTickerMessage();
            if (notificationCount == 1) {
                conversationId = lastNotification.getId();
            }
        }
        
        // Construct title and message.
        String title = Constants.BLANKSTR;
        String message = constructNotificationMessage();
        
        if (TextUtils.isEmpty(tickerMessage)) {
            tickerMessage = I18n.tr("You have new messages");
            displayAsNewNotification = false;
        }
        if (TextUtils.isEmpty(title)) {
            title = appEx.getResources().getString(R.string.app_name);
            displayAsNewNotification = false;
        }
        if (TextUtils.isEmpty(message)) {
            message = I18n.tr("New messages available!");
            displayAsNewNotification = false;
        }

        // Make an intent with necessary data in the bundle.
        Bundle bundle = new Bundle();
        if (conversationId != null) {
            final ChatConversation chatConversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);

            if (chatConversation == null) {
                //AD-1338 it's possible that when user manually clicks LEAVE chat which deleted the chat totally when a
                // message of that chat just came, then we cannot find the chat. then reasonable for adding null check here
                return;
            }
    
            bundle.putString(Events.ChatConversation.Extra.CHAT_ID, chatConversation.getChatId());
            bundle.putByte(Events.ChatConversation.Extra.CHAT_TYPE, chatConversation.getChatType().getValue());
            bundle.putByte(Events.ChatConversation.Extra.IM_MESSAGE_TYPE, chatConversation.getImMessageType().getValue());
        }
        
        final Intent notifIntent = makeIntent(AppEvents.Notification.CHAT_SYSTEM_NOTIFICATION, bundle);
        
        createNotification(tickerMessage, title, message, notifIntent, displayAsNewNotification);
    }

    @Override
    protected int getNotificationIcon() {
        return R.drawable.icon_notification_chat;
    }

    /**
     * Constructs the message to be displayed for the latest
     * {@link ChatNotificationStatus}.
     * 
     * @return A String message to be used in the system notification.
     */
    private String constructNotificationMessage() {
        
        String message = String.format(I18n.tr("%d messages"), getTotalNotificationsCount());
        final int noOfMessages = getTotalNotificationsCount();
        // Single message OR Multiple messages?
        if (noOfMessages > 1) {
            final String commonDisplayName = getCommonSenderName();
            if (TextUtils.isEmpty(commonDisplayName)) {
                message = String.format(I18n.tr("%d friends sent %d messages"), getNotificationsCount(), noOfMessages);
            } else {
                message = String.format(I18n.tr("%s sent %d messages"), commonDisplayName, noOfMessages);
            }
        } else {
            final ChatNotificationStatus latestNotif = getLatestNotification();
            if (latestNotif != null) {
                message = String.format(I18n.tr("%s sent a message"), latestNotif.getSender());
            }
        }
        
        return message;
    }
    
    /**
     * Checks whether all the chat notifications are sent by the same sender
     * (username / group chat name / chatroom name) and returns the common
     * sender name.
     * 
     * @return The commmon sender name that is shared among all the chat
     *         notifications and null or empty string if the chat notifications
     *         were sent by two or more different senders.
     */
    private String getCommonSenderName() {
        final List<ChatNotificationStatus> notifs = getAllNotifications();
        final int totalChatNotifs = notifs.size();
        String commonDisplayName = Constants.BLANKSTR;
        if (totalChatNotifs > 0) {
            commonDisplayName = notifs.get(0).getSender();
            if (!TextUtils.isEmpty(commonDisplayName)) {
                for (int i = 1; i < totalChatNotifs; ++i) {
                    final String displayName = notifs.get(i).getSender();
                    if (TextUtils.isEmpty(displayName) || !displayName.equals(commonDisplayName)) {
                        commonDisplayName = Constants.BLANKSTR;
                        break;
                    }
                }
            }
        }
        
        return commonDisplayName;
    }

    /* (non-Javadoc)
     * @see com.projectgoth.notification.StatusAlertHandler#getStatusAlertId()
     */
    @Override
    protected int getStatusAlertId() {
        return Constants.CHAT_STATUS_NOTIFICATION_ID;
    }


}
