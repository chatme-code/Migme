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

public class ChatSyncStorageExecutor extends ChatSyncExecutor {
   private static final LogFilter log;
   public static final String STORAGE_FAILURE_USER_MSG = "Due to an internal error, migme was unable to store chat sync info";

   private ChatSyncStorageExecutor() {
   }

   public static ChatSyncStorageExecutor getInstance() {
      return ChatSyncStorageExecutor.SingletonHolder.INSTANCE;
   }

   public ChatSyncStorageExecutor.StorageResult scheduleStorageAndWait(ChatSyncEntity entity) throws FusionException {
      if (entity.canRetryWrites()) {
         ChatSyncStats.getInstance().incrementTotalWriteRequests();
         int maxTries = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_TRIES_WRITE_SYNC);
         ChatSyncStorageExecutor.StorageResult result = new ChatSyncStorageExecutor.StorageResult("Due to an internal error, migme was unable to store chat sync info");

         for(int tryIndex = 0; tryIndex < maxTries; ++tryIndex) {
            if (tryIndex != 0) {
               ChatSyncStats.getInstance().incrementTotalWriteRetries();
            }

            result = this.scheduleStorageAndWaitInner(entity);
            if (!result.failed()) {
               return result;
            }

            if (tryIndex == 0) {
               ChatSyncStats.getInstance().incrementTotalWriteInitialFails();
            }
         }

         ChatSyncStats.getInstance().incrementTotalWriteFinalFails();
         return result;
      } else {
         return this.scheduleStorageAndWaitInner(entity);
      }
   }

   private ChatSyncStorageExecutor.StorageResult scheduleStorageAndWaitInner(ChatSyncEntity entity) {
      ChatSyncStorageExecutor.StorageResult result = new ChatSyncStorageExecutor.StorageResult("Due to an internal error, migme was unable to store chat sync info");
      Future ftr = null;

      try {
         ftr = getInstance().scheduleStorage(entity, (Integer)null);
         if (ftr != null) {
            int timeout = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STORAGE_TIMEOUT_MILLIS);
            result = (ChatSyncStorageExecutor.StorageResult)ftr.get((long)timeout, TimeUnit.MILLISECONDS);
         }

         return result;
      } catch (TimeoutException var5) {
         log.error("TimeoutException storing chat sync entity " + entity, var5);
         ftr.cancel(true);
         return new ChatSyncStorageExecutor.StorageResult("Due to an internal error, migme was unable to store chat sync info");
      } catch (Exception var6) {
         log.error("exception storing chat sync entity " + entity, var6);
         return new ChatSyncStorageExecutor.StorageResult("Due to an internal error, migme was unable to store chat sync info");
      }
   }

   public Future<ChatSyncStorageExecutor.StorageResult> scheduleStorage(ChatSyncEntity entity) throws FusionException {
      ChatSyncStats.getInstance().incrementTotalWriteRequests();
      return this.scheduleStorage(entity, 0);
   }

   private Future<ChatSyncStorageExecutor.StorageResult> scheduleStorage(ChatSyncEntity entity, Integer tryIndex) throws FusionException {
      try {
         int queueSize = this.executor.getQueue().size();
         ChatSyncStats.getInstance().setWriteQueueHighWatermark((long)queueSize);
         ChatSyncStats.getInstance().setWriteActiveThreads((long)this.executor.getActiveCount());
         int maxQueueSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.TPOOL_TASK_QUEUE_MAX_SIZE);
         if (queueSize <= maxQueueSize) {
            log.debug("Queueing storage of entity: " + entity);
            ChatSyncStorageExecutor.ChatSyncStorageWorker worker = new ChatSyncStorageExecutor.ChatSyncStorageWorker(entity, tryIndex);
            return this.executor.submit(worker);
         } else {
            log.warn("ChatSyncExecutor queue too busy (" + queueSize + "/" + maxQueueSize + "). " + "ChatSyncEntity storage failed for entity " + entity);
            return null;
         }
      } catch (Exception var6) {
         log.error("While sceduling entity storage", var6);
         throw new FusionException("In scheduleStorage: " + var6);
      }
   }

   private ChatSyncStorageExecutor.StorageResult storeEntity(ChatSyncEntity entity) throws Exception {
      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      ChatSyncStorageExecutor.StorageResult var6;
      try {
         ChatSyncStore[] stores = new ChatSyncStore[]{new RedisChatSyncStore(ChatSyncStore.StorePrimacy.MASTER)};
         entity.store(stores);
         return new ChatSyncStorageExecutor.StorageResult(ChatSyncStorageExecutor.StorageResult.Value.SUCCESS);
      } catch (Exception var11) {
         log.error("Failed to store chat sync entity " + entity, var11);
         ChatSyncStorageExecutor.StorageResult result = new ChatSyncStorageExecutor.StorageResult(ChatSyncStorageExecutor.StorageResult.Value.FAILURE);
         result.setError("Due to an internal error, migme was unable to store chat sync info");
         var6 = result;
      } finally {
         ChatSyncStats.getInstance().addCpuTimeForStorage(startCpu, entity.getEntityType());
      }

      return var6;
   }

   private void retry(ChatSyncEntity entity, int newTryIndex) throws FusionException {
      if (newTryIndex == 1) {
         ChatSyncStats.getInstance().incrementTotalWriteInitialFails();
      }

      int maxTries = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_TRIES_WRITE_ASYNC);
      if (maxTries > 1 && newTryIndex < maxTries) {
         this.scheduleStorage(entity, newTryIndex);
         ChatSyncStats.getInstance().incrementTotalWriteRetries();
      } else {
         ChatSyncStats.getInstance().incrementTotalWriteFinalFails();
      }

   }

   // $FF: synthetic method
   ChatSyncStorageExecutor(Object x0) {
      this();
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ChatSyncStorageExecutor.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }

   class ChatSyncStorageWorker implements Callable<ChatSyncStorageExecutor.StorageResult> {
      private ChatSyncEntity entity;
      private Integer tryIndex;

      public ChatSyncStorageWorker(ChatSyncEntity entity, Integer tryIndex) throws Exception {
         this.entity = entity;
         this.tryIndex = tryIndex;
      }

      public ChatSyncStorageExecutor.StorageResult call() {
         try {
            ChatSyncStorageExecutor.StorageResult result = ChatSyncStorageExecutor.this.storeEntity(this.entity);
            if (result != null) {
               if (result.failed()) {
                  if (this.tryIndex != null && this.entity.canRetryWrites()) {
                     ChatSyncStorageExecutor.this.retry(this.entity, this.tryIndex + 1);
                  }
               } else {
                  ChatSyncStats.getInstance().incrementTotalWriteSuccesses();
               }
            }

            return result;
         } catch (Exception var4) {
            ChatSyncStorageExecutor.log.error("Unable to store chat sync entity: " + this.entity, var4);

            try {
               if (this.tryIndex != null && this.entity.canRetryWrites()) {
                  ChatSyncStorageExecutor.this.retry(this.entity, this.tryIndex + 1);
               }
            } catch (FusionException var3) {
               ChatSyncStorageExecutor.log.error("Exception scheduling retry=" + (this.tryIndex + 1) + " of chatsync storer: " + this.entity, var3);
            }

            ChatSyncStorageExecutor.StorageResult res = new ChatSyncStorageExecutor.StorageResult(ChatSyncStorageExecutor.StorageResult.Value.FAILURE);
            res.setError("Unable to store chat sync entity");
            return res;
         }
      }
   }

   public static class StorageResult {
      private ChatSyncStorageExecutor.StorageResult.Value value;
      private String error;

      StorageResult(ChatSyncStorageExecutor.StorageResult.Value v) {
         this.value = v;
      }

      StorageResult(String errorArg) {
         this.value = ChatSyncStorageExecutor.StorageResult.Value.FAILURE;
         this.error = errorArg;
      }

      public boolean failed() {
         return this.value == ChatSyncStorageExecutor.StorageResult.Value.FAILURE;
      }

      public ChatSyncStorageExecutor.StorageResult.Value getValue() {
         return this.value;
      }

      public void setError(String s) {
         this.error = s;
      }

      public String getError() {
         return this.error;
      }

      public static enum Value {
         SUCCESS,
         FAILURE;
      }
   }

   private static class SingletonHolder {
      public static final ChatSyncStorageExecutor INSTANCE = new ChatSyncStorageExecutor();
   }
}
