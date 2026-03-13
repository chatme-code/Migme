package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LazyStats;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

public class ChatSyncStats extends LazyStats {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatSyncStats.class));
   private AtomicLong totalGetChatsReceived;
   private AtomicLong totalGetMessagesReceived;
   private AtomicLong totalChatsRetrieved;
   private AtomicLong totalMessagesRetrieved;
   private AtomicLong totalChatsStored;
   private AtomicLong totalMessagesStored;
   private AtomicLong totalGetMessageStatusEventsReceived;
   private WallclockTime getChatsAsyncQueueingWallclockTime;
   private WallclockTime getChatsAsyncWallclockTime;
   private WallclockTime getChatsAsyncPeakWallclockTime;
   private WallclockTime getChatsAsyncOffpeakWallclockTime;
   private WallclockTime getMessagesAsyncQueueingWallclockTime;
   private WallclockTime getMessagesWallclockTime;
   private WallclockTime getMessagesPeakWallclockTime;
   private WallclockTime getMessagesOffpeakWallclockTime;
   private AtomicLong totalReadRequests;
   private AtomicLong totalReadSuccesses;
   private AtomicLong totalReadInitialFails;
   private AtomicLong totalReadRetries;
   private AtomicLong totalReadFinalFails;
   private AtomicLong readQueueHighWatermark;
   private AtomicLong readQueueCumulativeSize;
   private AtomicLong readActiveThreads;
   private AtomicLong totalWriteRequests;
   private AtomicLong totalWriteSuccesses;
   private AtomicLong totalWriteInitialFails;
   private AtomicLong totalWriteRetries;
   private AtomicLong totalWriteFinalFails;
   private AtomicLong writeQueueHighWatermark;
   private AtomicLong writeQueueCumulativeSize;
   private AtomicLong writeActiveThreads;
   private AtomicLong totalRedisOps;
   private AtomicLong totalCpuTime;
   private AtomicLong totalRedisChatStorageOps;
   private AtomicLong totalRedisChatRetrievalOps;
   private AtomicLong totalRedisChatListStorageOps;
   private AtomicLong totalRedisChatListRetrievalOps;
   private AtomicLong totalRedisMessageStorageOps;
   private AtomicLong totalRedisMessageRetrievalOps;
   private AtomicLong totalRedisChatStorageOpsPipelined;
   private AtomicLong totalRedisChatRetrievalOpsPipelined;
   private AtomicLong totalRedisChatListStorageOpsPipelined;
   private AtomicLong totalRedisChatListRetrievalOpsPipelined;
   private AtomicLong totalRedisMessageStorageOpsPipelined;
   private AtomicLong totalRedisMessageRetrievalOpsPipelined;

   protected boolean isStatsEnabled() {
      return true;
   }

   protected int getStatsIntervalMinutes() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STATS_COLLECTION_INTERVAL_MINUTES);
   }

   private ChatSyncStats() {
      this.totalGetChatsReceived = new AtomicLong(0L);
      this.totalGetMessagesReceived = new AtomicLong(0L);
      this.totalChatsRetrieved = new AtomicLong(0L);
      this.totalMessagesRetrieved = new AtomicLong(0L);
      this.totalChatsStored = new AtomicLong(0L);
      this.totalMessagesStored = new AtomicLong(0L);
      this.totalGetMessageStatusEventsReceived = new AtomicLong(0L);
      this.getChatsAsyncQueueingWallclockTime = new WallclockTime();
      this.getChatsAsyncWallclockTime = new WallclockTime();
      this.getChatsAsyncPeakWallclockTime = new WallclockTime();
      this.getChatsAsyncOffpeakWallclockTime = new WallclockTime();
      this.getMessagesAsyncQueueingWallclockTime = new WallclockTime();
      this.getMessagesWallclockTime = new WallclockTime();
      this.getMessagesPeakWallclockTime = new WallclockTime();
      this.getMessagesOffpeakWallclockTime = new WallclockTime();
      this.totalReadRequests = new AtomicLong(0L);
      this.totalReadSuccesses = new AtomicLong(0L);
      this.totalReadInitialFails = new AtomicLong(0L);
      this.totalReadRetries = new AtomicLong(0L);
      this.totalReadFinalFails = new AtomicLong(0L);
      this.readQueueHighWatermark = new AtomicLong(0L);
      this.readQueueCumulativeSize = new AtomicLong(0L);
      this.readActiveThreads = new AtomicLong(0L);
      this.totalWriteRequests = new AtomicLong(0L);
      this.totalWriteSuccesses = new AtomicLong(0L);
      this.totalWriteInitialFails = new AtomicLong(0L);
      this.totalWriteRetries = new AtomicLong(0L);
      this.totalWriteFinalFails = new AtomicLong(0L);
      this.writeQueueHighWatermark = new AtomicLong(0L);
      this.writeQueueCumulativeSize = new AtomicLong(0L);
      this.writeActiveThreads = new AtomicLong(0L);
      this.totalRedisOps = new AtomicLong(0L);
      this.totalCpuTime = new AtomicLong(0L);
      this.totalRedisChatStorageOps = new AtomicLong(0L);
      this.totalRedisChatRetrievalOps = new AtomicLong(0L);
      this.totalRedisChatListStorageOps = new AtomicLong(0L);
      this.totalRedisChatListRetrievalOps = new AtomicLong(0L);
      this.totalRedisMessageStorageOps = new AtomicLong(0L);
      this.totalRedisMessageRetrievalOps = new AtomicLong(0L);
      this.totalRedisChatStorageOpsPipelined = new AtomicLong(0L);
      this.totalRedisChatRetrievalOpsPipelined = new AtomicLong(0L);
      this.totalRedisChatListStorageOpsPipelined = new AtomicLong(0L);
      this.totalRedisChatListRetrievalOpsPipelined = new AtomicLong(0L);
      this.totalRedisMessageStorageOpsPipelined = new AtomicLong(0L);
      this.totalRedisMessageRetrievalOpsPipelined = new AtomicLong(0L);
   }

   public static ChatSyncStats getInstance() {
      return ChatSyncStats.SingletonHolder.INSTANCE;
   }

   public void incrementRedisStats(ChatSyncEntity entity, boolean storage) {
      if (entity instanceof ChatDefinition) {
         if (storage) {
            this.totalRedisChatStorageOps.incrementAndGet();
         } else {
            this.totalRedisChatRetrievalOps.incrementAndGet();
         }
      } else if (entity instanceof ChatList) {
         if (storage) {
            this.totalRedisChatListStorageOps.incrementAndGet();
         } else {
            this.totalRedisChatListRetrievalOps.incrementAndGet();
         }
      } else if (entity instanceof ChatListVersion) {
         if (storage) {
            this.totalRedisChatListStorageOps.incrementAndGet();
         } else {
            this.totalRedisChatListRetrievalOps.incrementAndGet();
         }
      } else if (entity instanceof ChatMessage) {
         if (storage) {
            this.totalRedisChatStorageOps.incrementAndGet();
         } else {
            this.totalRedisChatRetrievalOps.incrementAndGet();
         }
      } else if (entity instanceof ChatMessages) {
         if (storage) {
            this.totalRedisChatStorageOps.incrementAndGet();
         } else {
            this.totalRedisChatRetrievalOps.incrementAndGet();
         }
      } else if (entity instanceof OldChatLists) {
         if (storage) {
            this.totalRedisChatListStorageOps.incrementAndGet();
         } else {
            this.totalRedisChatListRetrievalOps.incrementAndGet();
         }
      } else if (entity instanceof UserChatLists) {
         if (storage) {
            this.totalRedisChatListStorageOps.incrementAndGet();
         } else {
            this.totalRedisChatListRetrievalOps.incrementAndGet();
         }
      }

   }

   public void incrementRedisStatsPipelined(ChatSyncEntity entity, boolean storage) {
      if (entity instanceof ChatDefinition) {
         if (storage) {
            this.totalRedisChatStorageOpsPipelined.incrementAndGet();
         } else {
            this.totalRedisChatRetrievalOpsPipelined.incrementAndGet();
         }
      } else if (entity instanceof ChatList) {
         if (storage) {
            this.totalRedisChatListStorageOpsPipelined.incrementAndGet();
         } else {
            this.totalRedisChatListRetrievalOpsPipelined.incrementAndGet();
         }
      } else if (entity instanceof ChatListVersion) {
         if (storage) {
            this.totalRedisChatListStorageOpsPipelined.incrementAndGet();
         } else {
            this.totalRedisChatListRetrievalOpsPipelined.incrementAndGet();
         }
      } else if (entity instanceof ChatMessage) {
         if (storage) {
            this.totalRedisChatStorageOpsPipelined.incrementAndGet();
         } else {
            this.totalRedisChatRetrievalOpsPipelined.incrementAndGet();
         }
      } else if (entity instanceof ChatMessages) {
         if (storage) {
            this.totalRedisChatStorageOpsPipelined.incrementAndGet();
         } else {
            this.totalRedisChatRetrievalOpsPipelined.incrementAndGet();
         }
      } else if (entity instanceof OldChatLists) {
         if (storage) {
            this.totalRedisChatListStorageOpsPipelined.incrementAndGet();
         } else {
            this.totalRedisChatListRetrievalOpsPipelined.incrementAndGet();
         }
      } else if (entity instanceof UserChatLists) {
         if (storage) {
            this.totalRedisChatListStorageOpsPipelined.incrementAndGet();
         } else {
            this.totalRedisChatListRetrievalOpsPipelined.incrementAndGet();
         }
      }

   }

   public void incrementTotalGetChatsReceived() {
      this.totalGetChatsReceived.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalGetMessagesReceived() {
      this.totalGetMessagesReceived.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalGetMessageStatusEventsReceived() {
      this.totalGetMessageStatusEventsReceived.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalChatsRetrieved() {
      this.totalChatsRetrieved.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalMessagesRetrieved(long delta) {
      this.totalMessagesRetrieved.addAndGet(delta);
      this.logStatsPeriodically();
   }

   public void incrementTotalChatsStored() {
      this.totalChatsStored.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalMessagesStored() {
      this.totalMessagesStored.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalReadRequests() {
      this.totalReadRequests.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalReadSuccesses() {
      this.totalReadSuccesses.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalReadInitialFails() {
      this.totalReadInitialFails.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalReadRetries() {
      this.totalReadRetries.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalReadFinalFails() {
      this.totalReadFinalFails.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void setReadQueueHighWatermark(long readQueueSize) {
      if (readQueueSize > this.readQueueHighWatermark.get()) {
         this.readQueueHighWatermark.set(readQueueSize);
         this.readQueueCumulativeSize.addAndGet(readQueueSize);
      }

      this.logStatsPeriodically();
   }

   public void setReadActiveThreads(long count) {
      this.readActiveThreads.set(count);
      this.logStatsPeriodically();
   }

   public void incrementTotalWriteRequests() {
      this.totalWriteRequests.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalWriteSuccesses() {
      this.totalWriteSuccesses.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalWriteInitialFails() {
      this.totalWriteInitialFails.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalWriteRetries() {
      this.totalWriteRetries.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void incrementTotalWriteFinalFails() {
      this.totalWriteFinalFails.incrementAndGet();
      this.logStatsPeriodically();
   }

   public void setWriteQueueHighWatermark(long queueSize) {
      if (queueSize > this.writeQueueHighWatermark.get()) {
         this.writeQueueHighWatermark.set(queueSize);
         this.writeQueueCumulativeSize.addAndGet(queueSize);
      }

      this.logStatsPeriodically();
   }

   public void setWriteActiveThreads(long count) {
      this.writeActiveThreads.set(count);
      this.logStatsPeriodically();
   }

   public void addGetChatsAsyncQueueingWallclockTime(long millis) {
      this.getChatsAsyncQueueingWallclockTime.addRequest(millis);
      this.logStatsPeriodically();
   }

   public void addGetChatsAsyncWallclockTime(long millis) {
      this.getChatsAsyncWallclockTime.addRequest(millis);
      if (this.isPeakTime()) {
         this.getChatsAsyncPeakWallclockTime.addRequest(millis);
      } else {
         this.getChatsAsyncOffpeakWallclockTime.addRequest(millis);
      }

      this.logStatsPeriodically();
   }

   public void addGetMessagesAsyncQueueingWallclockTime(long millis) {
      this.getMessagesAsyncQueueingWallclockTime.addRequest(millis);
      this.logStatsPeriodically();
   }

   public void addGetMessagesWallclockTime(long millis) {
      this.getMessagesWallclockTime.addRequest(millis);
      if (this.isPeakTime()) {
         this.getMessagesPeakWallclockTime.addRequest(millis);
      } else {
         this.getMessagesOffpeakWallclockTime.addRequest(millis);
      }

      this.logStatsPeriodically();
   }

   public long getCpuTime() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CPU_TIME_STATS_ENABLED) ? super.getCpuTime() : 0L;
   }

   public void addCpuTimeForStorage(long lStartCpuTimeNanos, ChatSyncEntity.ChatSyncEntityType entityType) {
      if (entityType.equals(ChatSyncEntity.ChatSyncEntityType.CONVERSATION)) {
         this.addCpuTime(lStartCpuTimeNanos, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
      } else if (entityType.equals(ChatSyncEntity.ChatSyncEntityType.MESSAGE)) {
         this.addCpuTime(lStartCpuTimeNanos, ChatSyncStats.ChatSyncOpCategory.MESSAGE_STORAGE);
      }

   }

   public void addCpuTimeForRetrieval(long lStartCpuTimeNanos, ChatSyncEntity.ChatSyncEntityType entityType) {
      if (entityType.equals(ChatSyncEntity.ChatSyncEntityType.CONVERSATION)) {
         this.addCpuTime(lStartCpuTimeNanos, ChatSyncStats.ChatSyncOpCategory.CHAT_RETRIEVAL);
      } else if (entityType.equals(ChatSyncEntity.ChatSyncEntityType.MESSAGE)) {
         this.addCpuTime(lStartCpuTimeNanos, ChatSyncStats.ChatSyncOpCategory.MESSAGE_RETRIEVAL);
      }

   }

   public void addCpuTime(long lStartCpuTimeNanos, ChatSyncStats.ChatSyncOpCategory opCategory) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CPU_TIME_STATS_ENABLED)) {
         long delta = this.getCpuTime() - lStartCpuTimeNanos;
         this.totalCpuTime.addAndGet(delta);
         opCategory.addCpuTime(delta);
      }

   }

   protected void doLog() {
      log.info("ChatSync stats:");
      log.info("ChatSync: total GET_CHATS packets received=" + this.totalGetChatsReceived.get());
      log.info("ChatSync: total GET_MESSAGES packets received=" + this.totalGetMessagesReceived.get());
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.WALLCLOCK_TIME_STATS_ENABLED)) {
         log.info("ChatSync: GET_CHATS (async) queueing wallclock time=" + this.getChatsAsyncQueueingWallclockTime);
         log.info("ChatSync: GET_CHATS (async) processing wallclock time=" + this.getChatsAsyncWallclockTime);
         log.info("ChatSync: peak time: GET_CHATS (async) processing wallclock time=" + this.getChatsAsyncPeakWallclockTime);
         log.info("ChatSync: offpeak time: GET_CHATS (async) processing wallclock time=" + this.getChatsAsyncOffpeakWallclockTime);
         log.info("ChatSync: GET_MESSAGES queueing wallclock time=" + this.getMessagesAsyncQueueingWallclockTime);
         log.info("ChatSync: GET_MESSAGES processing wallclock time=" + this.getMessagesWallclockTime);
         log.info("ChatSync: peak time: GET_MESSAGES processing wallclock time=" + this.getMessagesPeakWallclockTime);
         log.info("ChatSync: offpeak time: GET_MESSAGES processing wallclock time=" + this.getMessagesOffpeakWallclockTime);
      }

      log.info("ChatSync: total chats retrieved=" + this.totalChatsRetrieved.get());
      log.info("ChatSync: total messages retrieved=" + this.totalMessagesRetrieved.get());
      log.info("ChatSync: total chats stored=" + this.totalChatsStored.get());
      log.info("ChatSync: total messages stored=" + this.totalMessagesStored.get());
      log.info("ChatSync: total read requests=" + this.totalReadRequests.get());
      log.info("ChatSync: total read successes=" + this.totalReadSuccesses.get());
      log.info("ChatSync: total initial read fails=" + this.totalReadInitialFails.get());
      log.info("ChatSync: total read retries=" + this.totalReadRetries.get());
      log.info("ChatSync: total final read fails=" + this.totalReadFinalFails.get());
      log.info("ChatSync: read active threads=" + this.readActiveThreads.get());
      log.info("ChatSync: read queue high watermark=" + this.readQueueHighWatermark.get());
      if (this.totalReadRequests.get() != 0L) {
         log.info("ChatSync: read queue mean size=" + this.readQueueCumulativeSize.get() / this.totalReadRequests.get());
      }

      log.info("ChatSync: total write requests=" + this.totalWriteRequests.get());
      log.info("ChatSync: total write successes=" + this.totalWriteSuccesses.get());
      log.info("ChatSync: total initial write fails=" + this.totalWriteInitialFails.get());
      log.info("ChatSync: total write retries=" + this.totalWriteRetries.get());
      log.info("ChatSync: total final write fails=" + this.totalWriteFinalFails.get());
      log.info("ChatSync: write active threads=" + this.writeActiveThreads.get());
      log.info("ChatSync: write queue high watermark=" + this.writeQueueHighWatermark.get());
      if (this.totalWriteRequests.get() != 0L) {
         log.info("ChatSync: write queue mean size=" + this.writeQueueCumulativeSize.get() / this.totalWriteRequests.get());
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CPU_TIME_STATS_ENABLED)) {
         double totalCpuSecs = (double)this.totalCpuTime.get() / 1.0E9D;
         log.info("ChatSync: total cpu time=" + totalCpuSecs);
         log.info("ChatSync: total cpu time (chat retrieval)=" + ChatSyncStats.ChatSyncOpCategory.CHAT_RETRIEVAL.getCpuTimeSeconds());
         log.info("ChatSync: total cpu time (chat storage)=" + ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE.getCpuTimeSeconds());
         log.info("ChatSync: total cpu time (message retrieval)=" + ChatSyncStats.ChatSyncOpCategory.MESSAGE_RETRIEVAL.getCpuTimeSeconds());
         log.info("ChatSync: total cpu time (message storage)=" + ChatSyncStats.ChatSyncOpCategory.MESSAGE_STORAGE.getCpuTimeSeconds());
      }

      log.info("ChatSync:");
      log.info("ChatSync: Redis ops (non-pipelined)");
      log.info("ChatSync: chat storage redis ops (non-pipelined)" + this.totalRedisChatStorageOps.get());
      log.info("ChatSync: chat retrieval redis ops (non-pipelined)" + this.totalRedisChatRetrievalOps.get());
      log.info("ChatSync: chatlist storage redis ops (non-pipelined)" + this.totalRedisChatListStorageOps.get());
      log.info("ChatSync: chatlist retrieval redis ops (non-pipelined)" + this.totalRedisChatListRetrievalOps.get());
      log.info("ChatSync: msg storage redis ops (non-pipelined)" + this.totalRedisMessageStorageOps.get());
      log.info("ChatSync: msg retrieval redis ops (non-pipelined)" + this.totalRedisMessageRetrievalOps.get());
      log.info("ChatSync:");
      log.info("ChatSync: Redis ops (pipelined)");
      log.info("ChatSync: chat storage redis ops (pipelined)" + this.totalRedisChatStorageOpsPipelined.get());
      log.info("ChatSync: chat retrieval redis ops (pipelined)" + this.totalRedisChatRetrievalOpsPipelined.get());
      log.info("ChatSync: chatlist storage redis ops (pipelined)" + this.totalRedisChatListStorageOpsPipelined.get());
      log.info("ChatSync: chatlist retrieval redis ops (pipelined)" + this.totalRedisChatListRetrievalOpsPipelined.get());
      log.info("ChatSync: msg storage redis ops (pipelined)" + this.totalRedisMessageStorageOpsPipelined.get());
      log.info("ChatSync: msg retrieval redis ops (pipelined)" + this.totalRedisMessageRetrievalOpsPipelined.get());
   }

   public long getTotalReadRequests() {
      return this.totalReadRequests.get();
   }

   public long getTotalReadSuccesses() {
      return this.totalReadSuccesses.get();
   }

   public long getTotalReadInitialFails() {
      return this.totalReadInitialFails.get();
   }

   public long getTotalReadRetries() {
      return this.totalReadRetries.get();
   }

   public long getTotalReadFinalFails() {
      return this.totalReadFinalFails.get();
   }

   public long getTotalWriteRequests() {
      return this.totalWriteRequests.get();
   }

   public long getTotalWriteSuccesses() {
      return this.totalWriteSuccesses.get();
   }

   public long getTotalWriteInitialFails() {
      return this.totalWriteInitialFails.get();
   }

   public long getTotalWriteRetries() {
      return this.totalWriteRetries.get();
   }

   public long getTotalWriteFinalFails() {
      return this.totalWriteFinalFails.get();
   }

   public WallclockTime getGetChatsAsyncWallclockTime() {
      return this.getChatsAsyncWallclockTime;
   }

   // $FF: synthetic method
   ChatSyncStats(Object x0) {
      this();
   }

   private static class SingletonHolder {
      public static final ChatSyncStats INSTANCE = new ChatSyncStats();
   }

   public static enum ChatSyncOpCategory {
      CHAT_STORAGE,
      CHAT_RETRIEVAL,
      MESSAGE_STORAGE,
      MESSAGE_RETRIEVAL;

      private AtomicLong cpuTime = new AtomicLong(0L);

      public void addCpuTime(long deltaNanos) {
         this.cpuTime.addAndGet(deltaNanos);
      }

      public double getCpuTimeSeconds() {
         return (double)this.cpuTime.get() / 1.0E9D;
      }
   }
}
