/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatController.java
 * Created Jul 7, 2013, 3:01:45 AM
 */

package com.projectgoth.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeType;
import com.projectgoth.b.data.mime.StickerMimeData;
import com.projectgoth.b.data.mime.TextRichMimeData;
import com.projectgoth.blackhole.enums.ChatDestinationType;
import com.projectgoth.blackhole.enums.ChatParticipantType;
import com.projectgoth.blackhole.enums.ContentType;
import com.projectgoth.blackhole.enums.EmoteType;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.MessageStatusType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.enums.PacketType;
import com.projectgoth.blackhole.enums.PinnedType;
import com.projectgoth.blackhole.fusion.packet.FusionPacket;
import com.projectgoth.blackhole.fusion.packet.FusionPktGroupChat;
import com.projectgoth.blackhole.fusion.packet.FusionPktJoinChatroom;
import com.projectgoth.blackhole.fusion.packet.FusionPktLatestMessagesDigest;
import com.projectgoth.blackhole.fusion.packet.FusionPktMessage;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.listener.ChatSyncListenerImpl;
import com.projectgoth.listener.ChatSyncOnChatShownListener;
import com.projectgoth.listener.GroupChatListenerImpl;
import com.projectgoth.listener.PinMessageUpdatedListener;
import com.projectgoth.listener.SendChatMessageListener;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.model.Message.DeliveryStatus;
import com.projectgoth.model.PinMessageData;
import com.projectgoth.nemesis.NetworkResponseListener;
import com.projectgoth.nemesis.NetworkResponseListener.NetworkEventListener;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.nemesis.listeners.ChatListener;
import com.projectgoth.nemesis.listeners.ChatSyncListener;
import com.projectgoth.nemesis.listeners.CreateChatroomListener;
import com.projectgoth.nemesis.listeners.GetChatParticipantsListener;
import com.projectgoth.nemesis.listeners.GetMessageStatusEventsListener;
import com.projectgoth.nemesis.listeners.GetPinnedMessageListener;
import com.projectgoth.nemesis.listeners.JoinChatRoomListener;
import com.projectgoth.nemesis.listeners.LeaveChatRoomListener;
import com.projectgoth.nemesis.listeners.LeaveGroupChatListener;
import com.projectgoth.nemesis.listeners.LeavePrivateChatListener;
import com.projectgoth.nemesis.listeners.SetMuteChatListener;
import com.projectgoth.nemesis.listeners.SimpleResponseListener;
import com.projectgoth.nemesis.listeners.UploadPhotoListener;
import com.projectgoth.nemesis.model.BaseEmoticon;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.nemesis.model.MessageStatusEvent;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigResponse;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.nemesis.model.VirtualGift;
import com.projectgoth.service.NetworkService;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.fragment.ChatListFragment;
import com.projectgoth.ui.fragment.CreateChatroomFragment;
import com.projectgoth.ui.fragment.StartChatFragment;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.ChatUtils;

/**
 * Purpose: - Provides utility methods to send Chat related requests.
 * 
 * @author angelorohit
 */
public class ChatController {

    private static final String         TAG      = AndroidLogger.makeLogTag(ChatController.class);

    // TODO: set this variable from the Chat Activity/ Fragment
    private String                      activeConversationId;

    private static final String STICKER_COMMAND = "/sticker %s";
    private static final String GIFT_COMMAND = "/gift %s %s";
    private static final String GIFT_ALL_COMMAND = "/gift all %s";
    private static final String GIFT_COMMAND_PARAM_MSG = "-m %s";
    
    private final static ChatController INSTANCE = new ChatController();

    /**
     * Constructor
     */
    private ChatController() {
    }

    /**
     * A single point of entry for this controller.
     * 
     * @return An instance of the controller.
     */
    public static synchronized ChatController getInstance() {
        return INSTANCE;
    }

    
    //@formatter:off
    private final ChatSyncListener chatSyncListener = new ChatSyncListenerImpl();
    
    private GetChatParticipantsListener getChatParticipantListener = new GetChatParticipantsListener() {

        @Override
        public void onChatRoomParticipantsReceived(final String chatId
                , final List<ChatParticipant> chatRoomParticipantList, final ChatParticipantType requestingUserType) {
            ChatDatastore.getInstance().setChatRoomParticipants(chatId, chatRoomParticipantList, requestingUserType);
        }

        @Override
        public void onGroupChatParticipantsReceived(String chatId,
                List<ChatParticipant> groupChatParticipantList) {
            ChatDatastore.getInstance().setGroupChatParticipants(chatId, groupChatParticipantList);
        }

        @Override
        public void onError(MigError error) {
            super.onError(error);
            
            FusionPacket sentPacket = error.getRequest().getPacket();
            String chatId = sentPacket.getStringField((short) 1);
            switch (sentPacket.getType()) {
                case GET_CHATROOM_PARTICIPANTS:
                    BroadcastHandler.ChatParticipant.ChatRoom.sendFetchAllError(error, chatId);
                    break;
                case GET_GROUP_CHAT_PARTICIPANTS:
                    BroadcastHandler.ChatParticipant.GroupChat.sendFetchAllError(error, chatId);
                    break;
                default:
                    break;
            }
        }
        
    };
    
    private ChatListener chatListener = new SendChatMessageListener();
    
