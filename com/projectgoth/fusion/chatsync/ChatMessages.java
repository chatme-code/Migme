package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import redis.clients.jedis.Tuple;

public class ChatMessages implements ChatSyncEntity {
   private static final LogFilter log;
   protected final ChatDefinition chatID;
   protected final Long oldestTimestamp;
   protected final Long newestTimestamp;
   private final Integer maxMessages;
   private final String parentUsername;
   protected final ChatMessage chatMessageKey;
   protected final MessageStatusEvents statuses;
   protected Integer startRank;
   protected byte[] precedingMessage;
   protected ArrayList<byte[]> retrievedMessages;
   protected List<Tuple> msgs;

   public ChatMessages(ChatDefinition chatID, Long oldestTimestamp, Long newestTimestamp, Integer maxMessages, String parentUsername) throws FusionException {
      this.chatID = chatID;
      this.oldestTimestamp = oldestTimestamp;
      this.newestTimestamp = newestTimestamp;
      this.maxMessages = maxMessages;
      this.parentUsername = parentUsername;
      this.chatMessageKey = new ChatMessage(chatID);
      if (chatID.isPrivateChat()) {
         this.statuses = new MessageStatusEvents(chatID, oldestTimestamp, newestTimestamp, maxMessages, parentUsername);
      } else {
         this.statuses = null;
      }

   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.MESSAGE;
   }

   public String getKey() {
      return this.chatID.getKey();
   }

   public String getValue() {
      return null;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   private void renewExpiry(SystemPropertyEntities.ChatSyncSettings toggle, ChatSyncStore store) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)toggle)) {
         int hrs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_CONTENT_EXPIRY_HOURS);
         store.expire(this, hrs * 3600);
         if (this.statuses != null) {
            store.expire(this.statuses, hrs * 3600);
            String otherParty = this.chatID.internalToExternalChatID(this.parentUsername);
            MessageStatusEventKey otherPartyStatuses = new MessageStatusEventKey(this.chatID, otherParty);
            store.expire(otherPartyStatuses, hrs * 3600);
         }
      }

   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("ChatMessages.retrieve: chatID=" + this.chatID);
      }

      this.retrievedMessages = new ArrayList();
      ChatSyncStore[] arr$ = stores;
      int len$ = stores.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatSyncStore store = arr$[i$];
         store.setMaster();
         this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_READ_ENABLED, store);
         List<ChatSyncPipelineOp> readOps = new ArrayList();
         readOps.add(new ChatSyncPipelineOp(this, ChatSyncPipelineOp.OpType.READ));
         if (this.statuses != null) {
            readOps.add(new ChatSyncPipelineOp(this.statuses, ChatSyncPipelineOp.OpType.READ));
         }

         store.setSlave();
         store.pipelinedBinary((List)readOps);
         if (this.msgs == null) {
            return;
         }

         if (this.msgs.size() != 0) {
            this.startRank = store.zrankBinary(this.chatMessageKey, ((Tuple)this.msgs.get(0)).getBinaryElement());
            if (this.startRank > 0) {
               Set<byte[]> beforeFirst = store.zrangeBinary(this.chatMessageKey, this.startRank - 1, this.startRank - 1);
               ArrayList arr2 = new ArrayList(beforeFirst);
               if (arr2.size() != 0) {
                  this.precedingMessage = (byte[])((byte[])arr2.get(0));
               }
            }

            Iterator i$ = this.msgs.iterator();

            while(i$.hasNext()) {
               Tuple tuple = (Tuple)i$.next();
               this.retrievedMessages.add(tuple.getBinaryElement());
            }
         }
      }

      ChatSyncStats.getInstance().incrementTotalMessagesRetrieved((long)this.retrievedMessages.size());
      if (log.isDebugEnabled()) {
         log.debug("ChatMessages.retrieve: retrieved count=" + this.retrievedMessages.size());
      }

   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
      long lStart = this.oldestTimestamp != null ? this.oldestTimestamp : 0L;
      long lEnd = this.newestTimestamp != null ? this.newestTimestamp : Long.MAX_VALUE;
      if (this.maxMessages != null) {
         pipelineStore.zrevrangeByScoreWithScores(this.chatMessageKey, (double)lEnd, (double)lStart, 0, this.maxMessages);
      } else {
         pipelineStore.zrangeByScoreWithScores(this.chatMessageKey, (double)lStart, (double)lEnd);
      }

   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      if (pipelineResults == null) {
         this.msgs = new ArrayList();
         return startIndex;
      } else {
         this.msgs = new ArrayList((Set)pipelineResults.get(startIndex));
         if (this.maxMessages != null) {
            Collections.reverse(this.msgs);
         }

         return startIndex + 1;
      }
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
      return false;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ChatMessages.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
