package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;

public class ChatSyncRetrievalExecutor extends ChatSyncExecutor {
   private static final LogFilter log;

   private ChatSyncRetrievalExecutor() {
   }

   public static ChatSyncRetrievalExecutor getInstance() {
      return ChatSyncRetrievalExecutor.SingletonHolder.INSTANCE;
   }

   public Future<Boolean> scheduleEntityRetrieval(ChatSyncEntity entity) throws FusionException {
      ChatSyncStats.getInstance().incrementTotalReadRequests();
      return this.scheduleEntityRetrieval(entity, 0);
   }

   private Future<Boolean> scheduleEntityRetrieval(ChatSyncEntity entity, Integer tryIndex) throws FusionException {
      try {
         int queueSize = this.executor.getQueue().size();
         ChatSyncStats.getInstance().setReadQueueHighWatermark((long)queueSize);
         ChatSyncStats.getInstance().setReadActiveThreads((long)this.executor.getActiveCount());
         int maxQueueSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.TPOOL_TASK_QUEUE_MAX_SIZE);
         if (queueSize <= maxQueueSize) {
            log.debug("Queueing check for chat sync entities");
            return this.executor.submit(new ChatSyncRetrievalExecutor.ChatSyncEntityRetrievalWorker(entity, tryIndex));
         } else {
            log.warn("ChatSyncEntityRetrievalWorker queue too busy (" + queueSize + "/" + maxQueueSize + "). ");
            return null;
         }
      } catch (Exception var5) {
         log.error("While sceduling entity retrieval", var5);
         throw new FusionException("In scheduleEntityRetrieval: " + var5);
      }
   }

   public ChatSyncEntity scheduleEntityRetrievalAndWait(ChatSyncEntity entity) throws FusionException {
      if (entity.canRetryReads()) {
         ChatSyncStats.getInstance().incrementTotalReadRequests();
         int maxTries = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_TRIES_READ_SYNC);

         for(int tryIndex = 0; tryIndex < maxTries; ++tryIndex) {
            if (tryIndex != 0) {
               ChatSyncStats.getInstance().incrementTotalReadRetries();
            }

            ChatSyncEntity result = this.scheduleEntityRetrievalAndWaitInner(entity);
            if (result != null) {
               return result;
            }

            if (tryIndex == 0) {
               ChatSyncStats.getInstance().incrementTotalReadInitialFails();
            }
         }

         ChatSyncStats.getInstance().incrementTotalReadFinalFails();
         return null;
      } else {
         return this.scheduleEntityRetrievalAndWaitInner(entity);
      }
   }

   public ChatSyncEntity scheduleEntityRetrievalAndWaitInner(ChatSyncEntity entity) throws FusionException {
      Future ftr = null;

      try {
         ftr = this.scheduleEntityRetrieval(entity, (Integer)null);
         Boolean result = null;
         if (ftr != null) {
            int timeout = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRIEVAL_TIMEOUT_MILLIS);
            result = (Boolean)ftr.get((long)timeout, TimeUnit.MILLISECONDS);
            return result ? entity : null;
         } else {
            return null;
         }
      } catch (TimeoutException var5) {
         log.error("TimeoutException storing chat sync entity " + entity, var5);
         ftr.cancel(true);
         return null;
      } catch (Exception var6) {
         log.error("exception storing chat sync entity " + entity, var6);
         return null;
      }
   }

   private void retry(ChatSyncEntity entity, int newTryIndex) throws FusionException {
      if (newTryIndex == 1) {
         ChatSyncStats.getInstance().incrementTotalReadInitialFails();
      }

      int maxTries = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_TRIES_READ_ASYNC);
      if (maxTries > 1 && newTryIndex < maxTries) {
         this.scheduleEntityRetrieval(entity, newTryIndex);
         ChatSyncStats.getInstance().incrementTotalReadRetries();
      } else {
         ChatSyncStats.getInstance().incrementTotalReadFinalFails();
      }

   }

   // $FF: synthetic method
   ChatSyncRetrievalExecutor(Object x0) {
      this();
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ChatSyncRetrievalExecutor.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }

   class ChatSyncEntityRetrievalWorker implements Callable<Boolean> {
      private ChatSyncEntity entity;
      private Integer tryIndex;

      ChatSyncEntityRetrievalWorker(ChatSyncEntity entity, Integer tryIndex) {
         this.entity = entity;
         this.tryIndex = tryIndex;
      }

      public Boolean call() {
         long startCpu = ChatSyncStats.getInstance().getCpuTime();

         Boolean var4;
         try {
            ChatSyncStore[] stores = new ChatSyncStore[]{new RedisChatSyncStore(ChatSyncStore.StorePrimacy.MASTER)};
            this.entity.retrieve(stores);
            ChatSyncStats.getInstance().incrementTotalReadSuccesses();
            var4 = true;
            return var4;
         } catch (Exception var11) {
            ChatSyncRetrievalExecutor.log.error("Exception retrieving chat sync entity: " + this.entity, var11);

            try {
               if (this.tryIndex != null && this.entity.canRetryReads()) {
                  ChatSyncRetrievalExecutor.this.retry(this.entity, this.tryIndex + 1);
               }
            } catch (FusionException var10) {
               ChatSyncRetrievalExecutor.log.error("Exception scheduling retry=" + (this.tryIndex + 1) + " of retrieving chatsync entity: " + this.entity, var10);
            }

            var4 = false;
         } finally {
            ChatSyncStats.getInstance().addCpuTimeForRetrieval(startCpu, this.entity.getEntityType());
         }

         return var4;
      }
   }

   private static class SingletonHolder {
      public static final ChatSyncRetrievalExecutor INSTANCE = new ChatSyncRetrievalExecutor();
   }
}
