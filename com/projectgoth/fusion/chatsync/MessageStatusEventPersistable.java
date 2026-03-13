package com.projectgoth.fusion.chatsync;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.gateway.packet.FusionPktMessage;
import com.projectgoth.fusion.gateway.packet.FusionPktMessageStatusEvent;
import com.projectgoth.fusion.slice.FusionException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class MessageStatusEventPersistable extends MessageStatusEvent {
   private static final LogFilter log;
   /** @deprecated */
   @Deprecated
   private int writeOpCount = 0;
   @Expose
   private int persistedMessageStatus;
   @Expose
   private int persistedServerGenerated;

   public int getPersistedMessageStatus() {
      return this.persistedMessageStatus;
   }

   public int getPersistedServerGenerated() {
      return this.persistedServerGenerated;
   }

   public MessageStatusEventPersistable() throws FusionException {
   }

   public MessageStatusEventPersistable(MessageStatusEventPersistable msep) throws FusionException {
      super((MessageStatusEvent)msep);
   }

   public MessageStatusEventPersistable(FusionPktMessageStatusEvent pkt) throws FusionException {
      super(pkt);
   }

   public MessageStatusEventPersistable(ChatDefinition chatKey, long msgTimestamp, String mseGson, String requestor) throws FusionException {
      super(chatKey, requestor);
      Gson gson = (new GsonBuilder()).setFieldNamingStrategy(MessageStatusEventPersistable.MSEStrategy.getInstance()).create();
      MessageStatusEventPersistable unpacked = (MessageStatusEventPersistable)gson.fromJson(mseGson, MessageStatusEventPersistable.class);
      this.messageType = MessageType.FUSION;
      this.messageSource = requestor;
      this.messageDestination = chatKey.internalToExternalChatID(requestor);
      this.messageGUID = unpacked.getMessageGUID();
      this.messageStatus = Enums.MessageStatusEventTypeEnum.fromValue(unpacked.getPersistedMessageStatus());
      this.serverGenerated = unpacked.getPersistedServerGenerated() == 1;
      this.messageTimestamp = msgTimestamp;
   }

   public MessageStatusEventPersistable(FusionPktMessage msg, Enums.MessageStatusEventTypeEnum status, boolean serverGenerated) throws FusionException {
      super(new ChatDefinition(msg.getDestination(), msg.getDestinationType(), msg.getSource()), msg.getSource());
      this.messageType = MessageType.fromValue(msg.getMessageType());
      this.messageSource = msg.getSource();
      this.messageDestination = msg.getDestination();
      this.messageGUID = msg.getGUID();
      this.messageStatus = status;
      this.serverGenerated = serverGenerated;
      this.messageTimestamp = msg.getTimestamp();
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("MessageStatusEventPersistable.store for guid=" + this.messageGUID);
      }

      List<ChatSyncPipelineOp> writeOps = new ArrayList();
      writeOps.add(new ChatSyncPipelineOp(this, ChatSyncPipelineOp.OpType.WRITE));
      ChatSyncStore[] arr$ = stores;
      int len$ = stores.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatSyncStore store = arr$[i$];
         store.setMaster();
         store.pipelined((List)writeOps);
      }

   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.MESSAGE_STATUS_EVENT;
   }

   public String getValue() {
      this.persistedMessageStatus = this.messageStatus.value();
      this.persistedServerGenerated = this.serverGenerated ? 1 : 0;
      Gson gson = (new GsonBuilder()).excludeFieldsWithoutExposeAnnotation().setFieldNamingStrategy(MessageStatusEventPersistable.MSEStrategy.getInstance()).create();
      return gson.toJson(this);
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      ChatSyncStore[] arr$ = stores;
      int len$ = stores.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatSyncStore store = arr$[i$];
         List<ChatSyncPipelineOp> readOps = new ArrayList();
         readOps.add(new ChatSyncPipelineOp(this, ChatSyncPipelineOp.OpType.READ));
         store.setSlave();
         store.pipelined((List)readOps);
      }

   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
      pipelineStore.zrangeByScore(this, (double)this.messageTimestamp, (double)this.messageTimestamp);
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      List<String> eventsAtTimestamp = (List)pipelineResults.get(startIndex);
      Enums.MessageStatusEventTypeEnum highestStatusForMsg = Enums.MessageStatusEventTypeEnum.COMPOSING;
      Iterator i$ = eventsAtTimestamp.iterator();

      while(i$.hasNext()) {
         String gson = (String)i$.next();
         MessageStatusEventPersistable mse = new MessageStatusEventPersistable(this.chatID, this.messageTimestamp, gson, this.messageSource);
         if (this.messageGUID.equals(mse.getMessageGUID()) && mse.getMessageStatus().value() > highestStatusForMsg.value()) {
            this.messageType = mse.getMessageType();
            this.messageSource = mse.getMessageSource();
            this.messageDestination = mse.getMessageDestination();
            this.messageStatus = highestStatusForMsg = mse.getMessageStatus();
            this.serverGenerated = mse.getServerGenerated();
         }
      }

      return startIndex + 1;
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Storing MessageStatusEvent for msg guid=" + this.messageGUID);
      }

      pipelineStore.zadd(this, (double)this.messageTimestamp, (String)this.getValue());
      ++this.writeOpCount;
      if (this.getMessageStatus() == Enums.MessageStatusEventTypeEnum.READ) {
         MessageStatusEventPersistable received = new MessageStatusEventPersistable(this);
         received.setMessageStatus(Enums.MessageStatusEventTypeEnum.RECEIVED);
         pipelineStore.zrem(received, received.getValue());
         ++this.writeOpCount;
      }

      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_WRITE_ENABLED, pipelineStore);
   }

   private void renewExpiry(SystemPropertyEntities.ChatSyncSettings toggle, ChatSyncStore store) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)toggle)) {
         int hrs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_CONTENT_EXPIRY_HOURS);
         store.expire(this, hrs * 3600);
         ++this.writeOpCount;
      }

   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return startIndex + this.writeOpCount;
   }

   public boolean canRetryReads() throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_GET_MESSAGE_STATUS_EVENTS_ENABLED);
   }

   public boolean canRetryWrites() throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_STORE_MESSAGE_STATUS_EVENTS_ENABLED);
   }

   /** @deprecated */
   @Deprecated
   public int getWriteOpCount() {
      return this.writeOpCount;
   }

   public void setMessageStatus(Enums.MessageStatusEventTypeEnum status) {
      this.messageStatus = status;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageStatusEventPersistable.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }

   protected static class MSEStrategy implements FieldNamingStrategy {
      private HashMap<String, String> map;

      public static MessageStatusEventPersistable.MSEStrategy getInstance() {
         return MessageStatusEventPersistable.MSEStrategy.SingletonHolder.INSTANCE;
      }

      private MSEStrategy() {
         this.map = new HashMap();
         this.map.put("messageGUID", "G");
         this.map.put("persistedMessageStatus", "S");
         this.map.put("persistedServerGenerated", "W");
      }

      public String translateName(Field field) {
         String alias = (String)this.map.get(field.getName());
         return alias != null ? alias : field.getName();
      }

      // $FF: synthetic method
      MSEStrategy(Object x0) {
         this();
      }

      private static class SingletonHolder {
         public static final MessageStatusEventPersistable.MSEStrategy INSTANCE = new MessageStatusEventPersistable.MSEStrategy();
      }
   }
}
