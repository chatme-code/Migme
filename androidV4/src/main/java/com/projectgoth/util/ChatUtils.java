/**
 * Copyright (c) 2013 Project Goth
 * ChatUtils.java
 * 
 * Jun 13, 2013 5:20:54 PM
 */

package com.projectgoth.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.projectgoth.blackhole.enums.ChatDestinationType;
import com.projectgoth.blackhole.enums.ContentType;
import com.projectgoth.blackhole.enums.EmoteType;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.MessageStatusType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.enums.PinnedType;
import com.projectgoth.blackhole.fusion.packet.FusionPacket;
import com.projectgoth.blackhole.fusion.packet.FusionPktMessage;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.ChatConversation.GroupChatInfo;
import com.projectgoth.model.ChatConversation.PrivateChatInfo;
import com.projectgoth.model.ChatConversation.PublicChatInfo;
import com.projectgoth.model.Message;
import com.projectgoth.model.Message.DeliveryStatus;
import com.projectgoth.nemesis.model.ChatRoomItem;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.MessageStatusEvent;

/**
 * Contains all helper utilities pertaining to Chat.
 * 
 * @author angelorohit
 */
public class ChatUtils {

    /** Construct a private ChatConversation */
    public static ChatConversation createConversationForPrivateChat(final String chatIdentifier, 
            final String displayName, final String displayPicGuid, 
            final ChatDestinationType destType, final MessageType imMessageType,
            final PrivateChatInfo privateChatInfo) {
        ChatConversation conv = new ChatConversation(chatIdentifier, displayName, displayPicGuid, destType, imMessageType);
        conv.setPrivateChatInfo(privateChatInfo);

        return conv;
    }

    /** Construct a group ChatConversation */
    public static ChatConversation createConversationForGroupChat(final String chatIdentifier, 
            final String displayName, final String displayPicGuid, 
            final ChatDestinationType destType, final MessageType imMessageType,
            final GroupChatInfo groupChatInfo) {
        // The display name is passed as null because group chat names should use the default display name.
        ChatConversation conv = new ChatConversation(chatIdentifier, null, displayPicGuid, destType, imMessageType);
        conv.setGroupChatInfo(groupChatInfo);

        return conv;
    }

    /** Construct a public ChatConversation */
    public static ChatConversation createConversationForPublicChat(final String chatIdentifier, 
            final String displayName, final String displayPicGuid, 
            final ChatDestinationType destType, final MessageType imMessageType,
            final PublicChatInfo publicChatInfo) {
        ChatConversation conv = new ChatConversation(chatIdentifier, displayName, displayPicGuid, destType, imMessageType);
        conv.setPublicChatInfo(publicChatInfo);

        return conv;
    }

    /** Construct a ChatConversation from a Message */
    public static ChatConversation createConversationFromMessage(final Message message) {
        
        MessageType imMessageType = message.getMessageType();

        if (message.isPrivate()) {
            PrivateChatInfo privateChatInfo = new PrivateChatInfo(message.getContactID());

            return createConversationForPrivateChat(message.getMessageChatIdentifier(), message.getDisplayName(),
                    message.getDisplayPictureGuid(), message.getDestinationType(), imMessageType, privateChatInfo);
        } else if (message.isGroup()) {
            GroupChatInfo groupChatInfo = new GroupChatInfo(message.getGroupChatOwner());

            return createConversationForGroupChat(message.getMessageChatIdentifier(), message.getDisplayName(),
                    message.getDisplayPictureGuid(), message.getDestinationType(), imMessageType, groupChatInfo);
        } else {
            PublicChatInfo publicChatInfo = new PublicChatInfo(0);

            return createConversationForPublicChat(message.getMessageChatIdentifier(), message.getDisplayName(),
                    message.getDisplayPictureGuid(), message.getDestinationType(), imMessageType, publicChatInfo);
        }
    }
    
    /**
     * Create an Info or Error Message
     * 
     * @param message
     * @param userName
     * @param displayName
     * @return
     */
    public static Message createInfoOrErrorMessage(final MessageType messageType, final String message, final String userName,
            final ChatDestinationType destinationType, final String destination) {

        return createInfoOrErrorMessage(messageType, message, userName, destinationType, destination, null);
    }

    /**
     * Create an Info or Error Message
     * 
     * @param message
     * @param userName
     * @param displayName
     * @param hotkeys
     * @return
     */
    //TODO
    public static Message createInfoOrErrorMessage(final MessageType messageType, final String message, final String userName, final ChatDestinationType destinationType,
            final String destination, final String[] hotkeys) {
        Message temp = new Message(messageType, userName, destinationType, destination, ContentType.TEXT, message, null,
                null, true);
        temp.setHotkeys(hotkeys);
        return temp;
    }
    
