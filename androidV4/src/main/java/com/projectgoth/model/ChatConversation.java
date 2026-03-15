/**
 * ChatConversation.java
 * 
 * Jun 10, 2013 2:32:22 PM
 */
package com.projectgoth.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.projectgoth.b.data.mime.ImageMimeData;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeType;
import com.projectgoth.blackhole.enums.ChatDestinationType;
import com.projectgoth.blackhole.enums.ChatParticipantType;
import com.projectgoth.blackhole.enums.ContentType;
import com.projectgoth.blackhole.enums.EmoteType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.common.Config;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ChatController;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.nemesis.model.ChatData;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.ChatUtils;
import com.projectgoth.util.mime.MimeUtils;

/**
 * Represents the data for a single chat conversation.
 * @author angelorohit
 */
public class ChatConversation {
    
    private static final String     LOG_TAG = AndroidLogger.makeLogTag(ChatConversation.class);
    
	/**
	 * An identifier for this chat conversation that is guaranteed to be unique.
	 * This id is a combination of {@link #chatId}, {@link #imMessageType} and {@link #destinationType}
	 */
	protected String id;
	
	//TODO: hide chat ID
	/** An identifier for this chat conversation based on the messages that are in it.
	 *  This identifier is not guaranteed to be unique.  
	 */
	protected String chatId;
	
	/** The name to be displayed for this chat. */
	protected String displayName;
	
	/** Indicates that the client has set the display name for this conversation 
	 * and that no display name has been received from the server.
	 */
	 //This is necessary because whenever new participants are added to the conversation, 
	 //the client needs to decide whether to update the display name or leave it as is.	 
	protected boolean didUseDefaultDisplayName;
	
	/**
	 * The type of this chat
	 * @see ChatTypeEnum
	 */
	protected ChatTypeEnum chatType;
	
	/** The {@link Message.IMMessageType} of this conversation */
	protected MessageType imMessageType;
	
	/** The {@link Message.DestinationType} of this conversation */
	protected ChatDestinationType destinationType;
	
	/** The number of unread messages in this conversation. */
	protected int unreadMessageCounter;		
	
	/** Indicates that the logged in user is joined in this conversation */
	protected boolean isJoined = false;
	
	/** The timestamp of this chat conversation */
	protected long chatTimestamp = Tools.getClientTimestampBasedOnServerTime(); 
	
	/** The display pic guid of the sender. */
    public String displayPicGuid = null;
    
    private String messageSnippet = null;

    private boolean isChecked;
    
    private Message pinnedMessage;
	
    /** Information if this conversation is a private chat.
     *  Note: this contact Id cannot be used for chat of external IM contact which can be changed
     *  */
    public static class PrivateChatInfo {

        public PrivateChatInfo(final int contactId) {
            this.contactId = contactId;
        }

        /** Contact ID of the person you are private chatting with. */
        private int contactId = -1;

        public int getContactId() {
            return contactId;
        }

        public void setContactId(final int value) {
            contactId = value;
        }
    }

    /** Information if this conversation is a group chat. */
    public static class GroupChatInfo {
        
        /**
         * This is an icon for the conversation if it is a group chat. A null value indicates that a
         * display icon made up of the chat participants has not been created yet.
         */
        private Bitmap displayIcon; 

        public GroupChatInfo(final String owner) {
            this.owner = owner;
            displayIcon = null;
        }

        /** The owner of the group chat. */
        private String owner = null;

        public String getOwner() {
            return owner;
        }

        public void setOwner(final String value) {
            owner = value;
        }
        
        public Bitmap getDisplayIcon() {
            return displayIcon;
        }
        
        public void setDisplayIcon(Bitmap displayIcon) {
            this.displayIcon = displayIcon;
        }
    }

    /** Information if this conversation is a public chat. */
    public static class PublicChatInfo {

        /** Group id of the group link to this conversation (if any) */
        private int groupId = 0;

