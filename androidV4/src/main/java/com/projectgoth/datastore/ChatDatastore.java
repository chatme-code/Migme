/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatDatastore.java
 * Created Jun 3, 2013, 12:03:13 PM
 */

package com.projectgoth.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.text.TextUtils;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.ChatroomInfo;
import com.projectgoth.blackhole.enums.ChatDestinationType;
import com.projectgoth.blackhole.enums.ChatParticipantType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.common.Logger;
import com.projectgoth.controller.ChatController;
import com.projectgoth.dao.ChatDAO;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.nemesis.enums.FriendGroupTypeEnum;
import com.projectgoth.nemesis.listeners.GetCategorizedChatRoomItemsListener;
import com.projectgoth.nemesis.listeners.GetChatroomCategoriesListener;
import com.projectgoth.nemesis.listeners.GetChatroomInfoListener;
import com.projectgoth.nemesis.model.ChatData;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.nemesis.model.ChatRoomCategory;
import com.projectgoth.nemesis.model.ChatRoomItem;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.service.NetworkService;
import com.projectgoth.util.ChatUtils;

/**
 * The chat data store manages caching and persistent storage of all chat
 * related data. This includes data for individual chat conversations and data
 * for a list of public chat rooms ordered by category.
 * 
 * @author angelorohit
 */
public class ChatDatastore extends BaseDatastore {

    private static final String             TAG                        = "ChatDatastore";

    // A lock that is obtained when working with any of the caches.
    private static final Object             CACHE_LOCK                 = new Object();

    private static final String             CHAT_LIST_VERSION          = "ChatDatastore.CHAT_LIST_VERSION";
    private static final String             CHAT_LIST_TIMESTAMP        = "ChatDatastore.CHAT_LIST_TIMESTAMP";

    private ChatDAO                         mChatDAO                   = null;

    private ChatNotification                chatNotif;

    /**
     * A cache containing all chat room categories. The key is the id of the
     * ChatRoomCategory.
     */
    protected Map<Short, ChatRoomCategory>  mChatRoomCategoryCache;

    /**
     * A cache containing all chat conversations. The key is the id of the
     * ChatConversation.
     */
    protected Map<String, ChatConversation> mChatConversationCache;

    /**
     * add when user request to leave, remove when user received confirmation
     * from server
     */
    private ConcurrentLinkedQueue<String>   chatRoomLeft;

    /**
     * add when user request to leave, remove when user received confirmation
     * from server
     */
    private ConcurrentLinkedQueue<String>   groupChatLeft;

    // The number of messages to be loaded from persistent storage at a time.
    private static int                      DEFAULT_LOAD_MESSAGE_COUNT = 20;

    private ConcurrentLinkedQueue<Message> mFailedMessageQueue = new ConcurrentLinkedQueue<Message>();


    /**
     * A cache containing message snippet from LDM
     */
    private Map<String, String> mChatMsgSnippetCache;


    /**
     *  cache of complete chatroom info
     */
    private DataCache<ChatroomInfo> mChatroomInfoCache;
    private static final int CHATROOM_INFO_CACHE_SIZE = 10;

    /**
     *  conversationId list, record when user clicks "Leave Chat" in private chat
     */
    private List<String> mLeavePrivateChatList = new ArrayList<>();

    /**
     * @author warrenbalcos
     */
    public static class ChatNotification {

        private String message;
        private String url;

        /**
         * @param message
         * @param url
         */
        public ChatNotification(String message, String url) {
            super();
            this.message = message;
            this.url = url;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the url
         */
        public String getUrl() {
            return url;
        }
    }

    //20150112 freddie.w:
    // rewrite the singleton by lazy holder.
    // in original design, INSTANCE may not be initialized when handling notification.
    // use lazy holder to avoid this issue.
    private static class ChatDatastoreHolder {
        private static final ChatDatastore INSTANCE = new ChatDatastore();
    }
    
    private GetChatroomCategoriesListener getChatRoomCategoriesListener = new GetChatroomCategoriesListener() {

        @Override
        public void onChatRoomCategoryReceived(final ChatRoomCategory chatRoomCategory) {
            if (chatRoomCategory != null) {
                cacheChatRoomCategory(chatRoomCategory);
                BroadcastHandler.ChatRoomCategory.sendReceived(chatRoomCategory.getID());

                // Send a request to get all the ChatRoomItems in
                // the category that was received.                            
                requestChatRoomItemsForCategoryWithId(chatRoomCategory.getID(), true);
            }
        }
        
        @Override
        public void onFavouriteChatRoomAdded(final ChatRoomItem chatRoomItem, final short categoryId) {
            synchronized (CACHE_LOCK) {                    
                final ChatRoomCategory chatRoomCategory = mChatRoomCategoryCache.get(categoryId);
                if (chatRoomCategory != null) {
                    chatRoomCategory.addChatRoomItemAtPosition(0, chatRoomItem, UIUtils.getChatroomColorArray());

                    BroadcastHandler.ChatRoom.sendFavourited(chatRoomItem.getName(), categoryId);
                }
            }
        }

        @Override
        public void onFavouriteChatRoomRemoved(final String chatRoomName, final short categoryId) {
            if (!TextUtils.isEmpty(chatRoomName)) {
                synchronized (CACHE_LOCK) {
                    final ChatRoomCategory chatRoomCategory = mChatRoomCategoryCache.get(categoryId);
                    if (chatRoomCategory != null) {
                        chatRoomCategory.removeChatRoomItemWithName(chatRoomName);
                        BroadcastHandler.ChatRoom.sendUnfavourited(chatRoomName, categoryId);
                    }
                }
            }
        }

        @Override
        public void onGetChatRoomCategoriesComplete() {
            BroadcastHandler.ChatRoomCategory.sendFetchAllCompleted();
        }


        @Override
        public void onGetChatRoomCategoriesError(MigError error) {
            BroadcastHandler.ChatRoomCategory.sendFetchAllError(error);
        }

        @Override
        public void onAddFavouriteChatRoomError(MigError error, String chatRoomName) {
            BroadcastHandler.ChatRoom.sendFavouritedError(error, chatRoomName);
        }

        @Override
        public void onRemoveFavouriteChatRoomError(MigError error, String chatRoomName, short removedFromCategoryId) {
            BroadcastHandler.ChatRoom.sendUnfavouritedError(error, chatRoomName);
        }
    };

    public static synchronized ChatDatastore getInstance() {
        return ChatDatastoreHolder.INSTANCE;
    }
	
