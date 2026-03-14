/**
 * Copyright (c) 2013 Project Goth
 * Message.java
 * 
 * Jun 10, 2013 4:59:44 PM
 */

package com.projectgoth.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeDataGenerator;
import com.projectgoth.b.data.mime.MimeType;
import com.projectgoth.b.data.mime.MimeTypeDataModel;
import com.projectgoth.blackhole.enums.ChatDestinationType;
import com.projectgoth.blackhole.enums.ContentType;
import com.projectgoth.blackhole.enums.EmoteType;
import com.projectgoth.blackhole.enums.MessageStatusType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.enums.PinnedType;
import com.projectgoth.blackhole.fusion.packet.FusionPktMessage;
import com.projectgoth.common.Config;
import com.projectgoth.common.Logger;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.nemesis.model.MessageStatusEvent;
import com.projectgoth.util.ChatUtils;
import com.projectgoth.util.CrashlyticsLog;
import com.projectgoth.util.mime.MimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents data for a single message in a chat conversation.
 * 
 * @author angelorohit
 */
public class Message extends MimeTypeDataModel {

    /**
     * {@link #messageDirection}
     */
    public enum MessageDirection {
        INCOMING((byte) 0), OUTGOING((byte) 1);

        private byte mValue;

        private MessageDirection(final byte value) {
            mValue = value;
        }

        public byte getValue() {
            return mValue;
        }
    }

    /**
     * An enum that represents the delivery status of an outgoing message.
     * @author angelorohit
     */
    public enum DeliveryStatus {
        /** The message was possibly never sent to the server. */
        UNKNOWN((byte)-2), 
        /** user is composing */
        COMPOSING((byte)1),  
        /** The message was or is being sent to the server and is awaiting an OK reponse. */
        SENDING((byte)-1),
        /** The message was successfully sent to the server and received an OK response. */
        SENT_TO_SERVER((byte)0), 
        /** The server sent the message to the recipient and informed the client. */
        RECEIVED_BY_RECIPIENT((byte)2), 
        /** the message read by the recipient. */
        READ_BY_RECIPIENT((byte)3), 
        
        /** The message failed to receive an OK response from the server. */
        FAILED((byte)-3);
        
        private byte value;
        
        private DeliveryStatus(byte value) {
            this.value = value;
        }
        
        public boolean isGreaterThanSentToServer() {
            if (this.value >= RECEIVED_BY_RECIPIENT.value)
                return true;
            
            return false;
        }
    }

    protected MessageType         messageType       = MessageType.FUSION;
    protected String              source;
    protected ChatDestinationType destinationType;
    protected String              destination;

    protected int                 contactID;

    protected ContentType         contentType;
    protected String              filename;

    /**
     * for MESSAGE_SMS; this will be the sms message for MESSAGE_TIMESTAMP; this
     * will be the timestamp for MESSAGE_SERVER_INFO; this will be the server
     * message for MESSAGE.FUSION or OTHERS; this will be the user's message
     */
    protected String              message;

    private byte[]                contentAsByteArray;

    /**
     * This is currently unused but will be preserved because it is part of the
     * Message Packet sent from the server
     **/
    protected byte                fromAdministrator;
    /** id for the profile picture to be displayed */
    protected String              displayPictureGuid;

    protected String[]            hotkeys           = null;
    protected int                 sourceColor       = -1;
    protected int                 messageColor      = -1;
    /**
     * This is currently unused but will be preserved because it is part of the
     * Message Packet sent from the server
     **/
    protected String              badgeHotkey;

    protected String              messageId;
    protected long                timestamp;
    protected String              displayName;
    protected String              groupChatOwner;
    /** Message ID of the message sent before this one */
    protected String              prevMessageId;
    protected EmoteType           emoteContentType  = EmoteType.PLAIN;

    /**
     * 0 = incoming; 1 outgoing
     */
    private MessageDirection      messageDirection  = MessageDirection.INCOMING;
    private String                messageChatIdentifier;
    private boolean               isServerInfo = false;

    protected boolean             hotkeysIdentified = true;
    
    protected PinnedType          pinnedType = PinnedType.NONE;
    
    /**
     * If true, then this message was retrieved from ChatMessagesDAO. If false,
     * assume this message was just sent/received within this session.
     */
    protected boolean             isLoadedFromCache;

    protected boolean             isPrevMessageFound;

