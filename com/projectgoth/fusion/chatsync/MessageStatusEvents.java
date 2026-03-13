package com.projectgoth.fusion.chatsync;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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

public class MessageStatusEvents implements ChatSyncEntity {
   private static final LogFilter log;
   /** @deprecated */
   @Deprecated
   protected int writeOpCount;
   protected MessageStatusEventPersistable[] events;
   protected final MessageStatusEvents.Mode mode;
   protected final MessageStatusEventKey eventsKey;
   protected final Long startTime;
   protected final Long endTime;
   protected final Integer maxResults;
   protected long[] messageTimestamps;
   protected String[] messageGuids;

   public MessageStatusEvents(ChatDefinition chatKey, Long startTime, Long endTime, Integer maxResults, String messageSource) throws FusionException {
      this.mode = MessageStatusEvents.Mode.BY_TIMESTAMP_RANGE;
      this.eventsKey = new MessageStatusEventKey(chatKey, messageSource);
      this.startTime = startTime;
      this.endTime = endTime;
      this.maxResults = maxResults;
      this.messageTimestamps = null;
      this.messageGuids = null;
      this.events = null;
   }

   public MessageStatusEvents(ChatDefinition chatKey, long[] messageTimestamps, String[] messageGuids, Integer maxResults, String messageSource) throws FusionException {
      this.mode = MessageStatusEvents.Mode.BY_MESSAGE_LIST;
      this.eventsKey = new MessageStatusEventKey(chatKey, messageSource);
      this.messageTimestamps = messageTimestamps;
      this.messageGuids = messageGuids;
      this.maxResults = maxResults;
      this.startTime = null;
      this.endTime = null;
      this.events = null;
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.MESSAGE_STATUS_EVENT;
   }

   public String getKey() {
      return this.eventsKey.getKey();
   }

   public MessageStatusEventKey getEventsKey() {
      return this.eventsKey;
   }

   public String getValue() {
      return null;
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
      if (this.mode == MessageStatusEvents.Mode.BY_MESSAGE_LIST) {
         this.retrievePipelineByMessageList(pipelineStore);
      } else {
         this.retrievePipelineByTimestampRange(pipelineStore);
      }

   }

   private void retrievePipelineByMessageList(ChatSyncStore pipelineStore) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("retrievePipelineByMessageList with timestamps and guids request params=");
      }

      for(int i = 0; i < this.messageTimestamps.length; ++i) {
         if (log.isDebugEnabled()) {
            log.debug("   timestamp=" + this.messageTimestamps[i] + " guid=" + this.messageGuids[i]);
         }

         pipelineStore.zrangeByScore(this.eventsKey, (double)this.messageTimestamps[i], (double)this.messageTimestamps[i]);
      }

   }

   private void retrievePipelineByTimestampRange(ChatSyncStore pipelineStore) throws FusionException {
      long lStart = this.startTime != null ? this.startTime : 0L;
      long lEnd = this.endTime != null ? this.endTime : Long.MAX_VALUE;
      if (this.maxResults != null) {
         pipelineStore.zrevrangeByScoreWithScores(this.eventsKey, (double)lEnd, (double)lStart, 0, this.maxResults);
      } else {
         pipelineStore.zrangeByScoreWithScores(this.eventsKey, (double)lStart, (double)lEnd);
      }

   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      return this.mode == MessageStatusEvents.Mode.BY_TIMESTAMP_RANGE ? this.loadPipelineByTimestampRange(pipelineResults, startIndex) : this.loadPipelineByMessageList(pipelineResults, startIndex);
   }

   private int loadPipelineByMessageList(List<Object> pipelineResults, int startIndex) throws FusionException {
      if (pipelineResults == null) {
         this.events = new MessageStatusEventPersistable[0];
         return startIndex;
      } else {
         List<MessageStatusEventPersistable> results = new ArrayList();
         if (log.isDebugEnabled()) {
            log.debug("loadPipelineByMessageList :");
         }

         for(int i = 0; i < this.messageTimestamps.length; ++i) {
            Set<String> eventsAtTimestamp = (Set)pipelineResults.get(startIndex + i);
            if (log.isDebugEnabled()) {
               log.debug("Result " + i + " has event count=" + eventsAtTimestamp);
            }

            Iterator i$ = eventsAtTimestamp.iterator();

            while(i$.hasNext()) {
               String gson = (String)i$.next();
               MessageStatusEventPersistable mse = new MessageStatusEventPersistable(this.eventsKey.getChatID(), this.messageTimestamps[i], gson, this.eventsKey.getMessageSource());
               if (log.isDebugEnabled()) {
                  log.debug("requesting guid=" + this.messageGuids[i] + " stored event guid=" + mse.getMessageGUID());
               }

               if (this.messageGuids[i].equals(mse.getMessageGUID())) {
                  if (log.isDebugEnabled()) {
                     log.debug("Adding stored event with guid=" + mse.getMessageGUID() + " status=" + mse.getMessageStatus());
                  }

                  results.add(mse);
               }
            }
         }

         this.events = (MessageStatusEventPersistable[])results.toArray(new MessageStatusEventPersistable[results.size()]);
         return startIndex + this.messageTimestamps.length;
      }
   }

   private int loadPipelineByTimestampRange(List<Object> pipelineResults, int startIndex) throws FusionException {
      if (pipelineResults == null) {
         this.events = new MessageStatusEventPersistable[0];
         return startIndex;
      } else {
         Set<Tuple> storedEventsSet = (Set)pipelineResults.get(startIndex);
         List<Tuple> storedEvents = new ArrayList(storedEventsSet);
         if (this.maxResults != null) {
            Collections.reverse(storedEvents);
         }

         List<MessageStatusEventPersistable> eventsList = new ArrayList();
         Iterator i$ = storedEvents.iterator();

         while(i$.hasNext()) {
            Tuple tuple = (Tuple)i$.next();
            long timestamp = (long)tuple.getScore();
            String gson = tuple.getElement();
            MessageStatusEventPersistable mse = new MessageStatusEventPersistable(this.eventsKey.getChatID(), timestamp, gson, this.eventsKey.getMessageSource());
            eventsList.add(mse);
         }

         this.events = (MessageStatusEventPersistable[])eventsList.toArray(new MessageStatusEventPersistable[eventsList.size()]);
         return startIndex + 1;
      }
   }

   public boolean canRetryReads() throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_GET_MESSAGE_STATUS_EVENTS_ENABLED);
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      throw new FusionException("Unimplemented");
   }

   public boolean canRetryWrites() throws FusionException {
      throw new FusionException("Unimplemented");
   }

   public Multimap<Long, MessageStatusEvent> getEventsByTimestamp() {
      Multimap<Long, MessageStatusEvent> map = ArrayListMultimap.create();
      MessageStatusEventPersistable[] arr$ = this.events;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MessageStatusEvent event = arr$[i$];
         map.put(event.getMessageTimestamp(), event);
      }

      return map;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageStatusEvents.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }

   private static enum Mode {
      BY_TIMESTAMP_RANGE,
      BY_MESSAGE_LIST;
   }
}
