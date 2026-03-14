/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatSyncOnChatShownListener.java
 * Created 10 Jun, 2014, 3:33:09 pm
 */

package com.projectgoth.listener;

import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.model.ChatConversation;


/**
 * @author Dan
 */
public class ChatSyncOnChatShownListener extends ChatSyncListenerImpl {
    
    String conversationId;
    
    public ChatSyncOnChatShownListener(String conversationId) {
        this.conversationId = conversationId;
    }
    
    
    @Override
    public void onEndMessagesReceived(String chatId, String firstMsgGuid, String lastMsgGuid, int messageSent) {
        super.onEndMessagesReceived(chatId, firstMsgGuid, lastMsgGuid, messageSent);
        
        ChatDatastore.getInstance().setConversationAsSynced(conversationId);
        
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conv != null) {
            conv.setSyncedOnShow(true);
        }
        BroadcastHandler.ChatMessage.sendFetchCompleted(chatId);
    }
}
