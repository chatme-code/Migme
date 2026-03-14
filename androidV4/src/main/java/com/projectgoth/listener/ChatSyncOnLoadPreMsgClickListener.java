/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatSyncOnLoadPreMsgClickListener.java
 * Created 10 Jun, 2014, 3:58:53 pm
 */

package com.projectgoth.listener;

import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.model.ChatConversation;


/**
 * @author Dan
 *
 */
public class ChatSyncOnLoadPreMsgClickListener extends ChatSyncListenerImpl {

    /**
     *  the message id of the message right after the 'load previous message' button.
     */
    String msgId;
    
    String conversationId;
    
    public ChatSyncOnLoadPreMsgClickListener(String msgId, String conversationId) {
        this.msgId = msgId;
        this.conversationId = conversationId;
    }

    @Override
    public void onEndMessagesReceived(String chatId, String firstMsgGuid, String lastMsgGuid, int messageSent) {
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        conv.resetLoadingStateOfMessage(msgId);
        
        BroadcastHandler.ChatMessage.sendFetchCompleted(chatId, true);
    }
    
}
