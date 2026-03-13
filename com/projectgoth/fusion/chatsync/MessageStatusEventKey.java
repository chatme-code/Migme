package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.slice.FusionException;
import java.util.List;
import org.apache.log4j.Logger;

public class MessageStatusEventKey implements ChatSyncEntity {
   private static final LogFilter log;
   protected final ChatDefinition chatID;
   protected String messageSource;
   protected String messageDestination;

   protected MessageStatusEventKey() {
      this.chatID = null;
   }

   public MessageStatusEventKey(ChatDefinition chatID, String messageSource) throws FusionException {
      this(chatID, messageSource, chatID.internalToExternalChatID(messageSource));
   }

   public MessageStatusEventKey(ChatDefinition chatID, String messageSource, String messageDestination) {
      this.chatID = chatID;
      this.messageSource = messageSource;
      this.messageDestination = messageDestination;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.MESSAGE_STATUS_EVENT;
   }

   public ChatDefinition getChatID() {
      return this.chatID;
   }

   public String getMessageSource() {
      return this.messageSource;
   }

   public String getMessageDestination() {
      return this.messageDestination;
   }

   public MessageDestinationData.TypeEnum getMessageDestinationType() {
      return MessageDestinationData.TypeEnum.fromValue(this.chatID.getChatType());
   }

   public String getKey() {
      String suffix = this.messageSource.compareTo(this.messageDestination) < 0 ? "A" : "B";
      return this.chatID.getKey() + ":" + suffix;
   }

   public String getChatKey() {
      return this.chatID.getKey();
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
      throw new FusionException("Unimplemented");
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      throw new FusionException("Unimplemented");
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return false;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageStatusEventKey.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