        public PublicChatInfo(final int groupId) {
            this.groupId = groupId;
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(final int value) {
            groupId = value;
        }

    }
	
	// Depending on the type of chat, these members can be null.
    private PrivateChatInfo         privateChatInfo = null;
    private GroupChatInfo           groupChatInfo   = null;
    private PublicChatInfo          publicChatInfo  = null;
	
    /**
     * if the chat conversation never did chat sync, for example it is from server, it is false, otherwise true.
     * isSynced means it has got a couple of latest messages. It's true by default because if user clicks a 
     * contact to create ChatConversation, no need to sync it.
     */
    protected boolean               isConversationSynced = true;
    
    /**
     * when the chat conversation is shown for the first time after app launches, we do a sync 
     */
    protected boolean               isSyncedOnShow = false;
    
    protected String                latestViewedMsgId;

    /** A list of participants for this chat conversation. */
	protected List<ChatParticipant> participantList = new ArrayList<ChatParticipant>();
	
	protected ChatParticipantType   requestingUserType;
	
	/** 
	 * A list of messages in this chat conversation
	 * This list is always maintained sorted by timestamp 
	 */
	protected final @NonNull List<Message> messageList = new ArrayList<Message>();
	/**
	 * A Hashmap of messages has the same messages as the messageList, it is used for optimization 
	 * of getting a message by id
	 */
	protected final @NonNull HashMap<String, Message> messageHashMap = new HashMap<String, Message>();

	private boolean isPinned;

    private boolean isMuted;
	
	/**
	 * timestamp delta of outgoing message between the internal timestamp created from client and the one returned 
	 * from server. Used for keeping the sending messages in correct order before receiving the timestamp
	 * from server
	 */
	private long timestampDelta;
	
    /**
     * Constructor Set the private, group or public information separately.
     * 
     * @see ChatConversation#setPrivateChatInfo(PrivateChatInfo)
     * @see ChatConversation#setGroupChatInfo(GroupChatInfo)
     * @see ChatConversation#setPublicChatInfo(PublicChatInfo)
     */
    public ChatConversation(final String chatIdentifier, final String displayName, final String displayPicGuid,
            final ChatDestinationType destType, final MessageType imMessageType) {
        this.chatId = chatIdentifier;
        this.destinationType = destType;
        this.imMessageType = imMessageType;

        this.id = ChatUtils.generateUniqueId(chatId, imMessageType, destinationType);
        this.displayPicGuid = displayPicGuid;

        this.setChatType(ChatTypeEnum.determineChatType(destType, imMessageType));

        setDisplayName(displayName);

        Logger.debug.flog(LOG_TAG, "ChatConversation with IMMessageType: %s and DestinationType: %s",
                imMessageType, destinationType);
    }

    public ChatConversation(ChatData chatData) {
        setChatData(chatData);
        Logger.debug.flog(LOG_TAG, "ChatConversation with IMMessageType: %s and DestinationType: %s",
                imMessageType, destinationType);
    }
	
    public void setChatData(ChatData chatData) {
        if (chatData != null) {
            String chatId = chatData.getChatIdentifier();
            if (chatId != null) {
                this.chatId = chatId;
            }
            ChatDestinationType dType = chatData.getDestType();
            if (dType != null) {
                destinationType = dType;
            }

            MessageType imMsgType = chatData.getImMessageType();
            if (imMsgType != null) {
                imMessageType = imMsgType;
            }

            id = ChatUtils.generateUniqueId(this.chatId, imMessageType, destinationType);
            
            String guid = chatData.getGuid();
            if (guid != null) {
                displayPicGuid = guid;
            }

            ChatTypeEnum chType = chatData.getChatType();
            if (chType != null) {
                setChatType(chatData.getChatType());
            }

            setDisplayName(chatData.getDisplayName());

            setChatTimestamp(chatData.getTimestamp());
            
            setConversationSynced(chatData.isSynced());
            
            setChatInfoFromChatData(chatData);
            
            setLatestViewedMsgId(chatData.getLastViewedMessageId());
            
            setPinned(chatData.isPinned());

            setMuted(chatData.isMute());
            
        }
    }
    
