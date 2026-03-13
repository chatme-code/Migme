package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.gateway.packet.chatsync.FusionPktChat;
import com.projectgoth.fusion.slice.ChatDefinitionIce;
import com.projectgoth.fusion.slice.FusionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class ChatDefinition implements ChatSyncEntity {
   private static final LogFilter log;
   private static final String COMMA = ",";
   private static final String COLON = ":";
   private static final String CHATROOM_ID_PREFIX = "CR#";
   private String chatStorageID;
   private String[] participantUsernames;
   private Byte chatType;
   private Integer unreadMessageCount;
   private Integer contactID;
   private String groupOwner;
   private Byte isClosedChat;
   private String displayGUID;
   private Byte messageType;
   private String chatName;
   private Boolean isPassivatedChat;
   private int writeOpCount;
   private ChatMessage latestMessage;

   public ChatDefinition() {
   }

   public ChatDefinition(String storageID) throws FusionException {
      this.chatStorageID = storageID;
      if (this.isPrivateChat()) {
         this.chatType = (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value();
      } else if (this.isChatRoomDef()) {
         this.chatType = (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value();
      } else {
         this.chatType = (byte)MessageDestinationData.TypeEnum.GROUP.value();
      }

   }

   public ChatDefinition(String chatID, byte chatType) throws FusionException {
      if (chatType == (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value()) {
         throw new FusionException("Invalid chat type for this ctor");
      } else {
         this.chatStorageID = this.userSuppliedToInternalChatID(chatType, (String)null, chatID);
         this.chatType = chatType;
      }
   }

   public ChatDefinition(String id, byte chatType, String username) throws FusionException {
      this.chatStorageID = this.userSuppliedToInternalChatID(chatType, username, id);
      this.chatType = chatType;
   }

   private String userSuppliedToInternalChatID(byte chatType, String username, String userSuppliedChatID) throws FusionException {
      if (chatType == (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value()) {
         String realChatID = this.makePrivateChatKey(username, userSuppliedChatID);
         if (log.isDebugEnabled()) {
            log.debug("user supplied chatID=" + userSuppliedChatID + " converted to=" + realChatID);
         }

         return realChatID;
      } else {
         return chatType == (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value() ? this.makeChatRoomKey(userSuppliedChatID) : userSuppliedChatID;
      }
   }

   private String makePrivateChatKey(String username, String otherUser) {
      return username.compareTo(otherUser) < 0 ? username + ":" + otherUser : otherUser + ":" + username;
   }

   private String makeChatRoomKey(String chatRoomName) throws FusionException {
      try {
         int crID = MemCacheOrEJB.getChatRoomID(chatRoomName);
         return "CR#" + crID;
      } catch (FusionException var4) {
         throw var4;
      } catch (Exception var5) {
         String errStr = "makeChatRoomKey for chatRoomName=" + chatRoomName + ": " + var5;
         log.error(errStr, var5);
         throw new FusionException(errStr);
      }
   }

   public ChatDefinition(ChatDefinitionIce cdi) throws FusionException {
      this.chatStorageID = cdi.chatStorageID;
      this.participantUsernames = cdi.participantUsernames;
      this.chatType = cdi.chatType;
      this.unreadMessageCount = cdi.unreadMessageCount;
      this.contactID = cdi.contactID;
      this.groupOwner = cdi.groupOwner;
      this.isClosedChat = cdi.isClosedChat == -128 ? null : cdi.isClosedChat;
      this.displayGUID = cdi.displayGUID;
      this.messageType = cdi.messageType;
      this.chatName = cdi.chatName;
      this.isPassivatedChat = cdi.isPassivatedChat == -128 ? null : cdi.isPassivatedChat == 1;
   }

   public ChatDefinition(String username, String chatID, String[] participantUsernames, Byte chatType, Integer unreadMessageCount, Integer contactID, String groupOwner, Byte isClosedChat, String displayGUID, Byte messageType) throws FusionException {
      this(username, chatID, participantUsernames, chatType, unreadMessageCount, contactID, groupOwner, isClosedChat, displayGUID, messageType, (String)null);
   }

   public ChatDefinition(String username, String chatID, String[] participantUsernames, Byte chatType, Integer unreadMessageCount, Integer contactID, String groupOwner, Byte isClosedChat, String displayGUID, Byte messageType, String chatName) throws FusionException {
      this.chatStorageID = this.userSuppliedToInternalChatID(chatType, username, chatID);
      this.participantUsernames = participantUsernames;
      this.chatType = chatType;
      this.unreadMessageCount = unreadMessageCount;
      this.contactID = contactID;
      this.groupOwner = groupOwner;
      this.isClosedChat = isClosedChat;
      this.displayGUID = displayGUID;
      this.messageType = messageType;
      this.chatName = chatName;
   }

   public ChatDefinition(String chatStorageID, ChatSyncStore store) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Loading ChatDefinition from redis with storage id=" + chatStorageID);
      }

      try {
         this.chatStorageID = chatStorageID;
         ChatSyncStore[] stores = new ChatSyncStore[]{store};
         this.retrieve(stores);
         if (log.isDebugEnabled()) {
            log.debug("Loaded ChatDefinition from redis with storage id=" + chatStorageID);
         }

      } catch (ChatDefinition.ChatDefinitionNotFoundException var4) {
         throw var4;
      } catch (FusionException var5) {
         log.error("Exception loading ChatDefinition id=" + chatStorageID, var5);
         throw var5;
      } catch (Exception var6) {
         log.error("Exception loading ChatDefinition id=" + chatStorageID, var6);
         throw new FusionException(var6.getMessage());
      }
   }

   private String loadOptionalString(Object oVal, String fieldName) throws FusionException {
      return this.loadString(oVal, fieldName);
   }

   private String loadString(Object oVal, String fieldName) throws FusionException {
      String val = Redis.getPipelineResult(oVal);
      if (log.isDebugEnabled()) {
         log.debug(fieldName + " val=" + val);
      }

      return val;
   }

   private Byte loadOptionalByte(Object oVal, String fieldName) throws FusionException {
      String val = Redis.getPipelineResult(oVal);
      if (log.isDebugEnabled()) {
         log.debug(fieldName + " val=" + val);
      }

      return val != null && !val.equals("null") ? Byte.parseByte(val) : null;
   }

   private Integer loadInteger(Object oVal, String fieldName) throws FusionException {
      String val = Redis.getPipelineResult(oVal);
      if (log.isDebugEnabled()) {
         log.debug(fieldName + " val=" + val);
      }

      return val != null && !val.equals("null") ? Integer.parseInt(val) : null;
   }

   private Long loadLong(Object oVal, String fieldName) throws FusionException {
      String val = Redis.getPipelineResult(oVal);
      if (log.isDebugEnabled()) {
         log.debug(fieldName + " val=" + val);
      }

      return val != null && !val.equals("null") ? Long.parseLong(val) : null;
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.CONVERSATION;
   }

   public String getKey() {
      return this.chatStorageID;
   }

   public String getValue() {
      return null;
   }

   public String getChatID() {
      return this.chatStorageID;
   }

   public byte getChatType() {
      return this.chatType;
   }

   public String getGroupOwner() {
      return this.groupOwner;
   }

   public String[] getParticipantUsernames() {
      return this.participantUsernames;
   }

   public String getChatName() {
      return this.chatName;
   }

   private void renewExpiry(SystemPropertyEntities.ChatSyncSettings toggle, ChatSyncStore store) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)toggle)) {
         int hours = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_DEFINITION_EXPIRY_HOURS);
         store.expire(this, hours * 3600);
         ++this.writeOpCount;
      }

   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      ChatSyncStore[] arr$ = stores;
      int len$ = stores.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatSyncStore store = arr$[i$];
         if (log.isDebugEnabled()) {
            log.debug("Storing ChatDefinition:  participantUsernames=" + this.participantUsernames + " chatType=" + this.chatType);
         }

         store.setMaster();
         ArrayList<ChatSyncPipelineOp> writeOps = new ArrayList();
         writeOps.add(new ChatSyncPipelineOp(this, ChatSyncPipelineOp.OpType.WRITE));
         store.pipelinedBinary((List)writeOps);
         ChatSyncStats.getInstance().incrementTotalChatsStored();
         log.debug("Stored ChatDefinition");
      }

   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public FusionPktChat toFusionPktChat(short txnID, int chatListVersion, long chatListTimestamp, String recipientUsername) throws FusionException {
      FusionPktChat pkt = new FusionPktChat();
      pkt.setTransactionId(txnID);
      String returnChatID = this.internalToExternalChatID(recipientUsername);
      pkt.setChatIdentifier(returnChatID);
      if (!StringUtil.isBlank(this.chatName)) {
         pkt.setChatDisplayName(this.chatName);
         pkt.setIsRenamedChat((byte)1);
      } else {
         this.autoGenerateChatDisplayName(pkt, recipientUsername);
         pkt.setIsRenamedChat((byte)0);
      }

      pkt.setChatType(this.chatType);
      if (this.unreadMessageCount != null) {
         pkt.setUnreadMessageCount(this.unreadMessageCount);
      }

      if (this.contactID != null) {
         pkt.setContactId(this.contactID);
      }

      if (this.groupOwner != null) {
         pkt.setGroupOwner(this.groupOwner);
      }

      if (this.isClosedChat != null) {
         pkt.setIsClosedChat(this.isClosedChat);
      }

      if (this.displayGUID != null) {
         if (log.isDebugEnabled()) {
            log.debug("ChatDefinition.toFusionPktChat: setting pkt.displayGUID=" + this.displayGUID);
         }

         pkt.setDisplayGUID(this.displayGUID);
      } else {
         if (log.isDebugEnabled()) {
            log.debug("ChatDefinition.toFusionPktChat: set pkt.displayGUID to blank string");
         }

         pkt.setDisplayGUID("");
      }

      if (this.messageType != null) {
         pkt.setMessageType(this.messageType);
      }

      if (this.latestMessage != null) {
         pkt.setTimestamp(this.latestMessage.getTimestamp());
      } else {
         pkt.setTimestamp(System.currentTimeMillis());
      }

      pkt.setChatListVersion(chatListVersion);
      pkt.setChatListTimestamp(chatListTimestamp);
      if (this.isPassivatedChat != null) {
         pkt.setIsPassivatedChat((byte)(this.isPassivatedChat ? 1 : 0));
      }

      return pkt;
   }

   private void autoGenerateChatDisplayName(FusionPktChat pkt, String recipientUsername) {
      if (this.participantUsernames != null) {
         String commaSep = "";
         String[] arr$ = this.participantUsernames;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String p = arr$[i$];
            if (!p.equals(recipientUsername)) {
               commaSep = commaSep + (commaSep.length() != 0 ? "," : "") + p;
            }
         }

         pkt.setChatDisplayName(commaSep);
      }

   }

   public String internalToExternalChatID(String parentUsername) throws FusionException {
      if (this.chatType == (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value()) {
         String[] particips = this.chatStorageID.split(":");
         return parentUsername.equals(particips[0]) ? particips[1] : particips[0];
      } else if (this.chatType == (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value()) {
         try {
            String chatRoomID = this.chatStorageID.substring("CR#".length());
            return MemCacheOrEJB.getChatRoomName(Integer.parseInt(chatRoomID));
         } catch (Exception var4) {
            String errStr = "Unable to find chatroom name for chatStorageID=" + this.chatStorageID + ": " + var4;
            log.error(errStr, var4);
            throw new FusionException(errStr);
         }
      } else {
         return this.chatStorageID;
      }
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      try {
         if (this.isChatRoomDef()) {
            this.retrieveChatroom(stores);
         } else {
            this.retrieveInner(stores);
         }

      } catch (FusionException var3) {
         throw var3;
      } catch (Exception var4) {
         log.error("For chatStorageID=" + this.chatStorageID, var4);
         throw new FusionException(var4.getMessage());
      }
   }

   private void retrieveChatroom(ChatSyncStore[] stores) throws FusionException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
         throw new ChatDefinition.ChatDefinitionNotFoundException(this.chatStorageID);
      } else {
         ChatSyncStore store = stores[0];
         this.chatType = (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value();

         try {
            String chatRoomID = this.chatStorageID.substring("CR#".length());
            this.chatName = MemCacheOrEJB.getChatRoomName(Integer.parseInt(chatRoomID));
         } catch (Exception var6) {
            String sErr = "Unable to retrieve chatroom name for stored chatroom=" + this.chatStorageID + ": " + var6;
            log.error(sErr, var6);
            throw new FusionException(sErr);
         }

         store.setSlave();
         ChatMessage msgKey = new ChatMessage(this);
         Set<byte[]> msgs = store.zrangeBinary(msgKey, -1, -1);
         if (msgs != null && msgs.size() != 0) {
            List<byte[]> msgsList = new ArrayList(msgs);
            this.latestMessage = new ChatMessage(this, (byte[])msgsList.get(0));
         }

      }
   }

   private void retrieveInner(ChatSyncStore[] stores) throws FusionException {
      ChatSyncStore store = stores[0];
      store.setMaster();
      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_READ_ENABLED, store);
      store.setSlave();
      List<Object> results = store.pipelinedBinary(this, new ChatSyncStorePipeline() {
         public void execute(ChatSyncEntity entity, ChatSyncStore pipelineStore) throws FusionException {
            entity.retrievePipeline(pipelineStore);
         }
      });
      if (results != null) {
         this.loadPipeline(results, 0);
         ChatSyncStats.getInstance().incrementTotalChatsRetrieved();
      } else {
         throw new ChatDefinition.ChatDefinitionNotFoundException(this.chatStorageID);
      }
   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
      pipelineStore.hget(this, ChatDefinition.FieldName.CHAT_TYPE.toString());
      pipelineStore.hget(this, ChatDefinition.FieldName.UNREAD_MESSAGE_COUNT.toString());
      pipelineStore.hget(this, ChatDefinition.FieldName.CONTACT_ID.toString());
      pipelineStore.hget(this, ChatDefinition.FieldName.GROUP_OWNER.toString());
      pipelineStore.hget(this, ChatDefinition.FieldName.IS_CLOSED_CHAT.toString());
      pipelineStore.hget(this, ChatDefinition.FieldName.DISPLAY_GUID.toString());
      pipelineStore.hget(this, ChatDefinition.FieldName.MESSAGE_TYPE.toString());
      pipelineStore.hget(this, ChatDefinition.FieldName.CHAT_NAME.toString());
      ParticipantList participants = new ParticipantList(this);
      participants.retrievePipeline(pipelineStore);
      ChatMessage msgKey = new ChatMessage(this);
      pipelineStore.zrangeBinary(msgKey, -1, -1);
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      int index = startIndex + 1;
      Object oChatType = pipelineResults.get(startIndex);
      if (oChatType == null) {
         throw new ChatDefinition.ChatDefinitionNotFoundException(this.chatStorageID);
      } else {
         this.chatType = this.loadOptionalByte(oChatType, ChatDefinition.FieldName.CHAT_TYPE.toString());
         if (log.isDebugEnabled()) {
            log.debug("ChatDefinition: loaded chatType=" + this.chatType);
         }

         this.unreadMessageCount = this.loadInteger(pipelineResults.get(index++), ChatDefinition.FieldName.UNREAD_MESSAGE_COUNT.toString());
         this.contactID = this.loadInteger(pipelineResults.get(index++), ChatDefinition.FieldName.CONTACT_ID.toString());
         this.groupOwner = this.loadString(pipelineResults.get(index++), ChatDefinition.FieldName.GROUP_OWNER.toString());
         if (log.isDebugEnabled()) {
            log.debug("ChatDefinition: loaded groupOwner=" + this.groupOwner);
         }

         this.isClosedChat = this.loadOptionalByte(pipelineResults.get(index++), ChatDefinition.FieldName.IS_CLOSED_CHAT.toString());
         if (log.isDebugEnabled()) {
            log.debug("ChatDefinition: loaded closedChat=" + this.isClosedChat);
         }

         this.displayGUID = this.loadString(pipelineResults.get(index++), ChatDefinition.FieldName.DISPLAY_GUID.toString());
         this.messageType = this.loadOptionalByte(pipelineResults.get(index++), ChatDefinition.FieldName.MESSAGE_TYPE.toString());
         this.chatName = this.loadOptionalString(pipelineResults.get(index++), ChatDefinition.FieldName.CHAT_NAME.toString());
         ParticipantList participants = new ParticipantList(this);
         index = participants.loadPipeline(pipelineResults, index);
         if (participants.getUsernames() != null && participants.getUsernames().length != 0) {
            this.participantUsernames = participants.getUsernames();
         }

         List<byte[]> msgs = new ArrayList((Set)pipelineResults.get(index++));
         if (msgs.size() != 0) {
            this.latestMessage = new ChatMessage(this, (byte[])msgs.get(0));
         }

         return index;
      }
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
      this.writeOpCount = 0;
      pipelineStore.multi(this);
      ++this.writeOpCount;
      ParticipantList pl = new ParticipantList(this, this.participantUsernames);
      pl.storePipeline(pipelineStore);
      this.writeOpCount += pl.getWriteOpCount();
      pipelineStore.hset(this, ChatDefinition.FieldName.CHAT_TYPE.toString(), Byte.toString(this.chatType));
      ++this.writeOpCount;
      if (this.unreadMessageCount != null) {
         pipelineStore.hset(this, ChatDefinition.FieldName.UNREAD_MESSAGE_COUNT.toString(), this.unreadMessageCount.toString());
      } else {
         pipelineStore.hdel(this, ChatDefinition.FieldName.UNREAD_MESSAGE_COUNT.toString());
      }

      ++this.writeOpCount;
      if (this.contactID != null) {
         pipelineStore.hset(this, ChatDefinition.FieldName.CONTACT_ID.toString(), this.contactID.toString());
      } else {
         pipelineStore.hdel(this, ChatDefinition.FieldName.CONTACT_ID.toString());
      }

      ++this.writeOpCount;
      if (this.groupOwner != null) {
         pipelineStore.hset(this, ChatDefinition.FieldName.GROUP_OWNER.toString(), this.groupOwner.toString());
      } else {
         pipelineStore.hdel(this, ChatDefinition.FieldName.GROUP_OWNER.toString());
      }

      ++this.writeOpCount;
      if (this.isClosedChat != null) {
         pipelineStore.hset(this, ChatDefinition.FieldName.IS_CLOSED_CHAT.toString(), Byte.toString(this.isClosedChat));
      } else {
         pipelineStore.hdel(this, ChatDefinition.FieldName.IS_CLOSED_CHAT.toString());
      }

      ++this.writeOpCount;
      if (this.displayGUID != null) {
         pipelineStore.hset(this, ChatDefinition.FieldName.DISPLAY_GUID.toString(), this.displayGUID.toString());
      } else {
         pipelineStore.hdel(this, ChatDefinition.FieldName.DISPLAY_GUID.toString());
      }

      ++this.writeOpCount;
      if (this.messageType != null) {
         pipelineStore.hset(this, ChatDefinition.FieldName.MESSAGE_TYPE.toString(), this.messageType.toString());
      } else {
         pipelineStore.hdel(this, ChatDefinition.FieldName.MESSAGE_TYPE.toString());
      }

      ++this.writeOpCount;
      if (this.chatName != null) {
         pipelineStore.hset(this, ChatDefinition.FieldName.CHAT_NAME.toString(), this.chatName.toString());
      } else {
         pipelineStore.hdel(this, ChatDefinition.FieldName.CHAT_NAME.toString());
      }

      ++this.writeOpCount;
      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_WRITE_ENABLED, pipelineStore);
      pipelineStore.execTxn(this);
      ++this.writeOpCount;
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return startIndex + this.writeOpCount;
   }

   public ChatDefinitionIce toIceObject() {
      ChatDefinitionIce cdi = new ChatDefinitionIce();
      cdi.chatStorageID = this.chatStorageID;
      cdi.participantUsernames = this.participantUsernames;
      cdi.chatType = this.chatType;
      cdi.unreadMessageCount = this.unreadMessageCount == null ? Integer.MIN_VALUE : this.unreadMessageCount;
      cdi.contactID = this.contactID == null ? Integer.MIN_VALUE : this.contactID;
      cdi.groupOwner = this.groupOwner;
      cdi.isClosedChat = this.isClosedChat == null ? -128 : this.isClosedChat;
      cdi.displayGUID = this.displayGUID;
      cdi.messageType = this.messageType == null ? -128 : this.messageType;
      cdi.isPassivatedChat = (byte)(this.isPassivatedChat == null ? -128 : (this.isPassivatedChat ? 1 : 0));
      return cdi;
   }

   public Long getLatestMessageTimestamp() {
      return this.latestMessage == null ? null : this.latestMessage.getTimestamp();
   }

   public void setChatName(String chatName) {
      this.chatName = chatName;
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return false;
   }

   public boolean isPrivateChat() {
      return this.chatType != null && this.chatType == (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value() ? true : this.chatStorageID.contains(":");
   }

   public void setIsPassivatedChat(boolean isPassivated) {
      this.isPassivatedChat = isPassivated;
   }

   public boolean isChatRoomDef() {
      return this.chatType != null && this.chatType == (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value() ? true : this.chatStorageID.startsWith("CR#");
   }

   public boolean isGroupChat() {
      return !this.isPrivateChat() && !this.isChatRoomDef();
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ChatDefinition.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }

   public static enum FieldName {
      CHAT_TYPE,
      UNREAD_MESSAGE_COUNT,
      CONTACT_ID,
      GROUP_OWNER,
      IS_CLOSED_CHAT,
      DISPLAY_GUID,
      MESSAGE_TYPE,
      TIMESTAMP,
      CHAT_LIST_VERSION,
      CHAT_LIST_TIMESTAMP,
      CHAT_NAME;
   }

   public class ChatDefinitionNotFoundException extends FusionException {
      private String chatID;

      public String getChatID() {
         return this.chatID;
      }

      public ChatDefinitionNotFoundException(String chatID) {
         this.chatID = chatID;
      }
   }
}