	private ChatDatastore() {
	    super();
	    
	    final Context appCtx = ApplicationEx.getContext();
        if (appCtx != null) {
            mChatDAO = new ChatDAO(appCtx);
        }
        
        loadFromPersistentStorage();
	}
	
	@Override
	protected void initData() {
	    synchronized (CACHE_LOCK) {
	        mChatRoomCategoryCache = new LinkedHashMap<Short, ChatRoomCategory>();
	        mChatConversationCache = new HashMap<String, ChatConversation>();
	        mChatMsgSnippetCache = new HashMap<String, String>();
	        
	        chatRoomLeft = new ConcurrentLinkedQueue<String>();
	        groupChatLeft = new ConcurrentLinkedQueue<String>();
        }

        mChatroomInfoCache = new DataCache<ChatroomInfo>(CHATROOM_INFO_CACHE_SIZE);
	}
	
	@Override
    public void clearData() {
	    super.clearData();

        clearChatListVersion();
        clearChatListTimestamp();

        if (mChatDAO != null) {
            mChatDAO.clearIndex();
            mChatDAO.clearTables();
        }

        initData();
	}

	/**
	 * Retrieve a cached ChatConversation
	 * @param chatConversationId	The chat id of a ChatConversation to be retrieved.
	 * @return The ChatConversation if found in cache and null otherwise.
	 * @see ChatConversation#getId()
	 */
    public ChatConversation getChatConversationWithId(final String chatConversationId) {
        ChatConversation chatConversation = null;
        if (chatConversationId != null) {
            // find it in memory
            chatConversation = getChatConversationWithIdFromMemoryCache(chatConversationId);
            
            if (chatConversation == null) {
                // find it in db
                chatConversation = loadChatFromStorage(chatConversationId);
                
                if (chatConversation != null) {
                    // cache it in memory
                    cacheChatConversation(chatConversation, false);
                    
                    // load message of the chat from storage
                    loadLatestMessagesOfChatFromDB(chatConversation);
                }
            }
        }
        return chatConversation;	    
	}
    
    public ChatConversation getChatConversationWithIdFromMemoryCache(final String id) {
        synchronized (CACHE_LOCK) {
            return mChatConversationCache.get(id);
        }       
    }
	
	/**
	 * Removes a cached ChatConversation that matches the specified id.
	 * @param id               The id of the ChatConversation to be removed from cache.
	 * @param shouldPersist    Indicates whether the modified ChatConversation should be persisted to storage. 
	 * @see ChatConversation#getId()
	 */
	private void removeChatConversationWithId(final String id, final boolean shouldPersist) {
	    synchronized (CACHE_LOCK) {
            ChatConversation chatConversation = mChatConversationCache.remove(id);
            if (chatConversation != null && shouldPersist) {
                if (!removeChatConversationFromPersistentStorage(chatConversation)) {
                    Logger.error.log(TAG, "Failed to persist with removeChatConversationFromPersistentStorage");
                }
            }
        }
    }

    /**
     * Get all cached ChatConversations
     * 
     * @return A List containing all cached ChatConversations.
     */
    public List<ChatConversation> getAllChatConversations() {
        synchronized (CACHE_LOCK) {
            return new ArrayList<ChatConversation>(mChatConversationCache.values());
        }
    }

    public void setChatListTimestamp(long timestamp) {
        SystemDatastore.getInstance().saveData(CHAT_LIST_TIMESTAMP, timestamp);
    }

    public long getChatListTimestamp() {
        return SystemDatastore.getInstance().getLongData(CHAT_LIST_TIMESTAMP);
    }

    public void clearChatListTimestamp() {
        SystemDatastore.getInstance().clearData(CHAT_LIST_TIMESTAMP);
    }

    public void setChatListVersion(int version) {
        SystemDatastore.getInstance().saveData(CHAT_LIST_VERSION, version);
    }

    public int getChatListVersion() {
	    return SystemDatastore.getInstance().getIntegerData(CHAT_LIST_VERSION);
	}

    public void clearChatListVersion() {
        SystemDatastore.getInstance().clearData(CHAT_LIST_VERSION);
    }
    
    public ChatConversation addInfoOrErrorMessage(final String messageText,
            final ChatConversation chatConversation,
            final String activeConversationId,
            final boolean shouldPersist) {

        return addInfoOrErrorMessage(chatConversation.getImMessageType(), messageText, Session.getInstance().getUsername(),
                chatConversation.getDestinationType(), chatConversation.getChatId(),
                activeConversationId, shouldPersist);
    }

    public ChatConversation addInfoOrErrorMessage(Message infoOrErrorMsg, final String activeConversationId,
            final boolean shouldPersist) {
        return addChatMessage(infoOrErrorMsg, activeConversationId, shouldPersist);
    }

    public Message createInfoOrErrorMessage(final MessageType messageType, final String messageText,
            final String userName, final ChatDestinationType destinationType,
            final String destination) {
        Message infoOrErrorMsg = ChatUtils.createInfoOrErrorMessage(messageType,
                messageText, userName, destinationType, destination);
        return  infoOrErrorMsg;
    }

    public ChatConversation addInfoOrErrorMessage(final MessageType messageType, final String messageText,
            final String userName, final ChatDestinationType destinationType,
            final String destination, final String activeConversationId, final boolean shouldPersist) {

        final Message infoOrErrorMsg = createInfoOrErrorMessage(messageType,
                messageText, userName, destinationType, destination);
        return addChatMessage(infoOrErrorMsg, activeConversationId, shouldPersist);
    }
    
    public ChatConversation addInfoIMMessage(final MessageType messageType, final String messageText,
            final String userName, String displayName, final ChatDestinationType destinationType,
            final String destination, final String activeConversationId, final boolean shouldPersist, int groupId) {
        
        final Message infoOrErrorMsg = ChatUtils.createInfoOrErrorMessage(messageType,
                messageText, userName, destinationType, destination);
        
        ChatConversation conv = addChatMessage(infoOrErrorMsg, activeConversationId, shouldPersist);
        FriendGroupTypeEnum groupType = FriendGroupTypeEnum.fromGroupTypeValue(groupId);
        
        conv.setChatType(ChatTypeEnum.IM);
        conv.setDisplayName(displayName);
        
        if(groupType == FriendGroupTypeEnum.FACEBOOK) {
            conv.setImMessageType(MessageType.FACEBOOK);
        } else if (groupType == FriendGroupTypeEnum.GTALK) {
            conv.setImMessageType(MessageType.GTALK);
        } else if (groupType == FriendGroupTypeEnum.YAHOO) {
            conv.setImMessageType(MessageType.YAHOO);
        } else if (groupType == FriendGroupTypeEnum.MSN) {
            conv.setImMessageType(MessageType.MSN);
        }
        
        //update the the right IM chat type and display name of the conversation in disk cache 
        cacheChatConversation(conv, true);
        
        return conv;
    }
	