    protected boolean             hasMentions;
    private   boolean             isLoadingPreviousMessages = false;
    
    private   DeliveryStatus      deliveryStatus;
    
    private   long                internalTimestamp; 

    private static String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    public enum Type {
        HIDDEN,
        NOTIFICATION,
        INCOMING,
        OUTGOING,
        INCOMING_GIFT,
        OUTGOING_GIFT,
        BANNER;
    }

    //@formatter:off
		/**
		 * Sets the main fields in Message
		 * 
		 * Automatically sets messageId if it's null 
		 * Automatically sets timestamp to the current time 
		 * Automatically sets displayName to match the destination name
		 * Automatically determines message direction based on source value
		 * Automatically sets emote content type enum to PLAIN
		 * 
		 * Other parameters that can be set after calling this constructor 
		 * - {@link #setGuid(String)} 
		 * - {@link #setTimestamp(long)} 
		 * - {@link #setDisplayName(String)} 
		 * - {@link #setContactID(int)} 
		 * - {@link #setGroupChatOwner(String)} 
		 * - {@link #setPrevMessageId(String)} 
		 * - {@link #setSourceColor(int)} 
		 * - {@link #setMessageColor(int)}
		 * 
		 * @param messageType
		 * @param sourceAddr
		 * @param destType
		 * @param destAddr
		 * @param contentType
		 * @param message
		 * @param messageId
		 */
		//@formatter:on
    public Message(final MessageType messageType, final String sourceAddr, final ChatDestinationType destType,
            final String destAddr, final ContentType contentType, final String message, final byte[] content,
            final String messageId, final boolean isServerInfo) {
        this.messageType = messageType;
        this.source = sourceAddr;
        this.destinationType = destType;
        this.destination = destAddr;
        this.contentType = contentType;
        this.contentAsByteArray = content;
        setMessage(message);

        if (messageId != null && messageId.length() > 0) {
            this.messageId = messageId;
        } else {
            this.messageId = generateMessageId();
        }

        this.isServerInfo = isServerInfo;

        init();
    }

    /**
     * Initialize message attributes
     */
    private void init() {

        setChatIdentifier();
        
        this.displayName = this.messageChatIdentifier;
        this.deliveryStatus = DeliveryStatus.UNKNOWN;

        // TODO: refactor this dependency
        this.timestamp = Tools.getClientTimestampBasedOnServerTime();
        this.emoteContentType = EmoteType.PLAIN;

        // Alter message type if chatroom name is the same as username: this
        // is the room generated message
        if (messageChatIdentifier != null && messageChatIdentifier.equals(source) && isPublic()) {
            isServerInfo = true;
        }

        // Check and set the proper message direction
        if (Session.getInstance().getUsername() != null && Session.getInstance().getUsername().equals(source)) {
            this.messageDirection = MessageDirection.OUTGOING;
        } else {
            this.messageDirection = MessageDirection.INCOMING;
        }
        
        // save the tempTimestamp for outgoing message
        if (isOutgoing()) {
            internalTimestamp = timestamp;
        }
        
        if (pinnedType == null) {
            pinnedType = PinnedType.NONE;
        }
         
    }

    /**
     * Returns the other party in this chat exchange Still possible to contain
     * the user's name for messages wherein user is both the sender and receiver
     * (e.g. system messages)
     * 
     * @return the chatIdentifier
     */
    public String getMessageChatIdentifier() {
        return messageChatIdentifier;
    }

    private void setChatIdentifier() {
        messageChatIdentifier = ChatUtils.getChatId(destination, source, 
                getDestinationType());
    }

    public MessageType getMessageType() {
        MessageType imMessageType = messageType;
        
        if (imMessageType == null || isSMS()) {
            imMessageType = MessageType.FUSION;
        }
        return imMessageType;
    }

    public void setMessageType(final MessageType messageType) {
        this.messageType = messageType;
    }

    public String getSender() {
        return source;
    }

    public void setSender(String sender) {
        this.source = sender;
    }

    public ChatDestinationType getDestinationType() {
        return destinationType;
    }

    public String getRecipient() {
        return destination;
    }

    public void setDestination(final String destination) {
        this.destination = destination;
    }

    public int getContactID() {
        return contactID;
    }

    public void setContactID(final int contactID) {
        this.contactID = contactID;
    }

    public ContentType getContentType() {
        return contentType != null ? contentType : ContentType.TEXT;
    }

    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public byte getFromAdministrator() {
        return fromAdministrator;
    }