    public static Message createNewPrivateMessage(String username, String text) {
        return createNewPrivateMessage(username, username, text, null, MessageType.FUSION, -1);
    }
    
    /**
     * Function to create a new Message when sending a message
     * 
     * @param username
     * @param displayName
     * @param message
     * @param hotkeys
     * @param contactID
     * @return
     */
    public static Message createNewPrivateMessage(String username, String displayName, String message, String[] hotkeys,
            MessageType messageType, int contactID) {
        Message newMessage = new Message(messageType, Session.getInstance().getUsername(), ChatDestinationType.PRIVATE,
                username, ContentType.TEXT, message, null, null, false);
        newMessage.setHotkeys(hotkeys);
        
        // TODO: implement GUID
        // newMessage.setDisplayPictureGuid(Session.getInstance().getDisplayableGuid());
        newMessage.setDisplayName(displayName);
        newMessage.setContactID(contactID);
        return newMessage;
    }
    
    public static ChatConversation createChatroomConversation(String chatRoomName, int groupId) {
        PublicChatInfo publicChatInfo = new PublicChatInfo(groupId);
        return createConversationForPublicChat(chatRoomName, chatRoomName,
                null, ChatDestinationType.CHATROOM, MessageType.FUSION, publicChatInfo);
    }
    
    public static ChatConversation createGroupChatConversation(String groupName,
            String displayName, String owner) {
        GroupChatInfo groupChatInfo = new GroupChatInfo(owner);
        return createConversationForGroupChat(groupName, displayName,
                null, ChatDestinationType.GROUP_CHAT, MessageType.FUSION, groupChatInfo);
    } 
    
    /**
     * Constructs a {@link Message} from a {@link FusionPacket}
     * 
     * @param packet
     * @return
     */
    public static Message createMessageFromPacket(FusionPktMessage packet) {

        MessageType messageType = packet.getMessageType();

        String sourceAddr = packet.getSource();

        ChatDestinationType destinationType = packet.getChatType();

        String destination = packet.getDestination();

        Integer contactId = packet.getContactId();
        if (null == contactId) {
            contactId = -1;
        }
        ContentType contentType = packet.getContentType();

        String filename = packet.getFilename();
        String message = packet.getContent();

        String picGuid = packet.getDisplayPictureGuid();
        String[] hotKeys = packet.getHotkeyList();

        Integer sourceColor = packet.getSourceColor();
        if (null == sourceColor) {
            sourceColor = -1;
        } else {
            sourceColor = (255 << 24) | sourceColor;
        }
        //Logger.debug.log("sourceColor", "sourceColor:" +  Integer.toHexString(sourceColor.intValue()) + " msg:" + newMessage.getMessage());

        Integer messageColor = packet.getMessageColor();
        if (null == messageColor) {
            messageColor = -1;
        } else {
            messageColor = (255 << 24) | messageColor;
        }

        String messageId = packet.getGuid();
        String groupChatOwner = packet.getGroupChatOwner();
        String prevMessageId = packet.getPreviousMessageGuid();

        Long timestamp = packet.getTimestamp();
        if (timestamp == null || timestamp.longValue() == 0) {
            timestamp = Tools.getClientTimestampBasedOnServerTime();
        }

        EmoteType emoteContentType = packet.getEmoteType();

        MessageStatusType msgEventType = packet.getMessageStatus();

        String displayName = packet.getGroupChatName();

        PinnedType pinnedType = packet.getPinnedType();

        Friend foundFriend = null;
        if (contactId != -1) {
            foundFriend = UserDatastore.getInstance().getFriendWithContactId(contactId);
        } else {
            ImType imType = messageType.getImType();
            if (null != imType) {
                foundFriend = UserDatastore.getInstance().getFriendInContactGroupWithUsername(sourceAddr,
                        imType.getValue());
            }
        }

        if (foundFriend != null) {
            displayName = foundFriend.getDisplayName();
        }

        Message newMessage = null;
        if (destinationType == ChatDestinationType.GROUP_CHAT || destinationType == ChatDestinationType.CHATROOM) {
            if (sourceAddr.equals(destination)) {
                newMessage = createInfoOrErrorMessage(messageType, message, destination, destinationType, destination, hotKeys);
            }
        }

        if (newMessage == null) {
            newMessage = new Message(messageType, sourceAddr, destinationType, destination, contentType, message, null,
                    messageId, false);
        }

        newMessage.setRawMimeContent(packet.getMimeType(), packet.getMimeTypeData());
        newMessage.setDisplayPictureGuid(picGuid);
        newMessage.setHotkeys(hotKeys);
        newMessage.setLongTimestamp(timestamp);
        newMessage.setDisplayName(displayName);
        newMessage.setContactID(contactId);
        newMessage.setFilename(filename);
        newMessage.setSourceColor(sourceColor);
        newMessage.setMessageColor(messageColor);
        newMessage.setGroupChatOwner(groupChatOwner);
        newMessage.setPrevMessageId(prevMessageId);
        newMessage.setEmoteContentType(emoteContentType);
        if (msgEventType == MessageStatusType.RECEIVED) {
            newMessage.setDeliveryStatus(DeliveryStatus.RECEIVED_BY_RECIPIENT);    
        }      
        newMessage.setPinnedType(pinnedType);
        
        return newMessage;
    }
    