    public ChatConversation addChatMessage(Message message, String activeConversationId, final boolean shouldPersist) {
        //Logger.debug.log(TAG,
        //        String.format("addChatMessage() activeConv: %s - message: %s", activeConversationId, message));
        
        boolean found = true;
        
        ChatConversation chatConversation = findChatConversation(message);
        if (chatConversation == null) {
            chatConversation = ChatUtils.createConversationFromMessage(message);
            found = false;
        } 

        if (didUserLeaveChat(chatConversation.getChatId())) {
            // stop processing the message. user has already left this chatroom
            return null;
        }
        
        if (!found) {
            cacheChatConversation(chatConversation, true);
        }
        
        boolean shouldIncrementCounter = message.isIncoming() && (TextUtils.isEmpty(activeConversationId)
                || (activeConversationId != null && !chatConversation.getId().equals(activeConversationId)));
        boolean isNewMessageAdded = chatConversation.addMessage(message, shouldIncrementCounter);
        // We don't save messages for public chatrooms since those are very noisy.
        // We also don't save info or error messages.
        if (shouldPersist && !chatConversation.isChatroom() && !message.isInfoMessage()) {
            if (!saveChatMessageToPersistentStorage(chatConversation.getId(),message)) {
                Logger.error.log(TAG, "Failed to save with saveChatMessageToPersistentStorage");
            }
        }

        if (isNewMessageAdded) {
            BroadcastHandler.ChatMessage.sendReceived(chatConversation.getId(), message.getMessageId());
        }
        
        return chatConversation;
    }

    public void removeChatMessage(Message message, boolean forceRefresh) {
        if (message != null) {
            ChatConversation chatConversation = findChatConversation(message);
            chatConversation.removeMessage(message);
            if (forceRefresh) {
                BroadcastHandler.ChatMessage.sendReceived(chatConversation.getId(), message.getMessageId());
            }
        }
    }

    public ChatConversation findOrCreateConversation(ChatTypeEnum type, String chatId, MessageType messageType) {
        return findOrCreateConversation(type, chatId, 0, messageType);
    }

    public ChatConversation findOrCreateConversation(ChatTypeEnum type, String chatId, int groupId, MessageType messageType) {
        ChatConversation conv = findChatConversation(type, messageType, chatId);
        if (conv == null) {
            if (type.isPrivate()) {
                conv = addInfoOrErrorMessage(messageType,I18n.tr("Make chat fun with stickers and gifts. For help, type /help."),
                        chatId, ChatDestinationType.PRIVATE, chatId, null, true);
                removeFromLeavePrivateChatList(conv.getId());
            } else if (type.isPublic()) {
                conv = ChatUtils.createChatroomConversation(chatId, groupId);
                addInfoOrErrorMessage(I18n.tr("Joining"), conv, conv.getId(), true);
            } else if (type.isGroup()) {
                conv = ChatUtils.createGroupChatConversation(chatId, null, null);
                addInfoOrErrorMessage(String.format(I18n.tr("Group chat created"), chatId),
                        conv, conv.getId(), true);                
                ChatController.getInstance().requestGroupChatParticipants(chatId, conv.getImMessageType());
            } else {
                // TODO: find/create other Thirdparty IM conversation types
                conv = addInfoOrErrorMessage(messageType,I18n.tr("Third party information messenger is not supported temporarily."),
                        chatId, ChatDestinationType.PRIVATE, chatId, null, true);
            }            
        }
        return conv;
    }
    
    
    public ChatConversation findOrCreateIMConversation(ChatTypeEnum type, String chatId, String displayName, int groupId, MessageType messageType) {
        ChatConversation conv = findChatConversation(type, messageType, chatId);
        if (conv == null) {
            if (type.isIM()) {
                conv = addInfoIMMessage(messageType, I18n.tr("Chat with friends on other networks, right here on mig."),
                        chatId, displayName, ChatDestinationType.PRIVATE, chatId, null, true, groupId);
            }          
        }
        return conv;
    }

    /**
     * Set the conversation as finished with doing the chat sync
     * 
     * @param chatId
     */
    public void setConversationAsSynced(String conversationId) {
        Logger.debug.log(TAG, "setConversationAsSynced id: ", conversationId);
        ChatConversation conv = getChatConversationWithId(conversationId);
        if (conv != null && !conv.isConversationSynced()) {
            conv.setConversationSynced(true);
            //cache chat conversation persistently for saving isSynced
            ChatDatastore.getInstance().cacheChatConversation(conv, true);
        }
    }

    /**
     * Process a {@link ChatData} object
     * 
     * @param chatData
     */
    public void addChatData(ChatData chatData) {
        Logger.debug.log(TAG, "addChatData: " + chatData);
        if (chatData != null) {
            ChatConversation conv = findChatConversation(chatData);
            if (conv != null) {
                if (chatData.isClosed()) {
                    ChatDatastore.getInstance().removeChatConversationWithId(conv.getId(), true);
                } else {
                    conv.setChatData(chatData);
                    cacheChatConversation(conv, true);
                }
            } else {
                if (!chatData.isClosed()) {
                    conv = new ChatConversation(chatData);
                    //this is new chat from server, not synced yet
                    conv.setConversationSynced(false);
                    cacheChatConversation(conv, true);
                    //fetch group participants when receiving a new group chat
                    if (conv.isGroupChat()) {
                        ChatController.getInstance().requestGroupChatParticipants(conv.getChatId(), conv.getImMessageType());
                    }
                }
            }
            if (conv != null) {
                BroadcastHandler.ChatConversation.sendReceived(conv.getId());
                
                String messageSnippet = ChatDatastore.getInstance().getMessageSnippet(conv.getId());
                
                if (messageSnippet != null) {
                    conv.setMessageSnippet(messageSnippet);
                }
                
                if (messageSnippet == null && Session.getInstance().getIsFirstTimeUserLogin()) {
                    conv.fetchMessageForNewConversation();
                } else if (!Session.getInstance().getIsFirstTimeUserLogin()) {
                    // got a new chat and it is not the first time login
                    ChatController.getInstance().sendGetLatestMessages(conv.getId());
                }
                
            }
        }
    }