    private void setChatInfoFromChatData(ChatData chatData) {
        if (chatData.isPrivate()) {
            setPrivateChatInfo(new PrivateChatInfo(chatData.getContactID()));
        } else if (chatData.isGroup()) {
            setGroupChatInfo(new GroupChatInfo(chatData.getGroupOwner()));
        } else {
            setPublicChatInfo(new PublicChatInfo(chatData.getGroupID()));
        }
    }
    
    /**
     * Constructs an instance of ChatData from this ChatConversation.
     * This routine is used to aggregate data for persistence.
     * @see      
     * @return  An object of ChatData.
     */
    public ChatData makeChatData() {
        ChatData chatData = new ChatData();
        chatData.setChatIdentifier(this.chatId);
        chatData.setImMessageType(this.imMessageType);
        chatData.setGuid(this.displayPicGuid);
        chatData.setChatType(this.getChatType());
        chatData.setDisplayName(this.displayName);
        chatData.setClosed(!this.isJoined);
        chatData.setTimestamp(this.chatTimestamp);
        chatData.setSynced(this.isConversationSynced);
        chatData.setLastViewedMessageId(this.latestViewedMsgId);
        chatData.setPinned(isPinned);
        chatData.setMute(isMuted);
        
        if (isPrivateChat() && privateChatInfo != null) {
            chatData.setContactID(privateChatInfo.getContactId());            
        } else if (isGroupChat() && groupChatInfo != null) {
            chatData.setGroupOwner(groupChatInfo.getOwner());
        } else if (publicChatInfo != null) {
            chatData.setGroupID(publicChatInfo.getGroupId());
        }
        
        return chatData;
    }
    
    /**
     * Sets the display name for this conversation.
     * 
     * @param name
     *            The new display name to be set.
     */
    public void setDisplayName(final String name) {
        // Use a default name, if no name is available.
        if (TextUtils.isEmpty(name)) {
            displayName = getDefaultDisplayName();
            didUseDefaultDisplayName = true;
        } else {
            displayName = name;
            didUseDefaultDisplayName = false;
        }
    }
	
    /**
     * Constructs a default display name for this conversation. Note: This
     * routine is only to be called if a display name was not sent by the
     * server.
     * 
     * @return A string of the default display name.
     */
    protected String getDefaultDisplayName() {
        // By default, use chatId for public and private chats.
        String result = chatId;

        if (isGroupChat()) {
            // Use a comma separated string of participants for a group chat.
            final String participantsStr = getConcatenatedParticipantNames();
            if (TextUtils.isEmpty(participantsStr)) {
                result = I18n.tr("Group chat");
            } else {
                result = participantsStr;
            }
        }

        return result;
    }
    
    /**
     * Returns the number of total participants in this chat conversation
     * @param   shouldExcludeCurrentUser Whether the currently logged in user should be excluded from the count.
     * @return  Number of total participants in the chat.
     */
    public int getParticipantsCount(final boolean shouldExcludeCurrentUser) {
        synchronized (participantList) {
            boolean hasCurrentlyLoggedInUser = false;
            final String currentlyLoggedInUsername = Session.getInstance().getUsername();
            if (shouldExcludeCurrentUser && !TextUtils.isEmpty(currentlyLoggedInUsername)) {
                for (ChatParticipant participant : participantList) {
                    if (participant.getUsername().equals(currentlyLoggedInUsername)) {
                        hasCurrentlyLoggedInUser = true;
                        break;
                    }
                }
            }
                        
            return (hasCurrentlyLoggedInUser) ? participantList.size() - 1 : participantList.size();
        }
    }

