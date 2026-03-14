/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatNotificationStatus.java
 * Created Aug 23, 2013, 11:57:57 AM
 */

package com.projectgoth.notification;

import com.projectgoth.controller.ChatController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.util.StringUtils;

/**
 * @author cherryv
 * 
 */
public class ChatNotificationStatus extends StatusAlert {

    private static final int CHATNOTIFICATION_MAX_GROUP_NAME_LENGTH = 15;
    private static final int CHATNOTIFICATION_MAX_SENDER_LENGTH = 10;
    
    private String  conversationId;
    private Message message;

    public static boolean isValidChatNotification(String conversationId, String messageId) {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null && !conversationId.equals(ChatController.getInstance().getActiveConversationId())) {
            //since we have chat sync now, the message received can be an old message for which we don't want to 
            //trigger a notification 
            Message lastMessage = conversation.getMostRecentMessage();
            if (lastMessage != null && lastMessage.getMessageId().equals(messageId)
                    && lastMessage.isIncoming() && !lastMessage.isInfoMessage()) {
                return true;
            }
        }
        return false;
    }

    public ChatNotificationStatus(String conversationId, String messageId) {
        super();
        this.conversationId = conversationId;

        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null) {
            this.message = conversation.getMessage(messageId);
        }
    }

    @Override
    public String getId() {
        return conversationId;
    }

    @Override
    public String getTickerMessage() {
        return constructLastMessage();
    }

    @Override
    public String getTitle() {
        return message.getDisplayName();
    }

    /**
     * Returns the message of the notification.
     * Overrides {@link com.projectgoth.notification.StatusAlert#getMessage}
     * 
     * @return The message content of the notification.
     */
    @Override
    public String getMessage() {
        return message.getMessage();
    }

    @Override
    public int getCount() {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null) {
            return conversation.getUnreadMessageCounter();
        }
        return 1;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.CHAT_NOTIFICATION;
    }

    @Override
    public boolean willTriggerNewEvent() {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null) {
            if (conversation.isChatroom() && !message.hasOwnMention()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the sender of the message notification. If the notification came
     * from a group chat or chatroom, then the sender is the name of the group chat / chatroom
     * 
     * @return The sender of the message.
     */
    public String getSender() {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null && (conversation.isGroupChat() || conversation.isChatroom())) {
            return StringUtils.truncate(conversation.getDisplayName(), CHATNOTIFICATION_MAX_GROUP_NAME_LENGTH, true);
        } else {
            return StringUtils.truncate(message.getDisplayName(), CHATNOTIFICATION_MAX_SENDER_LENGTH, true);
        }
    }

    private String constructLastMessage() {
        String sender = getSender();
        return String.format("%s: %s", sender, message.getMessage());
    }
}