    public void setFromAdministrator(final byte fromAdministrator) {
        this.fromAdministrator = fromAdministrator;
    }

    public String getDisplayPictureGuid() {
        return displayPictureGuid;
    }

    public void setDisplayPictureGuid(final String displayPictureGuid) {
        this.displayPictureGuid = displayPictureGuid;
    }

    public String[] getHotkeys() {
        return hotkeys;
    }

    public void setHotkeys(final String[] hotkeys) {
        this.hotkeys = hotkeys;
    }

    public int getSourceColor() {
        return sourceColor;
    }

    public void setSourceColor(final int sourceColor) {
        this.sourceColor = sourceColor;
    }

    public int getMessageColor() {
        return messageColor;
    }

    public void setMessageColor(final int messageColor) {
        this.messageColor = messageColor;
    }

    public String getBadgeHotkey() {
        return badgeHotkey;
    }

    public void setBadgeHotkey(final String badgeHotkey) {
        this.badgeHotkey = badgeHotkey;
    }

    public String getMessageId() {
        return messageId;
    }

    /**
     * Returns formatted version of the timestamp
     * 
     * @return
     */
    public String getFormattedTimestamp() {
        return Tools.getMessageDisplayTime(timestamp);
    }

    /**
     * Returns the actual timestamp
     * 
     * @return
     */
    public long getLongTimestamp() {
        return timestamp;
    }

    /**
     * Sets the actual timestamp
     * 
     * @param timestamp
     *            the timestamp to set
     */
    public void setLongTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Display name is automatically set when this Message object is created
     * When it's being set to a null or empty value, ignore it so that default
     * value is used instead
     * 
     * @param displayName
     */
    public void setDisplayName(final String displayName) {
        if (displayName != null && displayName.length() > 0) {
            this.displayName = displayName;
        }
    }

    public String getGroupChatOwner() {
        return groupChatOwner;
    }

    public void setGroupChatOwner(final String groupChatOwner) {
        this.groupChatOwner = groupChatOwner;
    }

    public String getPrevMessageId() {
        return prevMessageId;
    }

    public void setPrevMessageId(final String prevMessageId) {
        this.prevMessageId = prevMessageId;
    }

    public EmoteType getEmoteContentType() {
        return emoteContentType != null ? emoteContentType : EmoteType.PLAIN;
    }

    public void setEmoteContentType(final EmoteType emoteContentType) {
        this.emoteContentType = emoteContentType;
    }

    /**
     * @return the isLoadedFromCache
     */
    public boolean isLoadedFromCache() {
        return isLoadedFromCache;
    }

    /**
     * @param isLoadedFromCache
     *            the isLoadedFromCache to set
     */
    public void setLoadedFromCache(final boolean isLoadedFromCache) {
        this.isLoadedFromCache = isLoadedFromCache;
    }

    /**
     * @return the isPreviousMessageFound
     */
    public boolean isPreviousMessageFound() {
        return isPrevMessageFound;
    }

    /**
     * @param isPreviousMessageFound
     *            the isPreviousMessageFound to set
     */
    public void setPreviousMessageFound(final boolean isPreviousMessageFound) {
        this.isPrevMessageFound = isPreviousMessageFound;
    }

    public boolean isPublic() {
        return destinationType == ChatDestinationType.CHATROOM;
    }

    public boolean isGroup() {
        return destinationType == ChatDestinationType.GROUP_CHAT;
    }

    public boolean isPrivate() {
        return destinationType == ChatDestinationType.PRIVATE;
    }

    public boolean isDistributionList() {
        return destinationType == ChatDestinationType.DISTRIBUTION_LIST;
    }

    public boolean isFusionMessage() {
        return messageType == MessageType.FUSION;
    }

    public boolean isInfoMessage() {
        return isServerInfo;
    }

    public boolean isSMS() {
        return messageType == MessageType.SMS;
    }

    public boolean isIncoming() {
        return messageDirection == MessageDirection.INCOMING;
    }

    /**
     * here outgoing is actually decided if the source is myself, it can also be 
     * an incoming message of my own from another live client of mine or chat sync
     * */
    public boolean isOutgoing() {
        return messageDirection == MessageDirection.OUTGOING;
    }