	/**
	 * Get the list of participant users in this chat conversation.
	 * @param shouldExcludeCurrentUser Indicates whether the currently logged in user should be excluded from the return list.
	 * @return A copy of the list of ChatParticipant {@link #participantList}
	 */
	public List<ChatParticipant> getParticipants(final boolean shouldExcludeCurrentUser) {	    
	    	    
	    List<ChatParticipant> resultList;
	    
	    synchronized (participantList) {
	        resultList = new ArrayList<ChatParticipant>(participantList);	        
        }			    
	    
	    final String currentlyLoggedInUsername = Session.getInstance().getUsername();
	    if (shouldExcludeCurrentUser && !TextUtils.isEmpty(currentlyLoggedInUsername)) {
            for (ChatParticipant participant : resultList) {
                if (participant.getUsername().equals(currentlyLoggedInUsername)) {
                    resultList.remove(participant);
                    break;
                }
            }
        }
	    
	    return resultList;
	}
	
	/**
	 * Sets the participants of the conversation
	 * @param participantList	A list of ChatParticipant to set.
	 */
	public void setParticipants(final List<ChatParticipant> participantList) {	
	    synchronized (participantList) {
	        this.participantList = participantList;
	        
            // Update the display name of the conversation if
            // no display name was received from the server.
            if (didUseDefaultDisplayName) {
                setDisplayName(null);
            }
	    }
	}
	
	/**
	 * Returns a concatenated string of the participant user names in the conversation.
	 * This will be used to set a default group display name if one is not provided by the server.
	 * @return A delimited string containing the names of the participants in the conversation.
	 */
	public String getConcatenatedParticipantNames() {				
	    synchronized (participantList) {
            String delim = "";
            StringBuilder strBuilder = new StringBuilder();
            for (ChatParticipant participant : participantList) {
                strBuilder.append(delim).append(participant.getUsername());
                delim = ", ";
            }
            
            return strBuilder.toString();
        }
	}
	
	/**
	 * Adds a participant to this conversation.
	 * @param participant	The {@link ChatParticipant} to be added to the conversation
	 * @return	true if the participant was added to the conversation and false otherwise
	 */
	public boolean addParticipant(final ChatParticipant participant) {
	    
        final String participantUsername = participant.getUsername();
        
        synchronized (participantList) {
    		// Early break: Check if the participant is already in the list of participants.
    		for (ChatParticipant user : participantList) {
    			if (user.getUsername().equals(participantUsername)) {
    				return false;
    			}
    		}
    		
    		boolean found = false;
    		int idx = 0;
    		// for adding a user alphabetically
    		for (ChatParticipant user : participantList) {
    			if (participantUsername.compareTo(user.getUsername()) < 0) {
    				found = true;
    				break;
    			}
    			idx++;
    		}
    		
    		if (found) {
    			this.participantList.add(idx, participant);
    		} else {
    			this.participantList.add(participant);
    		}
	    }
		
		// Update the display name of the conversation if
		// no display name was received from the server.
		if (didUseDefaultDisplayName) {
			setDisplayName(null);
		}
		
		return true;
	}
	
	/**
	 * Removes a participant from the conversation.
	 * @param username The name of the participant to be added to the conversation.
	 */
	public void removeParticipant(final String username) {			    
		int index = 0;
		synchronized (participantList) {
    		for (ChatParticipant user : participantList) {
    			if (user.getUsername().equals(username)) {
    				participantList.remove(index);
    				break;
    			}
    			index++;
    		}
		}
		
		// Update the display name of  the conversation if
		// no display name was received from the server.
		if (didUseDefaultDisplayName) {
			setDisplayName(null);
		}
	}
	
	/**
	 * Get the number of unread messages in this conversation
	 * @return {@link #unreadMessageCounter}
	 */
	public int getUnreadMessageCounter() {
		return unreadMessageCounter;
	}
	
	/**
	 * Increases the unread messages in this conversation
	 * @param value The amount by which to increase the {@link #unreadMessageCounter}
	 */
	public void incrementUnreadMessageCounter(final int value) {
		unreadMessageCounter += value;
	}
	
