/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatSyncListenerImpl.java
 * Created 10 Jun, 2014, 3:25:41 pm
 */

package com.projectgoth.listener;

import com.projectgoth.common.Logger;
import com.projectgoth.controller.ChatController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.listeners.ChatSyncListener;
import com.projectgoth.nemesis.model.ChatData;
import com.projectgoth.nemesis.model.MigResponse;

/**
 * default chat sync listener
 */
public class ChatSyncListenerImpl extends ChatSyncListener {

    @Override
    public void onChatDataReceived(ChatData data) {
        ChatDatastore.getInstance().addChatData(data);
    }

    @Override
    public void onChatListVersionReceived(int version, long timestamp) {
        ChatDatastore.getInstance().setChatListVersion(version);
        ChatDatastore.getInstance().setChatListTimestamp(timestamp);
    }
    
    @Override
    public void onChatMessageResponse(MigResponse response) {
        Logger.debug.log("Packet", "MESSAGE in ChatSyncListener");
        ChatController.getInstance().processChatMessageServerResponse(response, false);
    }

    @Override
    public void onEndMessagesReceived(String chatId, String firstMsgGuid, String lastMsgGuid, int messageSent) {

        BroadcastHandler.ChatMessage.sendFetchCompleted(chatId);
    }

}
