package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.MessageDestinationDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.List;
import org.apache.log4j.Logger;

public class MessageSendHandler implements ChatSyncEntity {
   private static final LogFilter log;
   private SessionPrx currentSession;
   private MessageDataIce msg;
   private UserDataIce senderUserData;
   private UserPrx sender;
   private GroupChatPrx groupChat;

   public MessageSendHandler(SessionPrx currentSession, MessageDataIce msg, UserDataIce senderUserData, UserPrx sender, GroupChatPrx groupChat) throws FusionException {
      this.currentSession = currentSession;
      this.msg = msg;
      this.senderUserData = senderUserData;
      this.sender = sender;
      this.groupChat = groupChat;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("onSendFusionMessage: msg=" + this.msg);
      }

      MessageDestinationDataIce dest = this.msg.messageDestinations[0];
      ChatDefinition chatKey = new ChatDefinition(dest.destination, (byte)dest.type, this.msg.source);
      if (log.isDebugEnabled()) {
         log.debug("Constructing ChatMessage to store with key=" + chatKey + " and destination=" + dest.destination + " and dest.type=" + dest.type + " and msg.messageText=" + this.msg.messageText + " and msg.guid=" + this.msg.guid);
         if (this.msg.guid != null) {
            log.debug("and msg.guid.length=" + this.msg.guid.length());
         }
      }

      ChatMessage cm = new ChatMessage(chatKey, this.msg, this.groupChat);
      if (log.isDebugEnabled()) {
         log.debug("Constructed ChatMessage ok: msg.guid=" + this.msg.guid);
      }

      cm.store(stores);
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.MESSAGE;
   }

   public String getKey() {
      return null;
   }

   public String getValue() {
      return null;
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      return 0;
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return 0;
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_STORE_MESSAGE_ENABLED);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageSendHandler.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