	/**
	 * Resets the unread messages in this conversations
	 * {@link #unreadMessageCounter}
	 */
	public void resetUnreadMessageCounter() {
		unreadMessageCounter = 0;
	}
	
	public boolean isMigPrivateChat() {
		return getChatType() == ChatTypeEnum.MIG_PRIVATE;
	}
	
	public boolean isIMChat() {
		return getChatType() == ChatTypeEnum.IM;
	}
	
	public boolean isIMGroupChat() {
		return getChatType() == ChatTypeEnum.IMGROUP;
	}
	
	public boolean isMigGroupChat() {
		return getChatType() == ChatTypeEnum.MIG_GROUP;
	}
	
	public boolean isChatroom() {
		return getChatType() == ChatTypeEnum.CHATROOM;
	}
	
	public boolean isPrivateChat() {
		return (isMigPrivateChat() || isIMChat());
	}
	
	public boolean isGroupChat() {
		return (isMigGroupChat() || isIMGroupChat());
	}
	
	/**
	 * Gets a copy of the messages in this conversation
	 * @return A list of Message
	 */
	public final @NonNull List<Message> cloneMessageList() {
	    synchronized (messageList) {
	        return new ArrayList<Message>(messageList);
	    }
	}	
	
	/**
	 * Sorts the messages of message list according to the comparator set for Message.
	 * @param messageList A list of {@link Message} to be sorted.
	 */
	private void sortMessages(List<Message> messageList) {
		Collections.sort(messageList, new Comparator<Message>() {
			
			@Override
			public int compare(final Message lhs, final Message rhs) {
				final long lhsTimestamp = lhs.getLongTimestamp();
				final long rhsTimestamp = rhs.getLongTimestamp();
				
				return (rhsTimestamp > lhsTimestamp) ? -1 : 
					(rhsTimestamp < lhsTimestamp) ? 1 : 0;
			}
		});
	}
	
	public void sortMessages() {
	    synchronized (messageList) {
            sortMessages(messageList);
        }
    }
	
	
	/**
	 * Gets a messages with a specific message id.
	 * @param messageId	The id of the message to be retrieved from this conversation.
	 * @return The matching message in this conversation and null if the message was not found.
	 */
	public Message getMessage(final String messageId) {
	    synchronized (messageList) {
	        if (messageId == null) {
                return null;
            }
	        Message message = messageHashMap.get(messageId);
    		return message;
	    }		
	}
	
	public boolean hasMessage(final String msgId) {
	    if (msgId != null && getMessage(msgId) != null) {
            return true;
        } else {
            return false;
        }
	}
	
	public Message getPreviousMessage(Message msg) {
	    synchronized (messageList) {
	        int pos = getPositionOfMessage(msg);
	        if (pos > 0) {
	            Message preMessage = messageList.get(pos - 1);
	            return preMessage;
	        } else {
	            return null;
	        }
	    }	    
	}
	
	
	public Long getPreviousMessageTimestamp(Message msg) {
	    Long start = null;
        Message oldMessage = getPreviousMessage(msg);
        if (oldMessage != null) {
	        start = Long.valueOf(oldMessage.getLongTimestamp());
	    }
        return start;
    }
	
	/**
	 * Gets the message with the oldest timestamp in this conversation.
	 * @return The appropriate message or null if there are no messages in this conversation
	 */
	public Message getOldestMessage() {
	    synchronized (messageList) {
	        return (messageList.size() <= 0) ? null : messageList.get(0);
	    }
	}
	
	/**
	 * Gets the message with the most recent timestamp in this conversation.
	 * @return The appropriate message or null if there are no messages in this conversation
	 */
	public Message getMostRecentMessage() {
	    //TODO: currently if we send message when it's disconnected, the message can be sent and stored without 
	    //any UI feedback. And the timestamp of that message sent will be considered to the most recent message
	    // also and used to compare to the timestamp of the conversation from LatestMessageDiguest packet when
	    // the client is connected again, which is a problem
	    synchronized (messageList) {
	        return (messageList.size() <= 0) ? null : messageList.get(messageList.size() - 1);
	    }
	}