    /**
     * Generates a unique ChatConversation id.
     * @param chatId           The chatId of the conversation.
     * @param imMessageType    The {@link MessageType} of the conversation.
     * @param destinationType  The {@link ChatDestinationType} of the conversation.
     * @return A combination of the chatId, imMessageType and destinationType.
     */
    public static String generateUniqueId(final String chatId, 
            final MessageType imMessageType,
            final ChatDestinationType destinationType) {
        return chatId + "_" + destinationType.toString() + "_" + imMessageType.toString();
    }
    
    /** get chat id from message destination, source and destinationType*/
    public static String getChatId(String destination, String source, ChatDestinationType destinationType) {
        String chatId = destination;
        String username = Session.getInstance().getUsername();
        if (destinationType == ChatDestinationType.PRIVATE && username != null && username.equals(destination)) {
            chatId = source;
        }
        
        return chatId;
    }
    
    public static String getConversationIdFromMsgStatusEvent(MessageStatusEvent msgStatusEvent) {
        String chatId = getChatId(msgStatusEvent.getDestination(), 
                msgStatusEvent.getSource(), msgStatusEvent.getDestinationType());
        String conversationId = ChatUtils.generateUniqueId(chatId, msgStatusEvent.getMessageType(),
                msgStatusEvent.getDestinationType());
        
        return conversationId;      
    }
    
    
    /**
     * Finds an existing {@link Message} in the {@link ChatDatastore} that
     * matches the message described by the given {@link FusionPktMessage}
     * 
     * @param messagePacket
     *            The {@link FusionPktMessage} to be matched.
     * @return The matching {@link Message} if found and null otherwise.
     */
    public static Message findExistingMessageMatchingPacket(final FusionPktMessage messagePacket) {
        if (messagePacket != null) {
            final Message message = ChatUtils.createMessageFromPacket(messagePacket);
            if (message != null) {
                return ChatDatastore.getInstance().findExistingMessage(message);
            }
        }

        return null;
    }

    /**
     *  used in fragments of selecting gift to check the whether is it to send a gift message
     *  in a private chat
     */
    public static boolean canUseSelectedUsersForPrivateChat(final ChatConversation conversation,
                                                            List<String> selectedUsers) {
        return ((conversation != null && conversation.isMigPrivateChat()) ||
                (selectedUsers != null && selectedUsers.size() == 1));
    }

    /**
     *  used in fragments of selecting gift to check the whether is it to send a gift message
     *  in a group chat
     */
    public static boolean canUseSelectedUsersForGroupChat(final ChatConversation conversation,
                                                            List<String> selectedUsers) {
        return ((conversation != null && conversation.isMigGroupChat()) ||
                (selectedUsers != null && selectedUsers.size() > 1));
    }

    /**
     *  used in fragments of selecting gift to check the whether is it to send a gift message
     *  in a chatroom
     */
    public static boolean canUseSelectedUsersForChatroom(final ChatConversation conversation) {
        return conversation != null && conversation.isChatroom();
    }


    public static String[] getHotkeysInString(String message) {
        ArrayList<String> hotkeyList = new ArrayList<String>();
        Set<String> hotkeySet = EmoticonDatastore.getInstance().getAllOwnEmoticonHotkeys();
        for(String hotkey : hotkeySet) {
            if (message.contains(hotkey)) {
                hotkeyList.add(hotkey);
            }
        }

        String[] hotkeys = null;
        if (hotkeyList.size() > 0) {
            hotkeys = new String[hotkeyList.size()];
            hotkeyList.toArray(hotkeys);
        }

        return hotkeys;

    }
}