    /**
     * Find a chat conversation based on the Chat {@link Message}
     * 
     * @param message
     *            - The chat message to be matched.
     * @return The relevant ChatConversation if it could be found and null
     *         otherwise.
     */
    public ChatConversation findChatConversation(Message message) {
        ChatConversation chatConversation = null;
        String conversationId = message.getConversationId();
        chatConversation = getChatConversationWithId(conversationId);
        
        return chatConversation;
    }
    
    public ChatConversation findChatConversation(ChatData chatData) {
        ChatConversation chatConversation = null;
        String conversationId = ChatUtils.generateUniqueId(chatData.getChatIdentifier(),
                chatData.getImMessageType(),
                chatData.getDestType());
        chatConversation = getChatConversationWithId(conversationId);
        
        return chatConversation;
    }

    /**
     * Find the {@link ChatConversation} based on the {@link ChatTypeEnum}
     * 
     * @param type
     * @param chatId
     * @param imMessageType 
     * @return
     */
    public ChatConversation findChatConversation(ChatTypeEnum type, MessageType imMessageType, String chatId) {
        return findChatConversation(type.getDestinationType(), imMessageType, chatId);
    }
    
    public ChatConversation findChatConversation(ChatDestinationType type, MessageType imMessageType, String chatId) {
        ChatConversation chatConversation = null;
        
        String conversationId = ChatUtils.generateUniqueId(chatId, imMessageType, type);
        chatConversation = getChatConversationWithId(conversationId);
        
        return chatConversation;
    }
    
    /**
     * Finds an existing chat message that matches the given message.
     * @param message   The {@link Message} to be matched.
     * @return          The matching {@link Message} and null if no match was found.
     */
    public Message findExistingMessage(final Message message) {
        if (message != null) {
            final ChatConversation conv = ChatDatastore.getInstance().findChatConversation(message);
            if (conv != null) {
                Message existingMessage = conv.getMessage(message.getMessageId());
                
                if (existingMessage == null) {
                    //load from db
                    if (mChatDAO != null) {
                        existingMessage = mChatDAO.loadMessageFromDB(message.getConversationId(), message.getMessageId());
                    }
                }
                return existingMessage;
            }
        }
        
        return null;
    }
    
    private ChatConversation loadChatFromStorage(String chatConversationId) {
        //create chat conversation id
        ChatConversation chatConversation = null;
        
        if(mChatDAO != null) {
            chatConversation = mChatDAO.loadChatConversationFromDatabase(chatConversationId);
        }
        
        return chatConversation;
    }
    
    private void loadLatestMessagesOfChatFromDB(ChatConversation chatConversation) {
        final List<Message> messageList = mChatDAO.loadLatestMessagesForChatConversationFromDB(
                chatConversation, DEFAULT_LOAD_MESSAGE_COUNT);
        if (messageList != null && !messageList.isEmpty()) {
            for (Message message : messageList) {
                chatConversation.addMessage(message, false);
            }
        }
    }
    
    
	/**
     * Get a public chat conversation that matches the given chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     * @return The relevant ChatConversation if it could be found and null
     *         otherwise.
     */
    public ChatConversation getChatRoomWithChatId(final String chatId) {
        synchronized (CACHE_LOCK) {
            final Collection<ChatConversation> chatConversationColl = mChatConversationCache.values();
            for (ChatConversation chatConversation : chatConversationColl) {
                if (chatConversation.isChatroom() && chatConversation.getChatId().equals(chatId)) {
                    return chatConversation;
                }
            }

            return null;
        }
    }

    /**
     * Get a private chat conversation that matches the given chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     * @return The relevant ChatConversation if it could be found and null
     *         otherwise.
     */
    private ChatConversation getPrivateChatConversationWithChatId(final String chatId) {
        synchronized (CACHE_LOCK) {
            final Collection<ChatConversation> chatConversationColl = mChatConversationCache.values();
            for (ChatConversation chatConversation : chatConversationColl) {
                if (chatConversation.isPrivateChat() && chatConversation.getChatId().equals(chatId)) {
                    return chatConversation;
                }
            }
        }

        return null;
    }

    /**
     * Get a group chat conversation that matches the given chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     * @return The relevant ChatConversation if it could be found and null
     *         otherwise.
     */
    private ChatConversation getGroupChatConversationWithChatId(final String chatId) {
        synchronized (CACHE_LOCK) {
            final Collection<ChatConversation> chatConversationColl = mChatConversationCache.values();
            for (ChatConversation chatConversation : chatConversationColl) {
                if (chatConversation.isGroupChat() && chatConversation.getChatId().equals(chatId)) {
                    return chatConversation;
                }
            }
        }

        return null;
    }
	
    /**
     * Retrieve a cached ChatRoomCategory
     * 
     * @param categoryId
     *            The id of the ChatRoomCategory to be retrieved.
     * @param shouldForceFetch
     *            Indicates that the {@link ChatRoomItem} for this category
     *            should be force fetched from the server.
     * @return The ChatRoomCategory if found in cache and null otherwise.
     * @see ChatRoomCategory#getID()
     */
	public ChatRoomCategory getChatRoomCategoryWithId(final short categoryId, final boolean shouldForceFetch) {
	    synchronized (CACHE_LOCK) {
	        if (shouldForceFetch) {
	            requestChatRoomItemsForCategoryWithId(categoryId, true);
	        }
	        
	        return mChatRoomCategoryCache.get(categoryId);
	    }
	}
	
    /**
     * Finds a cached {@link ChatRoomCategory} containing {@link ChatRoomItem}
     * with the given name.
     * 
     * @param name
     *            The name of the {@link ChatRoomItem} whose
     *            {@link ChatRoomCategory} is to be fetched.
     * @param shouldForceFetch
     *            true to force fetch all ChatRoom categories and false to use
     *            from cache.
     * @return A {@link ChatRoomCategory} if an appropriate match was found and
     *         null otherwise.
     */
    public ChatRoomCategory getChatRoomCategoryForItemWithName(final String name, final boolean shouldForceFetch) {
        synchronized (CACHE_LOCK) {            
            final List<ChatRoomCategory> chatRoomCategoryList = getAllChatRoomCategories(shouldForceFetch);

            // cos chatroomItem could belong to two categories one is favority one is other.
            ChatRoomCategory tmpCategory = null;
            for (ChatRoomCategory chatRoomCategory : chatRoomCategoryList) {
                final ChatRoomItem chatRoomItem = chatRoomCategory.getChatRoomItemWithName(name);
                if (chatRoomItem != null) {
                    tmpCategory = chatRoomCategory;
                    if (chatRoomCategory.canBeDeleted()) {
                        break;
                    }
                }
            }

            return tmpCategory;
        }
    }
	