    public Message getMostRecentMessage(boolean excludingServerInfoMessage) {
        synchronized (messageList) {
            if (messageList.size() <= 0) {
                return null;
            }
            
            if (excludingServerInfoMessage) {
                for(int i = messageList.size() - 1; i >= 0; i--) {
                    if (!messageList.get(i).isInfoMessage()) {
                        return messageList.get(i);
                    }
                }
            } else {
                return messageList.get(messageList.size() - 1);
            }
            
            return null;
        }
        
    }
	
	
	/**
	 * Checks to see if there are more messages to load from storage or fetch from server.
	 * @return true if there are more messages to be loaded or fetched and false otherwise.
	 */
	public boolean hasOlderMessages() {
		final Message message = getOldestMessage();
					
		return (message == null || message.getPrevMessageId() == null
					|| message.getPrevMessageId().length() > 0) ? false : true;
	}
	
	/**
	 * Adds a message to this conversation
	 * @param message					The message to be added
	 * @param shouldIncrementCounter	Whether the unread counter for this conversation should be increased or not.
	 * @return	true if the message was added to this conversation and false if the message was already in the conversation. 
	 */
	public boolean addMessage(Message message, final boolean shouldIncrementCounter) {
		
		// check if duplicate so we know if counter must be increased
		boolean isNewMessage = false;
		
		Message mostRecentMsg = getMostRecentMessage();
		//increase counter if it is a new message not an old message from chat sync
		if(mostRecentMsg == null || mostRecentMsg.getLongTimestamp() < message.getLongTimestamp()) {
		    isNewMessage = true;
		}
		
		synchronized (messageList) {
		    Message existingMsg = getMessage(message.getMessageId());
		    if (existingMsg == null) {
		        messageList.add(message);
		    } else {
                replaceMessageInMessageList(message);
            }
		    
		    sortMessages(messageList);
			messageHashMap.put(message.getMessageId(), message);
		}			
			
        Logger.debug.log(LOG_TAG, "Adding message: ", message.getMessage(),
                ", time: ", Tools.getMessageDisplayTime(message.getLongTimestamp()));
		
		
		if (shouldIncrementCounter && isNewMessage) {
			incrementUnreadMessageCounter(1);			
		}
		
		return isNewMessage;
	}

    /**
     * remove specific message from this conversation
     *
     * @param message message object to be removed
     */
    public void removeMessage(Message message) {
        synchronized (messageList) {
            for (Message msg : messageList) {
                if (msg.getMessageId().equals(message.getMessageId())) {
                    messageList.remove(msg);
                    break;
                }
            }
        }
    }
	
	/**
     * @param message
     */
    private void replaceMessageInMessageList(Message message) {
        synchronized (messageList) {
            for (Message msg : messageList) {
               if (msg.getMessageId().equals(message.getMessageId())) {
                   messageList.remove(msg);
                   messageList.add(message);
                   break;
               } 
            }
        }
    }

    public void addMessageFromStorage(Message message) {
	    if (getMessage(message.getMessageId()) == null) {
            synchronized (messageList) {
                messageList.add(message);
                sortMessages(messageList);
                messageHashMap.put(message.getMessageId(), message);
            }           
        }
	}
	
	public ChatTypeEnum getChatType() {
		return chatType;
	}
	
	public MessageType getImMessageType() {
		return imMessageType;
	}
	
	public void setImMessageType(MessageType imMessageType) {
         this.imMessageType = imMessageType;
    }
	
	public ChatDestinationType getDestinationType() {
		return destinationType;
	}
	
	public String getId() {
	    return id;
	}
	
	public String getChatId() {
		return chatId;
	}
	
	public void setChatId(final String id) {
		chatId = id;
	}
	
	public PublicChatInfo getPublicChatInfo() {
		return publicChatInfo;
	}
	
