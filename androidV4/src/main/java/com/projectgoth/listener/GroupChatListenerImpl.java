/**
 * Copyright (c) 2013 Project Goth
 *
 * GroupChatListenerImpl.java
 * Created Jul 31, 2013, 6:12:51 PM
 */

package com.projectgoth.listener;

import android.text.TextUtils;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.fusion.packet.FusionPktGroupChatInvite;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.nemesis.listeners.GroupChatListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigRequest;
import com.projectgoth.util.ChatUtils;

/**
 * @author warrenbalcos
 * 
 */
public class GroupChatListenerImpl extends GroupChatListener {

    @Override
    public void onGroupCreated(String id, String owner, ImType type) {
        ChatConversation conv = ChatUtils.createGroupChatConversation(id, null, null);
        ChatDatastore.getInstance().addInfoOrErrorMessage (
                String.format(I18n.tr("%s was created by %s. Have fun!"),
                        conv.getDisplayName(), owner), conv, conv.getId(), true);
    }

    @Override
    public void onUserInvited(String id, String user, ImType type) {
        // NOTE: When a user gets invited and joins the group chat, server will
        // push a Message(500) packet and a GroupChatUserStatus (756) packet
    }

    /**
     * @see com.projectgoth.nemesis.NetworkResponseListener#onError(com.projectgoth.nemesis.model.MigError)
     */
    @Override
    public void onError(MigError error) {
        super.onError(error);        
        
        final MigRequest request = error.getRequest();
        if (request != null && request.getPacket() instanceof FusionPktGroupChatInvite) {
            FusionPktGroupChatInvite fusionPkt = (FusionPktGroupChatInvite) request.getPacket();
            final String id = fusionPkt.getGroupChatId();
            if (!TextUtils.isEmpty(id)) {
                ChatConversation conv = ChatDatastore.getInstance().findOrCreateConversation(ChatTypeEnum.MIG_GROUP, id, MessageType.FUSION);
                if (conv != null) {
                    ChatDatastore.getInstance().addInfoOrErrorMessage (
                            I18n.tr("We couldn't invite your friends to group chat. Try again."),
                            conv, conv.getId(), true);
                }
            } else {
                BroadcastHandler.ChatConversation.GroupChat.sendCreateError(error);
            }
        }
    }
}