    /**
     * Finds a cached {@link ChatRoomCategory} containing a {@link ChatRoomItem}
     * whose name matches the given {@link ChatConversation} display name.
     * 
     * @param chatConversation
     *            The {@link ChatConversation} for which the matching
     *            {@link ChatRoomCategory} is to be found.
     * @return A {@link ChatRoomCategory} if an appropriate match was found and
     *         null otherwise.
     * @see {@link ChatConversation#getDisplayName()}.
     */
    public ChatRoomCategory getChatRoomCategoryForChatConversation(final ChatConversation chatConversation) {
        if (chatConversation != null && chatConversation.getChatType() == ChatTypeEnum.CHATROOM
                && !TextUtils.isEmpty(chatConversation.getDisplayName())) {
            return getChatRoomCategoryForItemWithName(chatConversation.getDisplayName(), false);
        }

        return null;
    }
	
	/**
     * Retrieve more ChatRoomCategory from the server
     * 
     * @param categoryId
     *            The id of the ChatRoomCategory to be retrieved.
     */
    public void loadMoreChatRoomCategoryWithId(final short categoryId) {
        requestChatRoomItemsForCategoryWithId(categoryId, false);
    }
	
    /**
     * Get all cached ChatRoomCategory
     * 
     * @param shouldForceFetch
     *            Indicates that all the chatroom categories should be force
     *            fetched from the server.
     * @return A List containing all {@link ChatRoomCategory} and an empty list
     *         if no results are available.
     */
	public List<ChatRoomCategory> getAllChatRoomCategories(final boolean shouldForceFetch) {
	    synchronized (CACHE_LOCK) {
	        if (mChatRoomCategoryCache.values().isEmpty() || shouldForceFetch) {
	            requestChatRoomCategories();
	        }
	        
	        return new ArrayList<ChatRoomCategory>(mChatRoomCategoryCache.values());
	    }
	}
	
	/**
	 * Caches a ChatConversation
	 * @param chatConversation	The ChatConversation to be cached.
	 * @param shouldPersist     Indicates that the ChatConversation should be persisted to storage.
	 */
	public void cacheChatConversation(final ChatConversation chatConversation, final boolean shouldPersist) {
	    synchronized (CACHE_LOCK) {
	        mChatConversationCache.put(chatConversation.getId(), chatConversation);
	        
	        if (shouldPersist) {
	            if (!saveChatConversationToPersistentStorage(chatConversation)) {
	                Logger.error.log(TAG, "Failed to persist with saveChatConversationToPersistentStorage");
	            }
	        }
	    }
	}
	
	/**
	 * Caches a ChatRoomCategory
	 * @param chatRoomCategory	The ChatRoomCategory to be cached.
	 */
	public void cacheChatRoomCategory(final ChatRoomCategory chatRoomCategory) {
	    if (chatRoomCategory != null) {
	        synchronized (CACHE_LOCK) {
	            mChatRoomCategoryCache.put(chatRoomCategory.getID(), chatRoomCategory);
	        }
	    }
	}	
	
	/**
	 * Joins a public ChatConversation with the specified chat identifier.
	 * @param chatId   The chat identifier to match.
	 * @param groupId  The group page id to set for the ChatConversation.
	 */
	public void joinChatRoomWithChatId(final String chatId, final Integer groupId) {
	    final ChatConversation chatConversation = getChatRoomWithChatId(chatId);
	    if (chatConversation != null) {
            chatConversation.setJoined(true);                            
            if (groupId != null) {
                chatConversation.getPublicChatInfo().setGroupId(groupId);
            }
            addInfoOrErrorMessage(I18n.tr("You've joined this chat room"), chatConversation, chatConversation.getId(), true);
            BroadcastHandler.ChatConversation.ChatRoom.sendJoined(chatId);
        }
    }

    /**
     * Leaves a public ChatConversation with the specified chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     */
    public void leaveChatRoomWithChatId(final String chatId) {
        final ChatConversation chatConversation = getChatRoomWithChatId(chatId);
        if (chatConversation != null) {
            removeChatConversationWithId(chatConversation.getId(), true);                                                
        }

        BroadcastHandler.ChatConversation.ChatRoom.sendLeft(chatId);
    }

    public void setChatName(String conversationId, String newName) {
        synchronized (CACHE_LOCK) {
            ChatConversation conv = getChatConversationWithId(conversationId);
            if (conv != null) {
                conv.setDisplayName(newName);
                if (!saveChatConversationToPersistentStorage(conv)) {
                    Logger.error.log(TAG, "Failed to persist with saveChatConversationToPersistentStorage");
                }
                BroadcastHandler.ChatConversation.sendNameChanged(conversationId);
            }
        }
    }
	
	/**
	 * Leaves a private ChatConversation with the specified chat identifier.
	 * @param chatId   The chat identifier to be matched.
	 */
	public void leavePrivateChatConversationWithChatId(final String chatId) {	    
	    final ChatConversation chatConversation = getPrivateChatConversationWithChatId(chatId);	    
	    
	    if (chatConversation != null) {
	        removeChatConversationWithId(chatConversation.getId(), true);
	    }
	    
	    BroadcastHandler.ChatConversation.PrivateChat.sendLeft(chatId);
	}
	
	/**
     * Leaves a group ChatConversation with the specified chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     */
	public void leaveGroupChatConversationWithChatId(final String chatId) {
	    final ChatConversation chatConversation = getGroupChatConversationWithChatId(chatId);
	    if (chatConversation != null) {
	        removeChatConversationWithId(chatConversation.getId(), true);
	    }
	    
	    BroadcastHandler.ChatConversation.GroupChat.sendLeft(chatId);
	}
	
    public void disconnectAllChatrooms() {
        synchronized (CACHE_LOCK) {
            final Collection<ChatConversation> chatConversationColl = mChatConversationCache.values();
            for (ChatConversation chatConversation : chatConversationColl) {
                if (chatConversation.isChatroom() && chatConversation.isJoined()) {
                    chatConversation.setJoined(false);
                    addInfoOrErrorMessage(I18n.tr("You've left this chat room"), chatConversation, null, true);
                    
                    BroadcastHandler.ChatConversation.ChatRoom.sendDisconnected(chatConversation.getId());
                }
            }
        }
    }