    /**
     * Sends a request to join a ChatRoom
     * 
     * @param conversationId
     *            The ChatConversation chatId
     */
    public void requestJoinChatRoom(final String conversationId, final boolean retrievePinned) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendJoinChatRoom(new JoinChatRoomListener() {

                    @Override
                    public void onChatRoomJoined(final String chatId, final Integer groupId) {
                        ChatDatastore.getInstance().joinChatRoomWithChatId(chatId, groupId);
                    }

                    @Override
                    public void onChatRoomParticipantsReceived(String chatId
                            , List<ChatParticipant> chatRoomParticipantList, ChatParticipantType requestingUserType) {
                        ChatDatastore.getInstance().setChatRoomParticipants(chatId, chatRoomParticipantList, requestingUserType);
                    }

                    @Override
                    public void onChatRoomNoPinnedMessageReceived(String chatId, ChatDestinationType destinationType) {
                        ChatConversation chatroom = ChatDatastore.getInstance().getChatRoomWithChatId(chatId);
                        if (chatroom != null) {
                            ChatDatastore.getInstance().setChatConversationPinnedMessage(chatroom.getId(), null, true);
                        }
                    }

                    @Override
                    public void onMessageReceived(MigResponse response) {
                        ChatController.getInstance().processChatMessageServerResponse(response, false);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);

                        if (error.getMatchedPacket() instanceof FusionPktJoinChatroom) {
                            FusionPktJoinChatroom failedJoinedChatroom = (FusionPktJoinChatroom) error.getMatchedPacket();
                            String chatId = failedJoinedChatroom.getChatroomName();
                            String errorMessage = error.getErrorMsg();

                            if (!TextUtils.isEmpty(chatId) && !TextUtils.isEmpty(errorMessage)) {
                                ChatDatastore.getInstance().addInfoOrErrorMessage(MessageType.FUSION, errorMessage,
                                        chatId, ChatDestinationType.CHATROOM,
                                        chatId, activeConversationId, false);
                            }
                        }
                    }

                }, conversationId, retrievePinned);
            }
        }
    }

    /**
     * Sends a request to leave a ChatRoom
     * 
     * @param chatId
     *            The ChatConversation chatId.
     */
    public void requestLeaveChatRoom(final String chatId) {
        // We attempt to pre-emptively remove the chatroom.
        // If a message is received on failure, the chatroom would come back.
        ChatDatastore.getInstance().leaveChatRoomWithChatId(chatId);
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                ChatDatastore.getInstance().addToChatroomLeft(chatId);
                requestManager.sendLeaveChatRoom(new LeaveChatRoomListener() {

                    @Override
                    public void onChatRoomLeft(String chatId) {
                        ChatDatastore.getInstance().removeFromChatroomLeft(chatId);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        ChatDatastore.getInstance().removeFromChatroomLeft(chatId);
                        BroadcastHandler.ChatConversation.ChatRoom.sendLeaveError(error, chatId);
                    }

                }, chatId);
            }
        }
    }

    /**
     * Sends a request to retrieve chat room participants.
     * 
     * @param chatId
     *            The chat identifier of the chatroom.
     */
    public void requestChatRoomParticipants(final String chatId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetChatRoomParticipants(getChatParticipantListener, chatId);
            }
        }
    }

    /**
     * Sends a request to retrieve group chat participants.
     * 
     * @param chatId
     *            The chat identifier of the chatroom.
     */
    public void requestGroupChatParticipants(final String chatId, MessageType type) {
        requestGroupChatParticipants(ApplicationEx.getInstance().getNetworkService(), chatId, type);
    }
    
    /**
     * Sends a request to retrieve group chat participants.
     * 
     * @param chatId
     *            The chat identifier of the chatroom.
     */
    public void requestGroupChatParticipants(NetworkService service, final String chatId, MessageType type) {
        RequestManager requestManager = service == null ? null : service.getRequestManager();
        if (requestManager != null) {
            requestManager.sendGetGroupChatParticipants(getChatParticipantListener, chatId, type.getImType());
        }
    }

    /**
     * Sends a request to kick a user from a chat room. On success, the user
     * isn't actually kicked (unless the requestor is the admin of the
     * chatroom). Instead a voting process begins in the chat to kick the user.
     * 
     * @param conversationId
     *            The chat identifier of the public ChatConversation.
     * @param username
     *            The username of the ChatParticipant to be kicked.
     */
    public void requestKickChatRoomParticipant(final String conversationId, final String username) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                
                final ChatConversation chatConversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
                
                if(chatConversation == null) {
                    return;
                }
                
                final String chatId = chatConversation.getChatId();
                
                requestManager.sendKickChatRoomParticipant(new NetworkResponseListener() {

                    @Override
                    public void onResponseReceived(MigResponse response) {
                        // Nothing to do here.
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.ChatParticipant.ChatRoom.sendKickError(error, chatId, username);
                        ChatDatastore.getInstance().addInfoOrErrorMessage(error.getErrorMsg(), chatConversation,
                                activeConversationId, true);
                    }
                }, chatId, username);
                
                ChatDatastore.getInstance().addInfoOrErrorMessage(String.format(I18n.tr("Requesting to kick %s"), username), chatConversation,
                        activeConversationId, true);
            }
        }
    }

    /**
     * Sends a request to leave a private chat.
     * 
     * @param chatId
     *            The chat identifier of the ChatConversation to be left.
     * @param imType
     *            The IMMMessageTypeEnum of the ChatConversation to be left.
     */
    public void requestLeavePrivateChat(final String chatId, final ImType imType) {
        // We attempt to pre-emptively remove the private chat.
        // If a message is received on failure, the private chat would come back.
        ChatDatastore.getInstance().leavePrivateChatConversationWithChatId(chatId);
        
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendLeavePrivateChat(new LeavePrivateChatListener() {

                    @Override
                    public void onPrivateChatLeft(final String chatId) {
                        ChatDatastore.getInstance().leavePrivateChatConversationWithChatId(chatId);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.ChatConversation.PrivateChat.sendLeaveError(error, chatId);
                    }

                }, chatId, imType);
            }
        }
    }

    /**
     * Sends a request to leave a group chat.
     * 
     * @param chatId
     *            The chat identifier of the ChatConversation to be left.
     * @param imType
     *            The IMMessageTypeEnum of the ChatConversation to be left.
     */
    public void requestLeaveGroupChat(final String chatId, final ImType imType) {
        // We attempt to pre-emptively remove the group chat.
        // If a message is received on failure, the group chat would come back.
        ChatDatastore.getInstance().leaveGroupChatConversationWithChatId(chatId);
        
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                ChatDatastore.getInstance().addToGroupChatLeft(chatId);   
                requestManager.sendLeaveGroupChat(new LeaveGroupChatListener() {

                    @Override
                    public void onGroupChatLeft(final String chatId) {
                        ChatDatastore.getInstance().removeFromGroupChatLeft(chatId);
                        ChatDatastore.getInstance().leaveGroupChatConversationWithChatId(chatId);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        ChatDatastore.getInstance().removeFromGroupChatLeft(chatId);
                        BroadcastHandler.ChatConversation.GroupChat.sendLeaveError(error, chatId);
                    }

                }, chatId, imType);
            }
        }
    }

    public void sendPhotoMessage(String conversationId, byte[] photoData, final Activity activity) {

        final ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null && photoData != null) {
            Logger.debug.flog(TAG, "sendPhotoMessage convId: %s chatId: %s content length: %d",
                    conversationId, conversation.getChatId(), photoData.length);
            final Message infoMessage = ChatDatastore.getInstance().createInfoOrErrorMessage(conversation.getImMessageType(),
                    I18n.tr("Sending"), Session.getInstance().getUsername(),
                    conversation.getDestinationType(), conversation.getChatId());
            ChatDatastore.getInstance().addInfoOrErrorMessage(infoMessage, activeConversationId, true);

            //upload photo
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.sendUploadPhoto(new UploadPhotoListener() {

                    public void onPhotoUploaded(final String url, final String thumbnailUrl) {
                        //send message with url in the mime data
                        ChatDatastore.getInstance().removeChatMessage(infoMessage, false);
                        Message msg = conversation.createPhotoMessage(url, thumbnailUrl);
                        sendChatMessage(conversation, msg);
                    }

                    @Override
                    public void onPhotoUploadedError(MigError error) {
                        Logger.debug.log(TAG, "photo uploaded error : ", error.getErrorMsg());
                        ChatDatastore.getInstance().removeChatMessage(infoMessage, true);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Tools.showToast(activity, I18n.tr("Photo sending failed!"), Toast.LENGTH_LONG);

                            }
                        });

                    }
                }, photoData);
            }
        } else {
            Logger.error.log(TAG, "Failed to send message to conversation: ", conversationId);
        }
    }

    public void sendShareMessage(String conversationId, String comment, String url, ArrayList<MimeData> mimeDataList) {
        final ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        ArrayList<MimeData> dataList = new ArrayList<>(mimeDataList);
        if (conversation != null) {
            //plain text for clients that cannot handle the mime data
            String text = comment + Constants.SPACESTR + url;
            if (TextUtils.isEmpty(comment)) {
                text = url;
            }

            //create mime data of comment
            if (!TextUtils.isEmpty(comment)) {
                String[] hotkeys = ChatUtils.getHotkeysInString(comment);
                TextRichMimeData textRichMimeData = TextRichMimeData.createFromText(comment, null, hotkeys);
                dataList.add(0, textRichMimeData);
            }

            //create the message and send
            Message msg = conversation.createShareMessage(text, dataList);
            sendChatMessage(conversation, msg);
        }
    }

    /**
     * Sends a request to send a chat message text for a given conversation.
     * @param conversation  The {@link ChatConversation} to which the chat message is to be sent.
     * @param text  The text to be sent to the {@link ChatConversation}
     * @return  A {@link Message} object describing the content that was sent.
     */
    private Message requestSendMessage(ChatConversation conversation, String text) {
        return requestSendMessage(conversation, text, null);
    }

    /**
     * Sends a request to send a chat message text for a given conversation with an optional {@link ChatListener} 
     * @param conversation  The {@link ChatConversation} to which the chat message is to be sent.
     * @param text          The text to be sent to the {@link ChatConversation}
     * @param listener      A {@link ChatListener} to be informed of callbacks.
     * @return              A {@link Message} object describing the content that was sent.
     */
    private Message requestSendMessage(ChatConversation conversation, String text, ChatListener listener) {
        Message msg = conversation.createTextMessage(text);
        return requestSendMessage(msg, conversation, listener);
    }
    
    /**
     * Sends a request to send a chat message for a given conversation.
     * @param msg           An {@link Message} to be sent to the server.
     * @param conversation  The {@link ChatConversation} to which the chat message is to be sent.
     * @param listener      A {@link ChatListener} to be informed of callbacks.
     * @return              The {@link Message} that was sent to the server.
     */
    private Message requestSendMessage(final Message msg, ChatConversation conversation, ChatListener listener) {
        if (msg != null && msg.isValid()) {
            sendChatMessageToServer(msg, listener);
        }
        return msg;
    }
    
    /**
     * Creates a {@link Message} out of the given text and dispatches a request to send the message to a {@link ChatConversation} matching the given conversation id.
     * @param conversationId    The conversation id of the {@link ChatConversation} to which the chat message is to be sent.
     * @param text              The text to be sent.
     */
    public void sendChatMessage(String conversationId, String text) {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null && !TextUtils.isEmpty(text)) {
            //send message to server
            Message msg = conversation.createTextMessage(text);
            sendChatMessage(conversation, msg);
        } else {
            Logger.error.flog(TAG, "Failed to send message to conversation: %s message: %s", conversationId, text);
        }
    }
    
    /**
     * Prepares and sends a given {@link Message} to a given {@link ChatConversation}.
     * @param chatConversation The {@link ChatConversation} to which the chat message is to be sent.
     * @param message          The {@link Message} to be sent.
     */
    public void sendChatMessage(final ChatConversation chatConversation, final Message message) {
        if (chatConversation != null && message != null) {
            final String messageText = message.getMessage();
            final String chatConversationId = chatConversation.getId();
            if (!TextUtils.isEmpty(chatConversationId) && !TextUtils.isEmpty(messageText)) {
                Logger.debug.log(
                        TAG,
                        String.format("sendChatMessage convId: %s chatId: %s text: %s", chatConversationId,
                                chatConversation.getChatId(), messageText));
                
                //set the previous message id of the message to send from client side here
                setPrevMessageIdForLiveMessage(message, chatConversation);
                
                //add the message to the conversation and save it
                if (message.isValid() && !message.getMessage().startsWith(Constants.SLASHSTR)) {
                    ChatDatastore.getInstance().addChatMessage(message, activeConversationId, true);
                }
                requestSendMessage(message, chatConversation, null);
            } else {
                Logger.error.flog(TAG, "Failed to send message to conversation: %s message: %s",
                        chatConversationId, messageText);
            }
        }
    }

    /**
     * set the previous message id of live message, this is discussed with server guy,
     * for outgoing live message, the pre msg id will not be sent it to server, server will set it from server side
     * for incoming live message, server doesn't set it for better performance
     *
     * @param message
     * @param chatConversation
     */

    private void setPrevMessageIdForLiveMessage(Message message, ChatConversation chatConversation) {

        final boolean excludingServerInfoMessage = true;

        //if the previous message is a SERVER_INFO, we need to set the previous id of the message to be one
        //message which is not SERVER_INFO before it. because we don't save the SERVER_INFO message, it'll
        // display a message gap incorrectly otherwise
        Message lastMessage = chatConversation.getMostRecentMessage(excludingServerInfoMessage);
        String prevMessageId = lastMessage == null ? null : lastMessage.getMessageId();
        message.setPrevMessageId(prevMessageId);
    }

    public void sendSticker(String conversationId, Sticker sticker) {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);

        if (conversation != null && sticker != null) {
            final String commandText = String.format(STICKER_COMMAND, sticker.getAlias());
            Message stickerTextMsg = conversation.createTextMessage(commandText);
            if (stickerTextMsg != null && stickerTextMsg.isValid()) {
                Message stickerEmoteMsg = conversation.createStickerMessage(stickerTextMsg.getMessageId(), commandText, sticker);
                //we send sticker message as text message but save it as sticker emote message.
                // it is discussed with server guy
                ChatDatastore.getInstance().addChatMessage(stickerEmoteMsg, activeConversationId, true);
                requestSendMessage(stickerTextMsg, conversation, null);
            }
        } else {
            Logger.error.log(TAG, "Failed to send sticker to conversation: ", conversationId);
        }
    }

    public void sendGift(String conversationId, VirtualGift virtualGift) {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);

        if (conversation != null && virtualGift != null) {

            String text;

            if (conversation.isGroupChat() || conversation.isChatroom()) {
                text = createGiftAllCommand(virtualGift.getName());
            } else {
                text = createGiftCommand(conversation.getChatId(), virtualGift.getName());
            }
            requestSendMessage(conversation, text);
        } else {
            Logger.error.log(TAG, "Failed to send VG to conversation: ", conversationId);
        }
    }
    
    public void sendGift(String conversationId, String giftName, String message, ChatListener listener) {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);

        if (conversation != null && giftName != null) {

            String text;

            if (conversation.isGroupChat() || conversation.isChatroom()) {
                text = createGiftAllCommand(giftName, message);
            } else {
                text = createGiftCommand(conversation.getChatId(), giftName, message);
            }
            requestSendMessage(conversation, text, listener);
        } else {
            Logger.error.log(TAG, "Failed to send VG to conversation: ", conversationId);
        }
    }
    
    public void sendGift(String conversationId, String recipientName, String giftName, String message, ChatListener listener) {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);

        if (conversation != null && recipientName != null && giftName != null) {
            
            String text = createGiftCommand(recipientName, giftName, message);
            
            requestSendMessage(conversation, text, listener);
        } else {
            Logger.error.log(TAG, "Failed to send VG to: ", recipientName);
        }
    }
    
    private String createGiftCommand(String recipientName, String giftName, String message) {
        String command = String.format(GIFT_COMMAND, recipientName, giftName);
        command = appendGiftCommandMsgParam(command, message);
        return command;
    }
    
    private String createGiftCommand(String recipientName, String giftName) {
        return createGiftCommand(recipientName, giftName, null);
    }
    
    private String createGiftAllCommand(String giftName, String message ) {
        String command = String.format(GIFT_ALL_COMMAND, giftName);
        command = appendGiftCommandMsgParam(command, message);
        return command;
    }
    
    private String createGiftAllCommand(String giftName) {
        return createGiftAllCommand(giftName, null);
    }
    
    private String appendGiftCommandMsgParam(String command, String message) {
        String fullCommand = command;
        if (!TextUtils.isEmpty(message)) {
            String param = String.format(GIFT_COMMAND_PARAM_MSG, message);
            fullCommand = command + Constants.SPACESTR + param;
        }
        return fullCommand;
    }
    
    // TODO: refactor this method...
    public void sendPrivateChatMessage(String recipient, String text) {
        if (!TextUtils.isEmpty(recipient) && !TextUtils.isEmpty(text)) {
            Message msg = ChatUtils.createNewPrivateMessage(recipient, text);
            sendChatMessageToServer(msg);
        } else {
            Logger.error.flog(TAG, "Failed sending to recipient: %s message: %s", recipient, text);
        }
    }

    private final GroupChatListenerImpl defGroupChatListener = new GroupChatListenerImpl();

    /**
     * Start a group chat with multiple users
     * 
     * @param invitedUsers
     * @param type
     */
    public void startGroupChat(List<String> invitedUsers, final MessageType type,
                               final StartChatFragment.StartChatListener startChatListener) {
        GroupChatListenerImpl listener = new GroupChatListenerImpl();

        listener.setNetworkEventListener(new NetworkEventListener() {

            @Override
            public void onPreResponse(MigResponse response) {
                // DO NOTHING
            }

            @Override
            public void onPostResponse(MigResponse response) {
                FusionPacket packet = response.getPacketReceived();
                if (packet != null && packet.getType() == PacketType.GROUP_CHAT) {
                    FusionPktGroupChat grpPkt = new FusionPktGroupChat(packet);

                    String chatId = grpPkt.getGroupChatId();
                    ChatConversation conv = ChatDatastore.getInstance().findOrCreateConversation(
                            ChatTypeEnum.MIG_GROUP, chatId, MessageType.FUSION);
                    ActionHandler.getInstance().displayChatConversation(null, conv.getId());
                    if (startChatListener != null)
                        startChatListener.onChatCreated(conv.getId());
                }
            }
        });

        RequestManager manager = ApplicationEx.getInstance().getRequestManager();
        if (manager != null) {
            manager.sendCreateGroupChat(listener, invitedUsers, type);
        }
    }

    public void createChatroom(final String chatroomName,String description, String keywords,
                               String language, boolean allowKicking,
                               final CreateChatroomFragment.ChatroomCreatedListener createdlistener) {

        CreateChatroomListener listener = new CreateChatroomListener() {

            @Override
            public void onChatroomCreated(String chatId, Integer groupId) {

                // create or find the chatroom conversation
                ChatDatastore.getInstance().findOrCreateConversation(ChatTypeEnum.CHATROOM,
                        chatId, groupId == null ? 0 : groupId.intValue(), MessageType.FUSION);

                // server returns chatroom packet means the client already joined it
                ChatDatastore.getInstance().joinChatRoomWithChatId(chatId, groupId);

                if (createdlistener != null)
                    createdlistener.onChatroomCreated(chatId, groupId);
            }

            @Override
            public void onError(MigError error) {
                super.onError(error);

                BroadcastHandler.ChatRoom.sendCreatedError(error, chatroomName);
            }
        };

        RequestManager manager = ApplicationEx.getInstance().getRequestManager();
        if (manager != null) {
            manager.sendCreateChatroom(listener, chatroomName, description, keywords, language, allowKicking);
        }
    }

    /**
     * Invite multiple users to a group chat
     * 
     * @param groupChatId
     * @param invitedUsers
     * @param type
     */
    public void inviteToGroupChat(String groupChatId, List<String> invitedUsers, MessageType type) {
        RequestManager manager = ApplicationEx.getInstance().getRequestManager();
        if (manager != null) {
            manager.sendGroupChatInvite(defGroupChatListener, groupChatId, invitedUsers, type);
        }
    }

    /**
     * Starts a group chat
     * 
     * @deprecated
     * 
     * @param listener
     * @param invitee1
     * @param invitee2
     * @param type
     */
    public void startGroupChat(GroupChatListenerImpl listener, String invitee1, String invitee2,
            final MessageType type) {
        RequestManager manager = ApplicationEx.getInstance().getRequestManager();
        if (manager != null) {
            manager.sendCreateGroupChat(listener, invitee1, invitee2, type);
        }
    }

    /**
     * Invites a user to a group chat
     * 
     * @deprecated
     * 
     * @param groupChatId
     * @param username
     * @param type
     */
    public void inviteToGroupChat(String groupChatId, String username, MessageType type) {
        RequestManager manager = ApplicationEx.getInstance().getRequestManager();
        if (manager != null) {
            manager.sendGroupChatInvite(defGroupChatListener, groupChatId, username, type);
        }
    }

    private void sendChatMessageToServer(Message message) {
        sendChatMessageToServer(message, chatListener);
    }
    
    private void sendChatMessageToServer(Message message, ChatListener listener) {
        try {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            // TODO: refactor this request to sentMessage(listener, Message
            // message);
            if (requestManager != null) {
                message.setDeliveryStatus(DeliveryStatus.SENDING);
                message.estimateTimestampInSendingStatus();
                BroadcastHandler.ChatMessage.sendSending(message.getMessageId());
                requestManager.sendChatMessage(listener == null ? chatListener : listener, message.getMessagePacket());
            }
        } catch (Exception e) {
            Logger.error.log(TAG, "Failed to send private chat: ", e.getMessage());
            // TODO: handle sending error
        }
    }

    public void initializeChats() {
        initializeChats(ApplicationEx.getInstance().getNetworkService());
    }
    
    public void initializeChats(NetworkService service) {
        Logger.debug.log(TAG, "initializeChats: " + Config.getInstance().isChatSyncEnabled());
        if (Config.getInstance().isChatSyncEnabled()) {
            int version = ChatDatastore.getInstance().getChatListVersion();
            try {
                RequestManager requestManager = service == null ? null : service.getRequestManager();
                if (requestManager != null) {
                    requestManager.sendGetChats(chatSyncListener, version);
                }
            } catch (Exception e) {
                Logger.error.log(TAG, "Error Requesting for Chats: ", e.getMessage());
            }
        }
    }
   
    public void sendGetMessagesOnChatShown(String conversationId) {
        Logger.debug.log(TAG, "sendGetNewMessages: " + conversationId);
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conv == null) {
            return;
        }
        
        if (!conv.isSyncedOnShow()) {
            
            ChatSyncOnChatShownListener listener = new ChatSyncOnChatShownListener(conv.getId());
            sendGetMessages(conversationId, null, null, listener);
            
            String chatId = conv.getChatId();
            BroadcastHandler.ChatMessage.sendBeginFetchAll(chatId);

            //sync the pinned message
            // if private or group chat, fetch pinned message
            if (conv.isMigPrivateChat() || conv.isMigGroupChat()) {
                ChatController.getInstance().sendGetPinnedMessage(conv.getChatId(), conv.getDestinationType());
            }
        }
    }
    
    /**
     * @param conversationId
     */
    public void sendGetMsgDeliveryStatus(String conversationId, List<Message> messageList) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (requestManager != null && conv != null && conv.isMigPrivateChat()) {
            
            //collect the outgoing messages which are not received by recipient  
            ArrayList<String> msgGUIDs = new ArrayList<String>();
            ArrayList<Long> timestamps = new ArrayList<Long>();
            
            for (int i = 0; i < messageList.size(); i++) {
                Message msg = messageList.get(i);
                if (msg.isOutgoing() && (msg.getDeliveryStatus() == DeliveryStatus.SENDING
                        || msg.getDeliveryStatus() == DeliveryStatus.SENT_TO_SERVER)) {
                    msgGUIDs.add(msg.getMessageId());
                    timestamps.add(msg.getLongTimestamp());
                }
            }
            
            if (msgGUIDs.size() == 0) {
                return;
            }
            
            //convert Long ArrayList to long array
            long[] tsLongArray = new long[timestamps.size()];
            for (int i = 0; i < timestamps.size(); i++) {
                tsLongArray[i] = timestamps.get(i).longValue();
            }
            
            //send request
            requestManager.sendGetMessageStatusEvents(new GetMessageStatusEventsListener() {
                
                    @Override
                    public void onMessageStatusEventsReceived(MessageStatusEvent[] msgStatusEvents) {
                        // update the delivery status of the messages
                        
                        for (int i = 0; i < msgStatusEvents.length; i++) {
                            MessageStatusEvent msgStatusEvent = msgStatusEvents[i];
                            
                            String conversationId = ChatUtils.getConversationIdFromMsgStatusEvent(msgStatusEvent);
                            
                            ChatController.getInstance().processMessageStatusEvent(conversationId, 
                                    msgStatusEvent.getGuid(), 
                                    msgStatusEvent.getMessageEventType());
                        }
                    }
                }, 
                conv.getChatId(), 
                conv.getDestinationType(),
                msgGUIDs.toArray(new String[msgGUIDs.size()]), 
                tsLongArray);
        }
    }

    public void sendGetMessages(String conversationId, Long start, Long end) {
        sendGetMessages(conversationId, start, end, chatSyncListener);
    }
    
    public void sendGetMessages(String conversationId, Long start, Long end, ChatSyncListener listener) {
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conv != null) {
            sendGetMessages(conversationId, start, end, conv.getChatSyncLimit(), listener);
        }
    }
    
    public void sendGetMessages(String conversationId, Long start, Long end, int limit, ChatSyncListener listener) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conv != null && requestManager != null) {
            requestManager.sendGetMessages( listener,
                    conv.getChatId(),
                    conv.getDestinationType(),
                    start,
                    end,
                    limit);
        }
    }
    
    public void sendGetMessagesForNewChat(String conversationId) {
        sendGetMessages(conversationId, 
                null, 
                null, 
                Config.getInstance().getMsgReqLimitForNewChat(), 
                chatSyncListener);
    }
    
    
    /**
     * this is to fetch messages after the latest one of the chat
     * 
     * @param conversationId
     */
    public void sendGetLatestMessages(String conversationId) {
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conv == null) {
            return;
        }
        Message lastMessage = conv.getMostRecentMessage();
        
        Long start = lastMessage == null ? null : Long.valueOf(lastMessage.getLongTimestamp());// timestamp of the most recent message stored
        Long end = null;
        sendGetMessages(conversationId, start, end);
    }
    
    public void sendGetMessages(String chatId, ChatDestinationType chatType, Long start, Long end) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null) {
            requestManager.sendGetMessages(chatSyncListener,
                    chatId,
                    chatType,
                    start,
                    end,
                    Config.getInstance().getChatSyncMessagesRequestLimit());
        }
    }
    
    public void sendMessageReceivedEvent(final Message msg) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null && msg != null) {
           
            MessageStatusEvent event = msg.getMessageStatusEvent(MessageStatusType.RECEIVED);
            
            requestManager.sendMessageReceivedEvent(new SimpleResponseListener() {

                @Override
                public void onSuccess(MigResponse response) {
                }

            }, event);
        }
    }
    
    public void sendGetPinnedMessage(final String chatId, final ChatDestinationType chatType) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null) {
            requestManager.sendGetPinnedMessage(new GetPinnedMessageListener() {

                @Override
                public void onNoPinnedMessageReceived(String chatId, ChatDestinationType destinationType) {
                    String chatConversationId = ChatUtils.generateUniqueId(chatId, MessageType.FUSION, chatType);
                    ChatDatastore.getInstance().setChatConversationPinnedMessage(chatConversationId, null, true);
                }

                @Override
                public void onMessageReceived(MigResponse response) {
                    ChatController.getInstance().processChatMessageServerResponse(response, false);
                }

            }, chatId, chatType);
        }
    }
        
    public void requestChangeChatName(final String conversationId, final String newName) {
        Logger.debug.log(TAG, "requestChangeChatName: " + conversationId);
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (conv != null && requestManager != null) {
            requestManager.sendSetChatName(new SimpleResponseListener() {

                @Override
                public void onSuccess(MigResponse response) {
                    ChatDatastore.getInstance().setChatName(conversationId, newName);
                }

                @Override
                public void onError(MigError error) {
                    super.onError(error);
                    BroadcastHandler.ChatConversation.sendNameChangeError(error, conversationId);
                }

            }, conv.getChatId(), conv.getDestinationType(), newName);
        }
    }

    /**
     * @param response 
     *          the data of the chat message from server
     * @param isLiveMessage
     *          Message received from server can be pushed live chat message or response of the client's fetching
     *          messages which is part of chat sync, not live chat message.
     */
    public void processChatMessageServerResponse(MigResponse response, boolean isLiveMessage) {
        FusionPacket packet = null;
        if (response != null) {
            packet = response.getPacketReceived();
        }
        if (packet == null) {
            return;
        }
        Logger.debug.log(TAG, "processChatMessageServerResponse: ", packet);

        Message message = ChatUtils.createMessageFromPacket(new FusionPktMessage(packet));

        PinnedType pinnedType = message.getPinnedType();
        if(pinnedType != null && pinnedType != PinnedType.NONE) {
            ChatController.updatePinnedMessage(message);
            return;
        }

        if (message != null && message.isValid()) {
            //live message from server has null previous message id for a better performance on server side, 
            //so we estimate one on client side.
            if (isLiveMessage && message.getPrevMessageId() == null) {
                ChatConversation chatConversation = ChatDatastore.getInstance().findChatConversation(message);
                if (chatConversation != null) {
                    setPrevMessageIdForLiveMessage(message, chatConversation);
                }
                
                //if the live message comes before chat opened, we sync it here, otherwise we may miss a message gap
                // refer to chat sync doc
                if (chatConversation!= null && !chatConversation.isSyncedOnShow()) {
                    ChatSyncOnChatShownListener listener = new ChatSyncOnChatShownListener(chatConversation.getId());
                    sendGetMessages(chatConversation.getId(), null, null, listener);
                }
            }           
            
            //incoming message from server doesn't exist in db
            if(message.isPrivate() && message.isIncoming() && message.isFusionMessage()
                && !ChatDatastore.getInstance().hasMessageFromStorage(message.getConversationId(), message.getMessageId())) {
                // send message received event
                sendMessageReceivedEvent(message);
            }
            
            //outgoing message from server
            if (message.isOutgoing()) {
                //server does not store a "received by server" event. This is because the presence of a message in the chat sync 
                //store is evidence of "received by server" status. so we set it for this case
                DeliveryStatus deliveryStatus = message.getDeliveryStatus();
                if (deliveryStatus == null || deliveryStatus == DeliveryStatus.UNKNOWN) {
                    Message existingMsg = ChatDatastore.getInstance().findExistingMessage(message);
                    if (existingMsg != null && existingMsg.getDeliveryStatus().isGreaterThanSentToServer()) {
                        message.setDeliveryStatus(existingMsg.getDeliveryStatus());
                    } else {
                        message.setDeliveryStatus(DeliveryStatus.SENT_TO_SERVER);                            
                    }
                }
            }
            
            ChatDatastore.getInstance().addChatMessage(message, activeConversationId, true);
        } else {
            Logger.error.log(TAG, "processChatMessageServerResponse: Ignoring pushed message as it's null or not valid!");
        }
    }

    private static void updatePinnedMessage(Message message) {
        //find the existing chat conversation
        ChatConversation chatConversation = ChatDatastore.getInstance().findChatConversation(message);
        if (chatConversation == null) {
            return;
        }

        if (message.isMessagePinned()) {
            chatConversation.setPinnedMessage(message);
        } else if (message.isMessageUnpinned()) {
            chatConversation.setPinnedMessage(null);
        }

        if (message.isMessagePinned() || message.isMessageUnpinned()) {
            BroadcastHandler.ChatMessage.sendReceivedPinnedState(chatConversation.getId());
        }
    }

    /**
     * @param activeConversationId
     *            the activeConversationId to set
     */
    public void setActiveConversationId(String activeConversationId) {
        this.activeConversationId = activeConversationId;
    }

    public String getActiveConversationId() {
        return this.activeConversationId;
    }

    public void processLatestMessageDigest(FusionPktLatestMessagesDigest digestPkt) {
        
        String[] chatIds = digestPkt.getChatIdList();
        ChatDestinationType[] chatTypes = digestPkt.getChatTypeList();
        String[] timestamps = digestPkt.getTimestampList();
        String[] msgContentSnippets = digestPkt.getMessageContentsList();
        
        if (chatIds != null) {
            for (int i = 0; i < chatIds.length; i++) {
                String chatId = chatIds[i];
                ChatDestinationType chatType = chatTypes[i];
                long timestamp = Long.parseLong(timestamps[i]);
                
                final String conversationId = ChatUtils.generateUniqueId(chatId, MessageType.FUSION, chatType);
                
                final ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(
                        conversationId);               
                
                final String msgSnippet = 
                        (msgContentSnippets == null || msgContentSnippets.length <= i) ? null : msgContentSnippets[i];
                
                if (msgSnippet == null) {
                    Logger.error.log(TAG, 
                            "Server has failed to send a valid message content snippet for conversation with id: " + conversationId);
                }
                
                if (conversation != null) {
                    Logger.debug.logWithTrace(TAG, getClass(), 
                            "conversation display name:" + conversation.getDisplayName()
                            + " server timestamp:" + timestamp + " local timestamp:" + conversation.getChatTimestamp());
                    
                    conversation.setMessageSnippet(msgSnippet);
                    if(timestamp > conversation.getTimestamp()) {
                        Logger.debug.logWithTrace(TAG, getClass(), 
                                "fetch offline messages of existing chat: " + conversation.getDisplayName());
                        ChatController.getInstance().sendGetLatestMessages(conversation.getId());
                    }
                } else {
                    ChatDatastore.getInstance().addMessageSnippet(conversationId, msgSnippet);
                }
            }
        }
    }

    public void processMessageStatusEvent(String conversationId, String messageId, MessageStatusType msgEventType)  {
        
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        
        if(conversation == null)
            return;
        
        // find the message
        Message message = conversation.getMessage(messageId);
        
        switch(msgEventType) {
            case COMPOSING:
                break;
            case RECEIVED:
                if (message != null) {
                    message.setDeliveryStatus(DeliveryStatus.RECEIVED_BY_RECIPIENT, true);
                    BroadcastHandler.ChatMessage.sendReceivedByRecipient(messageId);
                }
                break;
            case READ:
                break;
            default:
                break;
        }
    }

    /**
     * sort the chat list by sortObjects to fix an issue that the ChatConversation object might be changed
     * in another thread during the sorting
     */
    public ArrayList<ChatConversation> getSortedChatList() {
        ArrayList<ChatSortObject> chatList = getAllChatSortObjects();
        ArrayList<ChatConversation> sortedChatList = sortChats(chatList);
        return sortedChatList;
    }

    private ArrayList<ChatSortObject> getAllChatSortObjects() {
        List<ChatConversation> chatList = ChatDatastore.getInstance().getAllChatConversations();
        ArrayList<ChatSortObject> sortObjects = new ArrayList<ChatSortObject>();
        for (ChatConversation chat : chatList) {
            ChatSortObject sortObject = new ChatSortObject(chat);
            sortObjects.add(sortObject);
        }

        return sortObjects;
    }

    private ArrayList<ChatConversation> getSortedChatConversations(ArrayList<ChatSortObject> sortObjects) {
        ArrayList<ChatConversation> chatList = new ArrayList<ChatConversation>();
        for (int i=0; i<sortObjects.size(); i++) {
            ChatSortObject sortObject = sortObjects.get(i);
            chatList.add(sortObject.getChatConversation());
        }
        return chatList;
    }


    private ArrayList<ChatConversation> sortChats(ArrayList<ChatSortObject> list) {
        // sort the rest
       Collections.sort(list, new Comparator<ChatSortObject>() {

           @Override
           public int compare(ChatSortObject lhs, ChatSortObject rhs) {
               if (lhs == rhs) {
                   return 0;
               } else if (rhs == null) {
                   return -1;
               } else if (lhs == null) {
                   return 1;
               }

               //compare by whether having unread msg first
               boolean leftHasUnreadMsg = lhs.hasUnreadMessage();
               boolean rightHasUnreadMsg = rhs.hasUnreadMessage();

               if (leftHasUnreadMsg != rightHasUnreadMsg) {
                   return leftHasUnreadMsg ? -1 : 1;
               }

               // if they both don't have unread msg
               if (!leftHasUnreadMsg && !rightHasUnreadMsg) {

                   //compare whether they are pinned first
                   if (lhs.isPinned() != rhs.isPinned()) {
                       return lhs.isPinned() ? -1 : 1;
                   }

               }

               //compare the timestamp
               if (lhs.getTimestamp() != rhs.getTimestamp()) {
                   return lhs.getTimestamp() > rhs.getTimestamp() ? -1 : 1;
               }

               return 0;

           }
       });

        ArrayList<ChatConversation> sortedChatList = getSortedChatConversations(list);
        
        return sortedChatList;
    }

    /**
     *  it keeps a snapshot of the chat list and properties for sorting for fixing the issue that the ChatConversation
     *  object might be changed in another thread during the sorting.  e.g. received an message during sorting in
     *  another thread
     */
    static private class ChatSortObject {

        private ChatConversation chatConversation;
        private boolean hasUnreadMessage;
        private boolean isPinned;
        private long timestamp;

        public ChatSortObject(ChatConversation chatConversation) {
            this.chatConversation = chatConversation;
            this.hasUnreadMessage = chatConversation.hasUnreadMessage();
            this.isPinned = chatConversation.isPinned();
            this.timestamp = chatConversation.getTimestamp();
        }

        public ChatConversation getChatConversation() {
            return chatConversation;
        }

        public boolean hasUnreadMessage() {
            return hasUnreadMessage;
        }

        public boolean isPinned() {
            return isPinned;
        }

        public long getTimestamp() {
            return timestamp;
        }

    }
    
    public void pin(ChatConversation chatConversation) {
        chatConversation.setPinned(true);
        ChatDatastore.getInstance().saveChatConversationToPersistentStorage(chatConversation);
    }
    
    public void unpin(ChatConversation chatConversation) {
        chatConversation.setPinned(false);
        ChatDatastore.getInstance().saveChatConversationToPersistentStorage(chatConversation);
    }

    public void setMute(final boolean isMute, final ChatConversation chatConversation) {
        setMute(isMute, chatConversation, null);
    }

    public void setMute(final boolean isMute, final ChatConversation chatConversation, final ChatListFragment chatListFragment) {

        try {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.sendSetMuteChat(new SetMuteChatListener() {
                    @Override
                    public void onMuteComplete() {
                        chatConversation.setMuted(isMute);
                        ChatDatastore.getInstance().saveChatConversationToPersistentStorage(chatConversation);

                        if (chatListFragment.getActivity() != null) {
                            chatListFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatListFragment.notifyDataSetChanged(false);
                                }
                            });
                        }
                    }

                    @Override
                    public void onMuteError(MigError error) {
                        if (chatListFragment.getActivity() != null) {
                            Tools.showToast(chatListFragment.getActivity(),
                                    I18n.tr("Oops, something’s not right. Try again."), Toast.LENGTH_SHORT);
                        }
                        Logger.error.log(TAG, "sendSetMuteChat error : " + error.getErrorMsg());
                    }

                }, chatConversation.getChatId(), chatConversation.getDestinationType(), isMute);
            }
        } catch (Exception e) {
            Logger.error.log(TAG, "Failed to send private chat: ", e.getMessage());
        }

    }

    public void fetchAllGroupChatParticipants() {
        fetchAllGroupChatParticipants(ApplicationEx.getInstance().getNetworkService());
    }
    
    public void fetchAllGroupChatParticipants(NetworkService service) {
        List<ChatConversation> chatList = ChatDatastore.getInstance().getAllChatConversations();
        for(ChatConversation conv : chatList) {
            if (conv.isGroupChat()) {
                requestGroupChatParticipants(service, conv.getChatId(), conv.getImMessageType());
            }
        }
    }

    /**
     * when pin message, we send a message created from the selected existing message and the mime data selected
     * also need to process the special cases like sticker message and gift message
     *
     * @param pinData
     */

    public void sendPinnedChatMessageToServer(PinMessageData pinData) {

        try {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {

                Message message = pinData.getMessage();
                MimeData mimeData = pinData.getMimeData();

                Message msgToPin = createMessageToPin(message, mimeData);
                if (msgToPin == null) {
                    return;
                }

                PinMessageUpdatedListener listener = new PinMessageUpdatedListener();
                if (message.isStickerEmoteMessage()) {
                    listener.setIsServerPushBackMessage(true);
                }

                FusionPktMessage messagePkt = msgToPin.getMessagePacket();

                //server will create a new id for the message to pin, it is considered a new message
                messagePkt.setGuid(null);

                requestManager.sendUpdatePinMessage(listener, messagePkt);
            }
        } catch (Exception e) {
            Logger.error.log(TAG, "Failed to send private chat: ", e.getMessage());
        }
    }

    /**
     * when unpin a message, we send the existing pinned message, also need to process the
     * special case of sticker
     *
     * */
    public void sendUnpinnedChatMessageToServer(Message message) {
        if (message == null)
            return;

        try {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {

                PinMessageUpdatedListener listener = new PinMessageUpdatedListener();

                Message msgToUnpin = createMessageToUnpin(message);
                FusionPktMessage messagePkt = msgToUnpin.getMessagePacket();

                requestManager.sendUpdatePinMessage(listener, messagePkt);
            }
        } catch (Exception e) {
            Logger.error.log(TAG, "Failed to send private chat: ", e.getMessage());
        }
    }

    public Message createMessageToPin(Message message, MimeData mimeData) {
        String myUsername = Session.getInstance().getUsername();

        Message msgToPin = new Message(message.getMessageType(), message.getSender(),
                message.getDestinationType(), message.getRecipient(),
                ContentType.TEXT, message.getMessage(), null, null, false);

        //set the mime data
        msgToPin.setRawMimeContent(mimeData.getMimeType().getValue(), mimeData.toJson());

        //make the sender to be myself
        if (msgToPin.isPrivate()) {
            if (!msgToPin.getSender().equals(myUsername)) {
                msgToPin.setDestination(msgToPin.getSender());
                msgToPin.setSender(myUsername);
            }
        } else if (msgToPin.isGroup() || msgToPin.isPublic()) {
            msgToPin.setSender(myUsername);
        }

        //process the sticker and gift
        if (mimeData.getMimeType() == MimeType.STICKER) {
            boolean isValid = processStickerMessageToPin(msgToPin, mimeData);
            if (!isValid) {
                return null;
            }
        } else if (mimeData.getMimeType() == MimeType.GIFT) {
            processGiftMessageToPin(msgToPin, mimeData);
        }

        msgToPin.setPinnedType(PinnedType.PINNED);

        return msgToPin;
    }

    private boolean processStickerMessageToPin(Message msgToPin, MimeData mimeData) {
        StickerMimeData stickerMimeData = (StickerMimeData) mimeData;

        //get sticker hotkey from the mime data
        String hotkey = stickerMimeData.getHotkey();

        //get Sticker object from the hotkey
        BaseEmoticon baseEmoticon = EmoticonDatastore.getInstance().getBaseEmoticonWithHotkey(hotkey);
        Sticker sticker;
        if (baseEmoticon != null && baseEmoticon instanceof Sticker) {
            sticker = (Sticker) baseEmoticon;
        } else {
            return false;
        }

        //construct sticker command with sticker alias
        final String commandText = String.format(STICKER_COMMAND, sticker.getAlias());
        msgToPin.setMessage(commandText);
        msgToPin.setRawMimeContent(null, null);

        return true;
    }

    private void processGiftMessageToPin(Message msgToPin, MimeData mimeData) {
        //gift is a special case of emote type message. we set it as Emote , which is
        //discussed with server guys
        msgToPin.setContentType(ContentType.EMOTE);
        msgToPin.setEmoteContentType(EmoteType.PLAIN);
    }

    /**
     *  creates a message from existing message to unpin, server doesn't really check the
     *  Content and guid of message when unpin. set the ContentType as TEXT then it works
     *  for unpin Sticker, Gift
     *
     * @param message
     * @return
     */
    public Message createMessageToUnpin(Message message) {
        Message msgToUnpin = new Message(message.getMessageType(), message.getSender(),
                message.getDestinationType(), message.getRecipient(),
                ContentType.TEXT, message.getMessage(), null, null, false);

        //swap source and destination for this case
        if (msgToUnpin.isPrivate() && !msgToUnpin.isMyOwnMessage()) {
            String source = msgToUnpin.getRecipient();
            String destination = msgToUnpin.getSender();
            msgToUnpin.setSender(source);
            msgToUnpin.setDestination(destination);
        }

        msgToUnpin.setPinnedType(PinnedType.UNPINNED);

        return msgToUnpin;
    }

    public synchronized void saveFailedMessage(Message message) {
        ChatDatastore.getInstance().persistFailedMessage(message);
    }

    public synchronized void resendAllFailedMessages() {

        ChatDatastore chatDatastore = ChatDatastore.getInstance();
        List<Message> failedMessages = chatDatastore.getAllFailedMessages();
        if (failedMessages != null && failedMessages.size() > 0) {
            for (Message message : failedMessages) {
                String messageConversationId = message.getConversationId();
                if (messageConversationId != null) {
                    ChatConversation chatConversation = chatDatastore.getChatConversationWithId(messageConversationId);
                    sendChatMessage(chatConversation, message);
                }
            }
            chatDatastore.deleteAllFailedMessages();
        }
    }

}