	public GroupChatInfo getGroupChatInfo() {
		return groupChatInfo;
	}
	
	public PrivateChatInfo getPrivateChatInfo() {
		return privateChatInfo;
	}
	
	public void setPublicChatInfo(final PublicChatInfo publicChatInfo) {
	    this.publicChatInfo = publicChatInfo;
	}
	
	public void setGroupChatInfo(final GroupChatInfo groupChatInfo) {
	    this.groupChatInfo = groupChatInfo;
	}
	
	public void setPrivateChatInfo(final PrivateChatInfo privateChatInfo) {
	    this.privateChatInfo = privateChatInfo;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public long getChatTimestamp() {
		final Message message = getMostRecentMessage();
		if (message != null) {
			return message.getLongTimestamp();
		}
		
		return chatTimestamp;
	}
	
	public void setChatTimestamp(final long timestamp) {
		if (timestamp > 0) {
			chatTimestamp = timestamp;
		}
	}
	
    public long getOldestMessageTimestamp() {
        Message msg = getOldestMessage();
        return msg == null ? -1 : msg.getLongTimestamp();
    }
	
	public boolean isConversationSynced() {
		return isConversationSynced;
	}
	
	public void setConversationSynced(final boolean state) {
		isConversationSynced = state;
	}
	
    public boolean isSyncedOnShow() {
        return isSyncedOnShow;
    }

    public void setSyncedOnShow(boolean isSyncedOnShow) {
        this.isSyncedOnShow = isSyncedOnShow;
    }
    
	public boolean isJoined() {
		return isJoined;
	}
	
	public void setJoined(final boolean joined) {
		this.isJoined = joined;				
	}
	
	public String getLatestViewedMsgId() {
        return latestViewedMsgId;
    }

    
    public void setLatestViewedMsgId(String latestViewedMsgId) {
        this.latestViewedMsgId = latestViewedMsgId;
    }

    public Message createPhotoMessage(String url, String thumbnailUrl) {
        //the content type is TEXT here for legacy devices don't support mime data,
        //so that they will just show the link as text message
        Message msg = createTextMessage(url);
        MimeData imageMimeData = ImageMimeData.createFromUrl(url, thumbnailUrl);
        //set raw json data when creating MimeData so that they can be used when saving the msg to db & send it to server
        msg.setRawMimeContent(imageMimeData.getMimeType().getValue(), imageMimeData.toJson());

        msg.addMimeData(imageMimeData);
        return msg;
    }

    public Message createShareMessage(String text, ArrayList<MimeData> mimeDataList) {
        //the content type is TEXT here for legacy devices don't support mime data,
        //so that they will just show the link as text message
        Message msg = createTextMessage(text);

        if (mimeDataList != null && mimeDataList.size() > 0) {
            if (mimeDataList.size() == 1) {
                MimeData data = mimeDataList.get(0);
                msg.setRawMimeContent(data.getMimeType().getValue(), data.toJson());
                msg.addMimeData(data);
            } else {
                msg.setRawMimeContent(MimeType.MULTIPART.getValue(), MimeUtils.convertMimeDataToJson(mimeDataList));
                for (MimeData data : mimeDataList) {
                    msg.addMimeData(data);
                }
            }
        }

        return msg;
    }

    public Message createTextMessage(String text) {
        Message msg = new Message(imMessageType, Session.getInstance().getUsername(), destinationType, getChatId(),
                ContentType.TEXT, text, null, null, false);
        if(this.isIMChat()) {
            msg.setDisplayName(this.displayName);
        }
        return msg;
    }
    
    public Message createStickerMessage(String messageId, String text, Sticker sticker) {
        Message newMessage = new Message(imMessageType, Session.getInstance().getUsername(), destinationType,
                getChatId(), ContentType.EMOTE, text, null, messageId, false);
        newMessage.setHotkeys(new String[] { sticker.getMainHotkey() });
        newMessage.setDisplayName(displayName);
        newMessage.setEmoteContentType(EmoteType.STICKERS);
        return newMessage;
    }

    public String getTitle() {
        String title = getDisplayName();
        return title;
    }

    public long getTimestamp() {
        return getChatTimestamp();
    }    
    
    public int getPositionOfMessage(Message message) {
        synchronized (messageList) {
            return messageList.lastIndexOf(message);
        }
    }

    /**
     * @param i
     * @return
     */
    public Message getMessageAtPostion(int i) {
        synchronized (messageList) {
            if (i >= 0 && i < messageList.size()) {
                return messageList.get(i);    
            } else {
                return null;
            }
        }
    }
    
    /**
     * @param chatType the chatType to set
     */
    public void setChatType(ChatTypeEnum chatType) {
        this.chatType = chatType;
    }

    /**
     * 
     */
    public void resetLoadingStateOfMessage(String messageId) {
        Message message = getMessage(messageId);
        if (message != null) {
            if (message.isLoadingPreviousMessages()) {
                // Logger.debug.log("Dangui", "setLoadingPreviousMessages(false) - " + message.getMessage());
                message.setLoadingPreviousMessages(false);
            }
        }
    }
    
    public int getChatSyncLimit() {
        if (isGroupChat()) {
            return Config.getInstance().getChatSyncGroupChatMsgRequestLimit();
        } else {
            return Config.getInstance().getChatSyncMessagesRequestLimit();
        }
    }

    /**
     * @return
     */
    public Message getLatestViewedMessage() {
        Message latestViewedMsg = getMessage(getLatestViewedMsgId());
        return latestViewedMsg;
    }
    
    public void fetchMessageForNewConversation() {
        synchronized(messageList) {
            if (messageList.size() == 0) {
                ChatController.getInstance().sendGetMessagesForNewChat(id);
            }
        }
    }

    public String getMessageSnippet() {
        return messageSnippet;
    }

    public void setMessageSnippet(String messageSnippet) {
        this.messageSnippet = messageSnippet;
    }

    public boolean hasUnreadMessage() {
        if (unreadMessageCounter > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }

    public boolean isMuted() { return isMuted; }

    public void setMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    /**
     * @param timestampDelta
     */
    public void estimateTimestampsOfMessageBeingSent(long timestampDelta) {
        boolean hasMessageSending = false;
        for (Message message : messageList) {
            //adjust the message in sending status
            if (message.isBeingSent()) {
                message.updateTimestamp(timestampDelta);
                if (!hasMessageSending) {
                    hasMessageSending = true;
                }
            }
        }
        
        if (!hasMessageSending) {
            this.timestampDelta = 0;
        } else {
            this.timestampDelta = timestampDelta;
            //sort message here after the estimating
            sortMessages();
        }
    }

    public long getTimestampDelta() {
        return timestampDelta;
    }

    public void setTimestampDelta(long timestampDelta) {
        this.timestampDelta = timestampDelta;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
    
    /**
     * @return the pinnedMessage
     */
    public Message getPinnedMessage() {
        return pinnedMessage;
    }
    
    /**
     * @param pinnedMessage the pinnedMessage to set
     */
    public void setPinnedMessage(Message pinnedMessage) {
        this.pinnedMessage = pinnedMessage;
    }

    public boolean hasPinnedMessage() {
        return pinnedMessage != null;
    }
    
    /**
     * @return the requestingUserType
     */
    public ChatParticipantType getRequestingUserType() {
        return requestingUserType;
    }
    
    /**
     * @param requestingUserType the requestingUserType to set
     */
    public void setRequestingUserType(ChatParticipantType requestingUserType) {
        this.requestingUserType = requestingUserType;
    }
    
    public boolean isChatRoomAdmin() {
        if (getRequestingUserType() != null && 
                getRequestingUserType() == ChatParticipantType.ADMINISTRATOR) {
            return true;
        }
        return false;
    }
    
}