    public void resetAllChatSyncState() {
        synchronized (CACHE_LOCK) {
            final Collection<ChatConversation> chatConversationColl = mChatConversationCache.values();
            for (ChatConversation chatConversation : chatConversationColl) {
                if (chatConversation.isSyncedOnShow()) {
                    chatConversation.setSyncedOnShow(false);
                }
            }
        }
    }

    /**
     * Sets the participants of a ChatConversation that matches the given id.
     * 
     * @param id
     *            The id of the ChatConversation.
     * @param participantList
     *            A list containing the ChatParticipant to be set.
     */
    public void setParticipantsForChatConversationWithId(final String id, final List<ChatParticipant> participantList,
            final ChatParticipantType requestingUserType) {
        final ChatConversation chatConversation = getChatConversationWithId(id);
        if (chatConversation != null) {
            chatConversation.setParticipants(participantList);
            chatConversation.setRequestingUserType(requestingUserType);
        }
    }

    /**
     * gets the participants of a ChatConversation that matches the given id.
     * 
     * @param id
     *            The id of the ChatConversation.
     * @param participantList
     *            A list containing the ChatParticipant to be set.
     * @param shouldExcludeCurrentUser
     *            Whether the currently logged in user should be excluded from the results.
     * @return
     *            A List of {@link ChatParticipant} or 
     *            null if a {@link ChatConversation} matching the given id could not be found.            
     */
    public List<ChatParticipant> getParticipantsForChatConversationWithId(final String id, boolean shouldExcludeCurrentUser) {
        final ChatConversation chatConversation = getChatConversationWithId(id);
        if (chatConversation != null) {
            return chatConversation.getParticipants(shouldExcludeCurrentUser);
        }
        return null;
    }

    public String getChatIdWithConversationId(final String id) {
        final ChatConversation chatConversation = getChatConversationWithId(id);
        if (chatConversation != null) {
            return chatConversation.getChatId();
        }
        return null;
    }

    /**
     * Adds a participant to a ChatConversation that matches the given id. NOTE:
     * This is not the same as calling
     * {@link #setParticipantsForChatConversationWithId(String, List)} with a
     * single item in the List.
     * 
     * @see ChatConversation#addParticipant(ChatParticipant)
     * @param id
     *            The id of the ChatConversation.
     * @param participant
     *            The ChatParticipant to be added.
     */
    public void addParticipantToChatConversationWithId(final String id, final ChatParticipant participant) {
        final ChatConversation chatConversation = getChatConversationWithId(id);
        if (chatConversation != null) {
            chatConversation.addParticipant(participant);
        }
    }

    /**
     * Removes a participant from a ChatConversation that matches the given id.
     * 
     * @param id
     *            The id of the ChatConversation.
     * @param username
     *            The username of the ChatParticipant to be removed.
     * @see ChatParticipant#getUsername()
     */
    public void removeParticipantFromChatConversationWithId(final String id, final String username) {
        final ChatConversation chatConversation = getChatConversationWithId(id);
        if (chatConversation != null) {
            chatConversation.removeParticipant(username);
        }
    }

    /**
     * Sets the participants of a public ChatConversation that matches the given
     * chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     * @param chatRoomParticipantList
     *            A list containing the chatroom participants to be set.
     */
    public void setChatRoomParticipants(final String chatId, final List<ChatParticipant> chatRoomParticipantList,
            final ChatParticipantType requestingUserType) {
        Logger.debug.log(TAG,
                String.format("setChatRoomParticipants chatId: %s participants: %s", chatId, chatRoomParticipantList));

        final ChatConversation chatConversation = getChatRoomWithChatId(chatId);
        if (chatConversation != null) {
            setParticipantsForChatConversationWithId(chatConversation.getId(), chatRoomParticipantList, requestingUserType);

            BroadcastHandler.ChatParticipant.sendFetchAllCompleted(chatConversation.getId());
        }
    }
    
    /**
     * Sets the participants of a group chat conversation that matches the given
     * chat identifier.
     * @param chatId
     *              The chat identifier to be matched.
     * @param groupChatParticipantList
     *              A List containing the group chat participants to be set.
     */
    public void setGroupChatParticipants(final String chatId, final List<ChatParticipant> groupChatParticipantList) {
        Logger.debug.log(TAG,
                String.format("setGroupChatParticipants chatId: %s participants: %s", chatId, groupChatParticipantList));
        
        final ChatConversation chatConversation = getGroupChatConversationWithChatId(chatId);
        if (chatConversation != null) {
            setParticipantsForChatConversationWithId(chatConversation.getId(), groupChatParticipantList, 
                    ChatParticipantType.NORMAL);
            
            BroadcastHandler.ChatParticipant.sendFetchAllCompleted(chatConversation.getId());
        }
    }
    
    /**
     * Adds a participant to a group chat ChatConversation that matches the given
     * chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     * @param participant
     *            The participant to be added.
     */
    public void addGroupChatParticipant(final String chatId, final ChatParticipant participant) {
        final ChatConversation chatConversation = getGroupChatConversationWithChatId(chatId);
        if (chatConversation != null) {
            addParticipantToChatConversationWithId(chatConversation.getId(), participant);
        }
    }
    
    /**
     * Removes a participant from a group chat ChatConversation that matches the
     * given chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     * @param username
     *            The username of the ChatParticipant to be removed.
     */
    public void removeGroupChatParticipant(final String chatId, final String username) {
        final ChatConversation chatConversation = getGroupChatConversationWithChatId(chatId);
        if (chatConversation != null) {
            removeParticipantFromChatConversationWithId(chatConversation.getId(), username);
        }
    }

    /**
     * Adds a participant to a public ChatConversation that matches the given
     * chat identifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     * @param chatRoomParticipant
     *            The participant to be added.
     */
    public void addChatRoomParticipant(final String chatId, final ChatParticipant chatRoomParticipant) {
        final ChatConversation chatConversation = getChatRoomWithChatId(chatId);
        if (chatConversation != null) {
            addParticipantToChatConversationWithId(chatConversation.getId(), chatRoomParticipant);
        }
    }

    /**
     * Removes a participant from a public ChatConversation that matches the
     * given chat idnetifier.
     * 
     * @param chatId
     *            The chat identifier to be matched.
     * @param username
     *            The username of the ChatParticipant to be removed.
     */
    public void removeChatRoomParticipant(final String chatId, final String username) {
        final ChatConversation chatConversation = getChatRoomWithChatId(chatId);
        if (chatConversation != null) {
            removeParticipantFromChatConversationWithId(chatConversation.getId(), username);
        }
    }

