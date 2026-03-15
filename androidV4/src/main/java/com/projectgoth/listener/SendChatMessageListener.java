/**
 * Copyright (c) 2013 Project Goth
 *
 * SendChatMessageListener.java
 * Created 11 Jun, 2014, 4:42:22 pm
 */

package com.projectgoth.listener;

import android.text.TextUtils;
import android.util.Log;

import com.projectgoth.blackhole.enums.ChatDestinationType;
import com.projectgoth.blackhole.enums.EmoteType;
import com.projectgoth.blackhole.enums.PacketType;
import com.projectgoth.blackhole.fusion.packet.FusionPktMessage;
import com.projectgoth.common.Logger;
import com.projectgoth.controller.ChatController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.nemesis.listeners.ChatListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigRequest;
import com.projectgoth.util.ChatUtils;

/**
 * @author Dan
 * 
 */
public class SendChatMessageListener extends ChatListener {

    @Override
    public void onMessageSent(FusionPktMessage messagePacket, Long timestamp) {
        if (messagePacket != null) {
            final Message existingMessage = ChatUtils.findExistingMessageMatchingPacket(messagePacket);
            if (existingMessage != null) {
                if (existingMessage.getEmoteContentType() == EmoteType.STICKERS) {
                    GAEvent.Chat_SendStickerUiSuccess.send();
                } else if (existingMessage.getEmoteContentType() == EmoteType.PLAIN) {
                    GAEvent.Chat_SendTextMessageSuccess.send();
                }
                
                // Existing message was successfully sent.
                existingMessage.setDeliveryStatus(Message.DeliveryStatus.SENT_TO_SERVER, true);
                if (timestamp != null) {
                    long timestampDelta = timestamp - existingMessage.getInternalTimestamp();
                    Logger.debug.log("MsgOrderIssue", "msg sent:", existingMessage.getMessage(), " delta:", timestampDelta);
                    //update timestamp from server
                    existingMessage.setLongTimestamp(timestamp);
                    
                    // save the message persistently for updating timestamp in db 
                    String conversationId = existingMessage.getConversationId();
                    ChatDatastore.getInstance().saveChatMessageToPersistentStorage(
                            conversationId, existingMessage);
                    
                    // estimate the timestamps of following sending messages to keep them in correct order
                    ChatConversation chat = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
                    chat.estimateTimestampsOfMessageBeingSent(timestampDelta);
                    
                }
                BroadcastHandler.ChatMessage.sendSent(existingMessage.getMessageId());
            }            
        }
    };

    @Override
    public void onPhotoSent(FusionPktMessage message) {
        GAEvent.Chat_SendImageSuccess.send();
        ChatDestinationType destinationType = message.getChatType();
        String destination = message.getDestination();
        
        ChatDatastore.getInstance().addInfoOrErrorMessage(message.getMessageType(), I18n.tr("Photo sent"), destination,
                destinationType, destination, ChatController.getInstance().getActiveConversationId(), true);
    }

    @Override
    public void onSmsSent(FusionPktMessage message) {
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
            
            if (error.getRequest() != null) {
                // Try to find the existing message from the original request.
                final MigRequest sentRequest = error.getRequest();
                final FusionPktMessage messagePacket = (FusionPktMessage) sentRequest.getPacket();
                if (messagePacket != null) {
                    final Message existingMessage = ChatUtils.findExistingMessageMatchingPacket(messagePacket);
                    if (existingMessage != null) {
                        if (existingMessage.getEmoteContentType().equals(EmoteType.STICKERS)) {
                            GAEvent.Chat_SendStickerUiFail.send();
                        } else if (existingMessage.getEmoteContentType().equals(EmoteType.PLAIN)) {
                            GAEvent.Chat_SendTextMessageFail.send();
                        }
                        // Existing message failed to be sent.
                        existingMessage.setDeliveryStatus(Message.DeliveryStatus.FAILED, true);

                        // Save failed chat message for later to resend
                        ChatController.getInstance().saveFailedMessage(existingMessage);

                        BroadcastHandler.ChatMessage.sendSendError(error, existingMessage.getMessageId());
                    }
                }
            }
        }
    }

}