    public boolean isIMChatMessage() {
        if (messageType == null) return false;
        switch(messageType.getImType())
        {
        case MSN:
        case AIM:
        case YAHOO:
        case ICQ:
        case GTALK:
        case FACEBOOK:
            return true;

        default:
            return false;
        }
    }

    public boolean isEmote() {
        return contentType == ContentType.EMOTE;
    }

    public boolean isStringContent() {
        return contentType == ContentType.TEXT;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    /**
     * Ensure the new message do contain a message of any kind, else ignore. Use
     * case: Emote attempt which gateway sends an empty string back (emote with
     * target etc etc)
     * 
     * @return
     */
    public boolean isValid() {
        return (!TextUtils.isEmpty(message) || (!isStringContent() && !isEmote()))
                && (Config.getInstance().isImEnabled() || isFusionMessage() || isInfoMessage() || isSMS());
    }
    
    public FusionPktMessage getMessagePacket() {
        FusionPktMessage packet = new FusionPktMessage();
        packet.setSource(source);
        packet.setDestination(destination);

        packet.setMessageType(messageType);
        packet.setChatType(destinationType);
        packet.setContentType(contentType);

        if (contentAsByteArray == null) {
            packet.setContent(message);
        } else {
            packet.setContentAsByteArray(contentAsByteArray);
        }

        //mime type and mime data
        String mimeTypes = getMimeType();
        String mimeDataJson = getMimeTypeData();
        if (!TextUtils.isEmpty(mimeTypes) && !TextUtils.isEmpty(mimeDataJson)) {
            packet.setMimeType(mimeTypes);
            packet.setMimeTypeData(mimeDataJson);
        }

        int srcColor = getSourceColor();
        if (srcColor != -1) {
            packet.setMessageColor(srcColor);
        }
        packet.setGuid(getMessageId());
        
        packet.setPinnedType(pinnedType);

        return packet;
    }

    /**
     * @return the contentAsByteArray
     */
    public byte[] getContentAsByteArray() {
        return contentAsByteArray;
    }

    /**
     * @param contentAsByteArray
     *            the contentAsByteArray to set
     */
    public void setContentAsByteArray(byte[] contentAsByteArray) {
        this.contentAsByteArray = contentAsByteArray;
    }

    public String toJsonString() {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new MimeUtils.MessageExclusionStrategy())
                .create();
        String json = gson.toJson(this);
        return json;
    }

    public static Message fromJsonString(final String jsonStr) {
        //WEIRD: some error json str is in MessageFormatTest.java, but they can pass tests....
        Message msg = null;
        try {
            msg = new Gson().fromJson(jsonStr, Message.class);
        } catch (AssertionError e) {
            CrashlyticsLog.log(e, "assertion error occurs when parse message " + jsonStr);
            //not raise exception, just log the error
        }
        return msg;
    }

    /**
     * @return
     */
    public boolean isMyOwnMessage() {
        if (source != null && source.equals(Session.getInstance().getUsername())) {
            return true;
        }
        return false;
    }

    public boolean hasOwnMention() {
        if (isStringContent()) {
            return Tools.stringContainsOwnMention(message);
        }

        return false;
    }
    
    public void setDeliveryStatus(final DeliveryStatus status) {
        setDeliveryStatus(status, false);
    }
    