    // Network requests
    
    public void requestChatRoomCategories() {
        requestChatRoomCategories(ApplicationEx.getInstance().getNetworkService());
    }
    
    public void requestChatRoomCategories(NetworkService service) {

        RequestManager requestManager = service == null ? null : service.getRequestManager();
        if (requestManager != null) {
            requestManager.sendGetChatroomCategories(getChatRoomCategoriesListener);
        }
    }
    
    public void requestAddFavouriteChatRoom(final String chatRoomName) {
        final ApplicationEx appEx = ApplicationEx.getInstance();

        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendAddFavouriteChatRoom(getChatRoomCategoriesListener, chatRoomName);
            }
        }
    }
    
    public void requestRemoveFavouriteChatRoom(final String chatRoomName, final short categoryId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();

        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendRemoveFavouriteChatRoom(getChatRoomCategoriesListener, chatRoomName);
            }
        }
    }

    public void requestChatRoomItemsForCategoryWithId(final short id, final boolean shouldDoFullRefresh) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                final ChatRoomCategory chatRoomCategory = getChatRoomCategoryWithId(id, false);                
                if (chatRoomCategory != null) {
                    if (shouldDoFullRefresh) {
                        chatRoomCategory.clearChatRoomItems();
                    }
                    requestManager.sendGetChatRoomItemsForCategoryWithId(new GetCategorizedChatRoomItemsListener() {
    
                        @Override
                        public void onChatRoomItemReceived(final ChatRoomItem chatRoomItem, final Short parentCategoryId) {
                        if (parentCategoryId != null && chatRoomItem != null) {
                                chatRoomCategory.addChatRoomItem(chatRoomItem, UIUtils.getChatroomColorArray());

                                BroadcastHandler.ChatRoom.sendReceived(chatRoomItem.getName(), parentCategoryId);
                            }
                        }
    
                        @Override
                        public void onGetCategorizedChatRoomsComplete(Short chatRoomCategoryId,
                                String chatRoomCategoryFooter) {
                            Logger.debug.log("chatfooter", "onGetCategorizedChatRoomsComplete[" + chatRoomCategoryId
                                    + "]: footer: " + chatRoomCategoryFooter);
                            if (chatRoomCategoryId != null) {
                                chatRoomCategory.setIsLoading(false);
                                chatRoomCategory.setCategoryLabel(chatRoomCategoryFooter);
                                BroadcastHandler.ChatRoom.sendFetchForCategoryCompleted(chatRoomCategoryId,
                                        chatRoomCategoryFooter);
                            }
                        }

                        @Override
                        public void onChatRoomNotificationReceived(final String message, final String url) {
                            setChatNotification(new ChatNotification(message, url));
                            BroadcastHandler.ChatRoom.sendChatNotification();
                        }
    
                        @Override
                        public void onError(MigError error) {
                            chatRoomCategory.setIsLoading(false);
                            BroadcastHandler.ChatRoom.sendFetchForCategoryError(error, chatRoomCategory.getName());
                        }
    
                    }, id, shouldDoFullRefresh);
                 
                    chatRoomCategory.setIsLoading(true);
                    BroadcastHandler.ChatRoom.sendBeginFetchForCategory(id);
                }
                else {
                    Logger.error.log(TAG, "Could not fetch chat room items for chatroom category with id: ", id,
                            " The chatroom category was not previously fetched!");
                }
            }
        }
	}
	
	/**
	 * Loads all chat related data from persistent storage.
	 */
	public void loadFromPersistentStorage() {
	    if (mChatDAO != null) {
	        // Load all ChatConversations.
	        final List<ChatConversation> chatConversationList = mChatDAO.loadAllChatConversationsFromDatabase();
	        if (chatConversationList != null && !chatConversationList.isEmpty()) {
	            for (ChatConversation chatConversation : chatConversationList) {
	                cacheChatConversation(chatConversation, false);
	                
	                // Load messages for each ChatConversation.
	                loadLatestMessagesOfChatFromDB(chatConversation);
	            }
	        }
	    }
	}
	
	public List<Message> loadMoreMessageFromStorage(ChatConversation chatConversation, long timestamp) {
	    List<Message> result = new ArrayList<Message>();
	    if (mChatDAO != null) {
            result = mChatDAO.loadMoreMessageFromDB(chatConversation, timestamp, DEFAULT_LOAD_MESSAGE_COUNT);
        }
	    
	    for (Message msg : result) {
	        chatConversation.addMessageFromStorage(msg);
        }
	    
	    BroadcastHandler.ChatMessage.sendMessagesLoadedFromDB(chatConversation.getChatId());
	    
	    return result;
	}
	
	public List<Message> loadMoreMessageFromStorage(ChatConversation chatConversation, long timestampStart, long timestampEnd) {
        List<Message> result = new ArrayList<Message>();
        if (mChatDAO != null) {
            result = mChatDAO.loadMoreMessageFromDB(chatConversation, timestampStart, timestampEnd, DEFAULT_LOAD_MESSAGE_COUNT);
            BroadcastHandler.ChatMessage.sendMessagesLoadedFromDB(chatConversation.getChatId());
        }
        
        for (Message msg : result) {
            chatConversation.addMessageFromStorage(msg);
        }
        
        BroadcastHandler.ChatMessage.sendMessagesLoadedFromDB(chatConversation.getChatId());
        
        return result;
    }
	
	
	public boolean hasMessageFromStorage(final String chatConversationId, final String msgId) {
        boolean result = false;
        
	    if (mChatDAO != null) {
	        result = mChatDAO.hasChatMessageInDatabase(chatConversationId, msgId);
        }
	    
	    return result;
    }
	
	public boolean hasOlderMessageFromStorage(final String chatConversationId, final long timestamp) {
	    boolean result = false;
        
        if (mChatDAO != null) {
            result = mChatDAO.hasOlderMessageInDatabase(chatConversationId, timestamp);
        }
        
        return result;
    }
	
	/**
	 * Saves a {@link ChatConversation} to persistent storage.
	 * @param chatConversation     The {@link ChatConversation} to be persisted.
	 * @return                     true on success and false otherwise.
	 */
	public boolean saveChatConversationToPersistentStorage(final ChatConversation chatConversation) {
	    if (mChatDAO != null) {
	        return mChatDAO.saveChatConversationToDatabase(chatConversation);
	    }
	    
	    return false;
	}
	
	/**
	 * Saves a {@link Message} in a ChatConversation to persistent storage.
	 * @param chatConversationId   The id of the {@link ChatConversation} that contains the message.
	 * @param message              The {@link Message} to be persisted.
	 * @return                     true on success and false otherwise.
	 */
	public boolean saveChatMessageToPersistentStorage(final String chatConversationId, final Message message) {
	    if (mChatDAO != null) {
	        return mChatDAO.saveChatMessageToDatabase(chatConversationId, message);
	    }
	    
	    return false;
	}

    /**
     * @param latestViewedMsgId
     * @param conversationId 
     */
    public void updateLastViewedMessage(String conversationId, String latestViewedMsgId) {
        final ChatConversation chatConversation = getChatConversationWithId(
                conversationId);
        if (chatConversation != null) {
            //set it in memory
            chatConversation.setLatestViewedMsgId(latestViewedMsgId);
            //save it in db
            saveLastViewedMessageToPersistentStorage(chatConversation);
        }
    }
    
	/**
	 * save the conversation, and the lastViewedMessage will be saved
	 */
	
	public boolean saveLastViewedMessageToPersistentStorage(final ChatConversation chatConversation) {
	    return saveChatConversationToPersistentStorage(chatConversation);
    }
	
	public void removeChatMessagesFromPersistentStorage(final String chatConversationId, final String[] messageIds) {
	   if (messageIds != null && mChatDAO != null) {
	       mChatDAO.removeChatMessageFromDatabase(chatConversationId, messageIds);
	   } 
	}
	
	/**
	 * Removes a {@link ChatConversation} and its associated Messages from persistent storage.
	 * @param chatConversation     The {@link ChatConversation} to be removed.
	 * @return                     true on success and false otherwise.
	 */
	public boolean removeChatConversationFromPersistentStorage(final ChatConversation chatConversation) {
	    if (mChatDAO != null) {
	        return mChatDAO.removeChatConversationFromDatabase(chatConversation);
	    }
	    
	    return false;
	}

    public boolean didUserLeaveChat(String chatId) {
        return chatRoomLeft.contains(chatId) || groupChatLeft.contains(chatId);
    }

    public void addToChatroomLeft(String chatId) {
        chatRoomLeft.add(chatId);
    }

    public void removeFromChatroomLeft(String chatId) {
        chatRoomLeft.remove(chatId);
    }

    public void addToGroupChatLeft(String chatId) {
        groupChatLeft.add(chatId);
    }

    public void removeFromGroupChatLeft(String chatId) {
        groupChatLeft.remove(chatId);
    }

    /**
     * @return the chatNotification
     */
    public ChatNotification getChatNotification() {
        return chatNotif;
    }
    
    public void setChatNotification(ChatNotification data) {
        chatNotif = data;
    }
    
    public void addMessageSnippet(String conversationId, String msgSnippet) {        
        Logger.debug.log("addMessageSnippet", msgSnippet);
        if (msgSnippet != null) {
            synchronized (CACHE_LOCK) {
                mChatMsgSnippetCache.put(conversationId, msgSnippet);
            }
        }
    }
    
    public String getMessageSnippet(String conversationId) {
        synchronized (CACHE_LOCK) {
            return mChatMsgSnippetCache.get(conversationId);
        }
    }

    public ChatRoomItem getChatRoomItem(final String chatroomName) {
        synchronized (CACHE_LOCK) {
            for(ChatRoomCategory category: mChatRoomCategoryCache.values()) {
                ChatRoomItem item = category.getChatRoomItemWithName(chatroomName);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
    }
    
    public void setChatConversationPinnedMessage(final String chatConversationId, final Message pinnedMessage,
            final boolean isNoPinnedMessage) {
        
        ChatConversation chatConversation = getChatConversationWithId(chatConversationId);

        if (chatConversation != null) { 
            chatConversation.setPinnedMessage(pinnedMessage);

            if (isNoPinnedMessage) {
                chatConversation.setPinnedMessage(null);
                BroadcastHandler.ChatMessage.sendNoPinnedMessage(chatConversationId);
            } else {
                BroadcastHandler.ChatMessage.sendReceivedPinnedState(chatConversationId);
            }
        }
    }

    public void cacheChatroomInfo(String chatroomName, ChatroomInfo chatroomInfo) {
        mChatroomInfoCache.cacheData(chatroomName, chatroomInfo);
    }

    public ChatroomInfo getChatroomInfo(final String chatroomName) {
        return getChatroomInfo(chatroomName, false);
    }

    public ChatroomInfo getChatroomInfo(final String chatroomName, boolean shouldForceFetch) {
        if (mChatroomInfoCache.isExpired(chatroomName) || shouldForceFetch) {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getChatroomInfo(new GetChatroomInfoListener() {
                    @Override
                    public void onChatroomInfoReceived(ChatroomInfo chatroomInfo) {
                        mChatroomInfoCache.cacheData(chatroomName, chatroomInfo);
                        BroadcastHandler.ChatRoom.sendFetchChatroomInfoSuccess(chatroomName);
                    }
                }, chatroomName);
            }
        }

        return mChatroomInfoCache.getData(chatroomName);
    }

    public void addToLeavePrivateChatList(String conversationId) {
        if (isInLeavePrivateChatList(conversationId)) {
           return;
        }
        mLeavePrivateChatList.add(conversationId);
    }

    private void removeFromLeavePrivateChatList(String conversationId) {
        for (int position = 0; position< mLeavePrivateChatList.size(); position++) {
            String convId = mLeavePrivateChatList.get(position);
            if (convId.equals(conversationId)) {
                mLeavePrivateChatList.remove(position);
                break;
            }
        }
    }

    public boolean isInLeavePrivateChatList(String conversationId) {
        for (String convId : mLeavePrivateChatList) {
            if (convId.equals(conversationId)){
                return true;
            }
        }
        return false;
    }

    public void persistFailedMessage(Message message) {
        if (mChatDAO != null) {
            mChatDAO.addFailedChatMessageToDatabase(message.getConversationId(), message);
        }
    }

    public boolean isFailedMessageQueueEmpty() {
        if (mFailedMessageQueue != null && mFailedMessageQueue.size() <= 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<Message> getAllFailedMessages() {
        if (mChatDAO != null) {
            List<Message> failedMessages = mChatDAO.loadAllFailedMessagesFromDatabase();
            return failedMessages;
        } else {
            return null;
        }
    }

    public void deleteAllFailedMessages() {
        if (mChatDAO != null) {
            mChatDAO.deleteAllFailedMessagesFromDatabase();
        }
    }


}
