package com.projectgoth.listener;

import android.text.TextUtils;
import com.projectgoth.blackhole.enums.PacketType;
import com.projectgoth.blackhole.enums.PinnedType;
import com.projectgoth.blackhole.fusion.packet.FusionPktMessage;
import com.projectgoth.controller.ChatController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.model.Message;
import com.projectgoth.nemesis.listeners.PinMessageListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.util.ChatUtils;

/**
 * Created by houdangui on 31/3/15.
 */
public class PinMessageUpdatedListener extends PinMessageListener {

    private boolean isServerPushBackMessage;

    public void setIsServerPushBackMessage(boolean isServerPushBackMessage) {
        this.isServerPushBackMessage = isServerPushBackMessage;
    }

    @Override
    public void onPinnedMessageUpdated(FusionPktMessage messagePacket) {
        Message pinnedMessageSent = ChatUtils.createMessageFromPacket(messagePacket);
        String conversationId = pinnedMessageSent.getConversationId();

        if (messagePacket.getPinnedType() == PinnedType.PINNED) {
            //sticker and plain emote message are all sent with command and then server will push back the
            //emote message, so we don't set them on client side
            if (!isServerPushBackMessage) {
                ChatDatastore.getInstance().setChatConversationPinnedMessage(conversationId,
                        pinnedMessageSent, false);
            }
        } else if (messagePacket.getPinnedType() == PinnedType.UNPINNED) {
            ChatDatastore.getInstance().setChatConversationPinnedMessage(conversationId,
                    null, false);
        }
    }

    public void onError(MigError error) {
        super.onError(error);

        if (error != null) {
            if (error.getMatchedPacket() != null && error.getMatchedPacket().getType() == PacketType.MESSAGE) {
                // This scenario occurs when the server replies with an error message to be shown to the user.
                FusionPktMessage failedMessage = new FusionPktMessage(error.getMatchedPacket());
                String errorMessage = error.getErrorMsg();
                if (failedMessage != null && !TextUtils.isEmpty(errorMessage)) {
                    ChatDatastore.getInstance().addInfoOrErrorMessage(failedMessage.getMessageType(), errorMessage, failedMessage.getDestination(),
                            failedMessage.getChatType(),
                            failedMessage.getDestination(), ChatController.getInstance().getActiveConversationId(),
                            false);
                }
            }
        }
    }
}
