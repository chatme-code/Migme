/**
 * Copyright (c) 2013 Project Goth
 *
 * SendGiftCommandListener.java
 * Created 11 Jun, 2014, 4:49:55 pm
 */

package com.projectgoth.listener;

import com.projectgoth.b.data.StoreItem;
import com.projectgoth.common.TextUtils;
import com.projectgoth.blackhole.fusion.packet.FusionPktMessage;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.enums.UsedChatItemType;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.events.GAEvent;
import com.projectgoth.model.UsedChatItem;
import com.projectgoth.nemesis.model.MigError;

/**
 * @author Dan
 * 
 */
public class SendGiftCommandListener extends SendChatMessageListener {

    private StoreItem mGiftItem;
    private String    mConversationId;

    /**
     * @param activeConversationId
     */
    public SendGiftCommandListener(StoreItem giftItem, String conversationId) {

        mGiftItem = giftItem;
        mConversationId = conversationId;
    }

    public void setConversationId(String conversationId) {
        mConversationId = conversationId;
    }

    @Override
    public void onMessageSent(FusionPktMessage message, Long timestamp) {
        super.onMessageSent(message, timestamp);

        GAEvent.Chat_SendGiftSuccess.send();
        StoreItem giftItem = mGiftItem;

        if (giftItem != null && !TextUtils.isEmpty(giftItem.getGiftHotkey())) {
            UsedChatItem usedChatItem = new UsedChatItem(UsedChatItemType.GIFT, giftItem.getGiftHotkey());
            usedChatItem.setStoreItemId(giftItem.getId().toString());
            // store used gift in chat
            EmoticonDatastore.getInstance().addUsedChatItemToUsedCache(usedChatItem);
        }

        BroadcastHandler.ChatConversation.sendGiftSentOk(mConversationId);
    };

    public void onError(MigError error) {
        super.onError(error);

        GAEvent.Chat_SendGiftFail.send();
        BroadcastHandler.ChatConversation.sendGiftSentError(error, mConversationId);
    }

    @Override
    public void onPhotoSent(FusionPktMessage message) {
    }

    @Override
    public void onSmsSent(FusionPktMessage message) {
    };
}