    public void setDeliveryStatus(final DeliveryStatus status, boolean shouldSavePersistently) {
        this.deliveryStatus = status;
        if (shouldSavePersistently) {
            ChatDatastore.getInstance().saveChatMessageToPersistentStorage(getConversationId(), this);
        }
    }
    
    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }    
    
    public boolean hasFailed() {
        return this.deliveryStatus == DeliveryStatus.FAILED;
    }

    public void setLoadingPreviousMessages(boolean isLoading) {
        isLoadingPreviousMessages = isLoading;
    }
    
    public boolean isLoadingPreviousMessages() {
        return isLoadingPreviousMessages;
    }

    public String getConversationId() {
        String chatConversationId = ChatUtils.generateUniqueId(
                getMessageChatIdentifier(), 
                getMessageType(), 
                getDestinationType());
        return  chatConversationId;
    }

    public MessageStatusEvent getMessageStatusEvent(MessageStatusType eventType) {
        
        MessageStatusEvent event = new MessageStatusEvent(messageType, 
                source, 
                destinationType, 
                destination, 
                getMessageId(), 
                eventType, 
                false,
                timestamp);
        
        return event;
    }
    
    public boolean isBeingSent() {
        if (deliveryStatus == DeliveryStatus.SENDING) {
            return true;
        } else {
            return false;
        }
    }

    public long getInternalTimestamp() {
        return internalTimestamp;
    }

    /**
     * @param timestampDelta
     */
    public void updateTimestamp(long timestampDelta) {
        timestamp = internalTimestamp + timestampDelta;
        Logger.debug.log("MsgOrderIssue", "msg:", getMessage(), " ts:", timestamp, " internalTs:", internalTimestamp,
                " delta:", timestampDelta);
    }

    /**
     * estimate the timestamp of the message in sending status before it receives the timestamp from server
     * to keep the right order
     */
    public void estimateTimestampInSendingStatus() {
        ChatConversation conv = ChatDatastore.getInstance().getChatConversationWithId(getConversationId());
        if (conv != null) {
            long delta = conv.getTimestampDelta();
            if (delta != 0) {
                updateTimestamp(delta);
            }
            conv.sortMessages();
        }
    }

    public final Type getType() {
        MimeData mimeData = getFirstMimeData();
        if (mimeData != null) {
            final MimeType mimeType = mimeData.getMimeType();
            
            if (mimeType == MimeType.GIFT || mimeType == MimeType.EMOTE_GIFT) {
                return (MimeUtils.isIncomingGift((GiftMimeData) mimeData)) ? Type.INCOMING_GIFT : Type.OUTGOING_GIFT;
            } else if (mimeType == MimeType.SYSTEM_CHATROOM_DESCRIPTION ||
                       mimeType == MimeType.SYSTEM_CHATROOM_MANAGEDBY) {
                return Type.BANNER;
            } else if (mimeType == MimeType.SYSTEM_CHATROOM_HELP ||
                       mimeType == MimeType.SYSTEM_CHATROOM_PARTICIPANTS) {
                return Type.HIDDEN;
            } else if (isInfoMessage() || 
                       mimeType == MimeType.EMOTE ||
                       mimeType == MimeType.SYSTEM_CHATROOM_PARTICIPANT_ENTER ||
                       mimeType == MimeType.SYSTEM_CHATROOM_PARTICIPANT_EXIT) {
                return Type.NOTIFICATION;
            } else {
                return isIncoming() ? Type.INCOMING : Type.OUTGOING;
            }
        }
        
        return Type.HIDDEN;
    }

    /* (non-Javadoc)
     * @see com.projectgoth.b.data.mime.MimeTypeDataModel#generateMimeData()
     */
    @Override
    protected List<MimeData> generateMimeData() {
        final MimeDataGenerator generator = com.projectgoth.b.data.common.Config.getInstance().getMimeDataGenerator();
        if (generator != null) {
            return generator.generateForMessage(this);
        }
        
        return new ArrayList<MimeData>();
    }
    
    /**
     * Gets the color of this {@link Message} as a String.
     * @return  The color of this {@link Message} as a String in the format "RRGGBB".
     */
    public String getMessageColorStr() {
        if (getMessageColor() != -1) {
            return UIUtils.getRRGGBBString(getMessageColor());
        }
        
        return null;
    }

    /**
     * @return the pinnedType
     */
    public PinnedType getPinnedType() {
        return pinnedType;
    }
    
    /**
     * @param pinnedType the pinnedType to set
     */
    public void setPinnedType(PinnedType pinnedType) {
        if (pinnedType == null) {
            pinnedType = PinnedType.NONE;
        }
        this.pinnedType = pinnedType;
    }
    
    public boolean isMessagePinned() {
        return pinnedType == PinnedType.PINNED;
    }
    
    public boolean isMessageUnpinned() {
        return pinnedType == PinnedType.UNPINNED;
    }


    public boolean isFusionPrivateMessage() {
        if (isFusionMessage() && isPrivate()) {
            return true;
        }

        return false;
    }

    public boolean isStickerEmoteMessage() {
        if (getContentType() == ContentType.EMOTE &&
                getEmoteContentType() == EmoteType.STICKERS) {
            return true;
        }

        return false;
    }

    public boolean isGiftMessage() {
        MimeData data = getFirstMimeData();
        if (data.getMimeType() == MimeType.GIFT) {
            return true;
        }

        return false;
    }

    public boolean hasOneMimeData() {
        List<MimeData> mimeDataList = getMimeDataList();
        if (mimeDataList != null && mimeDataList.size() == 1) {
            return true;
        }

        return false;
    }

}
