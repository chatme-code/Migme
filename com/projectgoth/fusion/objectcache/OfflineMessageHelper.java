package com.projectgoth.fusion.objectcache;

import com.google.gson.Gson;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.PipelineBlock;

public class OfflineMessageHelper {
   public static final String STORAGE_FAILURE_USER_MSG = "The recipient is offline and due to an internal error, migme was unable to store your message as an offline message";
   private static final String LIMIT_REACHED_MSG = "You have reached your limit for offline messaging for today";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(OfflineMessageHelper.class));
   private ThreadPoolExecutor executor;
   private AtomicLong lastSentStatsLoggingTime;
   private AtomicLong totalMessagesStored;
   private AtomicLong totalMessagesPushedToMigbo;
   private AtomicLong lastReceivedStatsLoggingTime;
   private AtomicLong totalMessagesRetrieved;
   private AtomicLong totalMessagesDelivered;
   private AtomicLong totalMessagesDropped;

   private OfflineMessageHelper() {
      this.lastSentStatsLoggingTime = new AtomicLong(0L);
      this.totalMessagesStored = new AtomicLong(0L);
      this.totalMessagesPushedToMigbo = new AtomicLong(0L);
      this.lastReceivedStatsLoggingTime = new AtomicLong(0L);
      this.totalMessagesRetrieved = new AtomicLong(0L);
      this.totalMessagesDelivered = new AtomicLong(0L);
      this.totalMessagesDropped = new AtomicLong(0L);
      this.executor = new ThreadPoolExecutor(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_CORE_SIZE), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_MAX_SIZE), (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_KEEP_ALIVE_SECONDS), TimeUnit.SECONDS, new LinkedBlockingQueue());
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.REFRESH_THREAD_POOL_PROPERTIES_ENABLED)) {
         (new OfflineMessageHelper.ThreadPoolPropsRefreshThread()).start();
      }

   }

   public static OfflineMessageHelper getInstance() {
      return OfflineMessageHelper.SingletonHolder.INSTANCE;
   }

   public OfflineMessageHelper.StorageResult scheduleOfflineMessageStorageAndWait(MessageData message, int senderID, int recipientID) {
      MessageDestinationData dest = (MessageDestinationData)message.messageDestinations.get(0);
      OfflineMessageHelper.StorageResult result = new OfflineMessageHelper.StorageResult("The recipient is offline and due to an internal error, migme was unable to store your message as an offline message");
      Future ftr = null;

      try {
         ftr = getInstance().scheduleOfflineMessageStorage(message, dest, senderID, recipientID);
         if (ftr != null) {
            int timeout = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.STORAGE_TIMEOUT_MILLIS);
            result = (OfflineMessageHelper.StorageResult)ftr.get((long)timeout, TimeUnit.MILLISECONDS);
         }

         return result;
      } catch (TimeoutException var8) {
         log.error("TimeoutException storing offline message", var8);
         ftr.cancel(true);
         return new OfflineMessageHelper.StorageResult("The recipient is offline and due to an internal error, migme was unable to store your message as an offline message");
      } catch (Exception var9) {
         log.error("exception storing offline message", var9);
         return new OfflineMessageHelper.StorageResult("The recipient is offline and due to an internal error, migme was unable to store your message as an offline message");
      }
   }

   public Future<OfflineMessageHelper.StorageResult> scheduleOfflineMessageStorage(MessageData msg, MessageDestinationData destination, int senderID, int recipientID) throws Exception {
      if (msg.contentType != MessageData.ContentTypeEnum.TEXT && msg.contentType != MessageData.ContentTypeEnum.EMOTE) {
         throw new FusionException("Offline messaging only implemented for TEXT and some EMOTE content types, not for " + msg.contentType);
      } else if (destination.type != MessageDestinationData.TypeEnum.INDIVIDUAL) {
         throw new FusionException("Offline messaging only implemented for private messaging, not for " + destination.type);
      } else {
         int queueSize = this.executor.getQueue().size();
         int maxQueueSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_TASK_QUEUE_MAX_SIZE);
         if (queueSize <= maxQueueSize) {
            log.debug("Queueing storage of offline message for user: " + destination.destination);
            OfflineMessageHelper.OfflineMessageStorageWorker worker = new OfflineMessageHelper.OfflineMessageStorageWorker(msg, destination, senderID, recipientID);
            return this.executor.submit(worker);
         } else {
            log.warn("OfflineMessageHelper queue too busy (" + queueSize + "/" + maxQueueSize + "). " + "Offline message storage failed for recipient " + destination.destination);
            return null;
         }
      }
   }

   public Future<Boolean> scheduleCheckForOfflineMessages(UserDataIce recipientData, SessionPrx session, ClientType deviceType, short clientVersion, IcePrxFinder ipf) throws Exception {
      int minMigLevel;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.OFFLINE_MESSAGE_GUARDSET_ENABLED)) {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         if (!userEJB.isUserInMigboAccessList(recipientData.userID, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.OFFLINE_MESSAGING.value())) {
            log.debug("Cannot check for offline messages: " + recipientData.username + " not in offline messaging guardset");
            return null;
         }

         log.debug("Can check for offline messages: " + recipientData.username + " is in offline messaging guardset");
      } else {
         minMigLevel = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.MIN_MIG_LEVEL_FOR_RECEIVING);
         if (minMigLevel != 0) {
            log.debug("OLM receiving control by mig level enabled");
            Integer level = MemCacheOrEJB.getUserReputationLevel(recipientData.userID);
            if (level == null) {
               level = 1;
            }

            if (level >= minMigLevel) {
               log.debug("Cannot check for offline messages: " + recipientData.username + " has mig level " + level + " whereas min mig level for receiving OLM=" + minMigLevel);
               return null;
            }
         } else {
            log.debug("Can check for offline messages: offline msging guardset is disabled  and no control by mig level");
         }
      }

      minMigLevel = this.executor.getQueue().size();
      int maxQueueSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_TASK_QUEUE_MAX_SIZE);
      if (minMigLevel <= maxQueueSize) {
         log.debug("Queueing check for offline messages for user: " + recipientData.username);
         return this.executor.submit(new OfflineMessageHelper.OfflineMessageRetrievalWorker(recipientData, session, deviceType, clientVersion, ipf));
      } else {
         log.warn("OfflineMessageRetrievalWorker queue too busy (" + minMigLevel + "/" + maxQueueSize + "). " + "Deferring offline message check until next login for user " + recipientData.username);
         return null;
      }
   }

   private void refreshThreadPoolExecutorProps() {
      int coreSizeProp = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_CORE_SIZE);
      int maxSizeProp = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_MAX_SIZE);
      int keepAliveSecondsProp = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_KEEP_ALIVE_SECONDS);
      if (this.executor.getCorePoolSize() != coreSizeProp) {
         log.info("Thread pool core size param changed: updating to=" + coreSizeProp);
         this.executor.setCorePoolSize(coreSizeProp);
      }

      if (this.executor.getMaximumPoolSize() != maxSizeProp) {
         log.info("Thread pool max size param changed: updating to=" + maxSizeProp);
         this.executor.setMaximumPoolSize(maxSizeProp);
      }

      if (this.executor.getKeepAliveTime(TimeUnit.SECONDS) != (long)keepAliveSecondsProp) {
         log.info("Thread pool keepalive time param changed: updating to=" + keepAliveSecondsProp);
         this.executor.setKeepAliveTime((long)keepAliveSecondsProp, TimeUnit.SECONDS);
      }

   }

   private OfflineMessageHelper.StorageResult storeOfflineMessage(MessageData msg, MessageDestinationData destination, int senderID, int recipientID) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("Storing offline message to user destDest=" + destination.destination + " with id:" + recipientID + " and msg.username=" + msg.username + " and sender user id=" + senderID);
      }

      Jedis recipientInstance = null;

      OfflineMessageHelper.StorageResult var8;
      try {
         final String msgKey = Redis.getOfflineMessageKey(recipientID);
         log.debug("Storing offline message under redis key=" + msgKey);
         Gson gson = new Gson();
         final String gsonMsg = gson.toJson(msg);
         recipientInstance = Redis.getMasterInstanceForUserID(recipientID);
         List<Object> results = recipientInstance.pipelined(new PipelineBlock() {
            public void execute() {
               this.rpush(msgKey, new String[]{gsonMsg});
               this.expire(msgKey, 86400 * SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.EXPIRY_DAYS));
            }
         });
         int recipientListSize = ((Long)results.get(0)).intValue();
         log.debug("Recipient " + recipientID + " now has " + recipientListSize + " offline msgs for today");
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.STATS_COLLECTION_ENABLED)) {
            this.totalMessagesStored.incrementAndGet();
            this.logSentStatsPeriodically();
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.PUSH_TO_MIGBO_ENABLED)) {
            try {
               String pathPrefix = "/user/" + senderID + "/messages/@new";
               MigboApiUtil apiUtil = MigboApiUtil.getInstance();
               String jsonStr = "{\"destinations\":[" + recipientID + "],\"message\":{\"_version\":\"1.0\",\"body\":\"" + msg.messageText + "\"}}";
               if (log.isDebugEnabled()) {
                  log.debug("Making API call to POST " + pathPrefix + " with senderID=" + senderID + " and jsonStr=" + jsonStr);
               }

               boolean ok = apiUtil.postAndCheckOk(pathPrefix, jsonStr);
               log.debug("Made API call to POST " + pathPrefix + " with success=" + ok);
               if (!ok) {
                  log.info("Completed but returned false: Migbo POST to " + pathPrefix + " jsonStr=" + jsonStr);
               }

               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.STATS_COLLECTION_ENABLED)) {
                  this.totalMessagesPushedToMigbo.incrementAndGet();
                  this.logSentStatsPeriodically();
                  return new OfflineMessageHelper.StorageResult(OfflineMessageHelper.StorageResult.Value.SUCCESS);
               }
            } catch (Exception var20) {
               String exmsg = "Failed (exception thrown) to post offline message to migbo private messaging from senderID=" + senderID + " to recipientID=" + recipientID;
               if (var20.toString().contains("You can only private message your friends, followers and people you follow")) {
                  log.info(exmsg, var20);
               } else {
                  log.error(exmsg, var20);
               }

               return new OfflineMessageHelper.StorageResult(OfflineMessageHelper.StorageResult.Value.SUCCESS);
            }

            return new OfflineMessageHelper.StorageResult(OfflineMessageHelper.StorageResult.Value.SUCCESS);
         }

         return new OfflineMessageHelper.StorageResult(OfflineMessageHelper.StorageResult.Value.SUCCESS);
      } catch (Exception var21) {
         log.error("Failed to store offline message for recipient [" + destination.destination + "]" + var21);
         OfflineMessageHelper.StorageResult result = new OfflineMessageHelper.StorageResult(OfflineMessageHelper.StorageResult.Value.FAILURE);
         result.setError("The recipient is offline and due to an internal error, migme was unable to store your message as an offline message");
         var8 = result;
      } finally {
         Redis.disconnect(recipientInstance, log);
      }

      return var8;
   }

   private void checkOfflineMessages(UserDataIce userData, SessionPrx session, ClientType deviceType, short clientVersion, IcePrxFinder ipf) {
      log.debug("Entering checkPendingOfflineMessages");
      Jedis ji = null;

      try {
         ji = Redis.getMasterInstanceForUserID(userData.userID);
         this.checkOfflineMessagesInner(userData, session, deviceType, clientVersion, ipf, ji);
      } catch (Exception var12) {
         log.error("Exception checking offline messages for user " + userData.userID + " " + var12);
      } finally {
         Redis.disconnect(ji, log);
      }

   }

   private void checkOfflineMessagesInner(final UserDataIce userData, SessionPrx session, ClientType deviceType, short clientVersion, IcePrxFinder ipf, Jedis ji) throws Exception {
      List<Object> msgsAndDelResults = ji.pipelined(new PipelineBlock() {
         public void execute() {
            Date lastLogin = new Date(userData.lastLoginDate);
            int maxDays = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.EXPIRY_DAYS);
            Calendar calEarliest = Calendar.getInstance();
            calEarliest.setTime(new Date());
            calEarliest.add(5, -1 * maxDays);
            Date earliestDate = calEarliest.getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            do {
               String key = Redis.getOfflineMessageKey(userData.userID, cal.getTime());
               OfflineMessageHelper.log.debug("Pipelining check for: " + key);
               this.lrange(key, 0L, -1L);
               this.del(key);
               cal.add(5, -1);
            } while(cal.getTime().after(lastLogin) && cal.getTime().after(earliestDate));

         }
      });
      int maxOlmsgRetrievalsPerLogin;
      if (deviceType == ClientType.ANDROID && clientVersion >= 300) {
         maxOlmsgRetrievalsPerLogin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.RX_LIMIT_ANDROID_3PLUS);
      } else if (deviceType == ClientType.BLACKBERRY) {
         maxOlmsgRetrievalsPerLogin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.RX_LIMIT_BLACKBERRY);
      } else if (deviceType == ClientType.MIDP2 && clientVersion >= 420) {
         maxOlmsgRetrievalsPerLogin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.RX_LIMIT_J2ME);
      } else if (deviceType == ClientType.MRE) {
         maxOlmsgRetrievalsPerLogin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.RX_LIMIT_MRE);
      } else {
         maxOlmsgRetrievalsPerLogin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.RX_LIMIT_DEFAULT);
      }

      log.debug("Recipient " + userData.username + ": using max offline message retrievals per login=" + maxOlmsgRetrievalsPerLogin);
      Gson gson = new Gson();
      int rxCount = 0;
      ArrayList<MessageDataIce> msgsToResend = new ArrayList();

      String alertKey;
      for(int daysBefore = 0; daysBefore * 2 < msgsAndDelResults.size(); ++daysBefore) {
         List<String> dayList = (List)msgsAndDelResults.get(daysBefore * 2);
         if (dayList.size() != 0) {
            for(int i = dayList.size() - 1; i >= 0; --i) {
               alertKey = (String)dayList.get(i);
               MessageData md = (MessageData)gson.fromJson(alertKey, MessageData.class);
               if (rxCount < maxOlmsgRetrievalsPerLogin) {
                  log.debug("Converted json blob back to MessageData with source=" + md.source + " and username=" + md.username);
                  MessageDataIce mdi = md.toIceObject();
                  msgsToResend.add(0, mdi);
               }

               ++rxCount;
            }
         }
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.STATS_COLLECTION_ENABLED)) {
         this.totalMessagesRetrieved.addAndGet((long)rxCount);
         this.totalMessagesDropped.addAndGet((long)(rxCount - msgsToResend.size()));
         this.logReceivedStatsPeriodically();
      }

      Iterator i$ = msgsToResend.iterator();

      while(i$.hasNext()) {
         MessageDataIce mdi = (MessageDataIce)i$.next();
         session.getUserProxy(userData.username).putMessage(mdi);
         log.debug("Resent offline message id=" + mdi.id + " to " + userData.username);
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.CHECK_YOUR_MIGBO_MSGS_ALERT_ENABLED) && rxCount > maxOlmsgRetrievalsPerLogin) {
         String msg = "You've got " + (rxCount - maxOlmsgRetrievalsPerLogin) + " more offline messages! To see them, please check your migbo messages";
         boolean oldMidpClient = deviceType == ClientType.MIDP2 && clientVersion < 420;
         if (oldMidpClient) {
            log.debug("Sending via FusionPktAlert: " + msg);
            session.putAlertMessage(msg, "Offline messages", (short)1000);
         } else {
            try {
               log.debug("Sending to " + userData.username + " via migbo alert: " + msg);
               UserNotificationServicePrx uns = ipf.getUserNotificationServiceProxy();
               log.debug("Got UserNotificationServicePrx: uns=" + uns);
               alertKey = userData.username + ":OLM";
               Map<String, String> messageMap = new HashMap();
               messageMap.put("alert_key", alertKey);
               messageMap.put("alert_content", msg);
               uns.notifyFusionUser(new Message(alertKey, userData.userID, userData.username, Enums.NotificationTypeEnum.SYS_ALERT.getType(), System.currentTimeMillis(), messageMap));
               log.debug("Sent via migbo alert: " + msg);
            } catch (Exception var18) {
               log.error("While sending via UNS: " + var18);
               if (var18.getCause() != null) {
                  log.error("UNS exception cause=" + var18.getCause());
               }
            }
         }
      }

   }

   private void logSentStatsPeriodically() {
      synchronized(this) {
         if (System.currentTimeMillis() - this.lastSentStatsLoggingTime.get() > (long)(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.STATS_COLLECTION_INTERVAL_MINUTES) * 60 * 1000)) {
            log.info("OLM: Offline messaging stats (sending):");
            log.info("OLM: total messages sent=" + this.totalMessagesStored.get());
            log.info("OLM: total messages pushed to migbo=" + this.totalMessagesPushedToMigbo.get());
            this.lastSentStatsLoggingTime.set(System.currentTimeMillis());
         }

      }
   }

   private void logReceivedStatsPeriodically() {
      synchronized(this) {
         if (System.currentTimeMillis() - this.lastReceivedStatsLoggingTime.get() > (long)(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.STATS_COLLECTION_INTERVAL_MINUTES) * 60 * 1000)) {
            log.info("OLM: Offline messaging stats (receiving):");
            log.info("OLM: total messages retrieved=" + this.totalMessagesRetrieved.get());
            log.info("OLM: total messages retrieved (delivered)=" + this.totalMessagesDelivered.get());
            log.info("OLM: total messages retrieved (dropped)=" + this.totalMessagesDropped.get());
            this.lastReceivedStatsLoggingTime.set(System.currentTimeMillis());
         }

      }
   }

   // $FF: synthetic method
   OfflineMessageHelper(Object x0) {
      this();
   }

   class OfflineMessageRetrievalWorker implements Callable<Boolean> {
      private UserDataIce recipientData;
      private SessionPrx session;
      private ClientType deviceType;
      private short clientVersion;
      private IcePrxFinder icePrxFinder;

      OfflineMessageRetrievalWorker(UserDataIce recipientData, SessionPrx session, ClientType deviceType, short clientVersion, IcePrxFinder ipf) {
         this.recipientData = recipientData;
         this.session = session;
         this.deviceType = deviceType;
         this.clientVersion = clientVersion;
         this.icePrxFinder = ipf;
      }

      public Boolean call() {
         try {
            OfflineMessageHelper.this.checkOfflineMessages(this.recipientData, this.session, this.deviceType, this.clientVersion, this.icePrxFinder);
            return true;
         } catch (Exception var2) {
            OfflineMessageHelper.log.error("Unable to check offline messages for user: " + this.recipientData.username, var2);
            return false;
         }
      }
   }

   class OfflineMessageStorageWorker implements Callable<OfflineMessageHelper.StorageResult> {
      private MessageData msg;
      private MessageDestinationData destination;
      private int senderID;
      private int recipientID;

      public OfflineMessageStorageWorker(MessageData msg, MessageDestinationData destination, int senderID, int recipientID) throws Exception {
         this.msg = msg;
         this.destination = destination;
         this.senderID = senderID;
         this.recipientID = recipientID;
      }

      public OfflineMessageHelper.StorageResult call() {
         try {
            return OfflineMessageHelper.this.storeOfflineMessage(this.msg, this.destination, this.senderID, this.recipientID);
         } catch (Exception var3) {
            OfflineMessageHelper.log.error("Unable to store offline messages for destination: " + this.destination.destination, var3);
            OfflineMessageHelper.StorageResult res = new OfflineMessageHelper.StorageResult(OfflineMessageHelper.StorageResult.Value.FAILURE);
            res.setError("We weren't able to store your offline message this time, please try later!");
            return res;
         }
      }
   }

   private class ThreadPoolPropsRefreshThread extends Thread {
      private ThreadPoolPropsRefreshThread() {
      }

      public void run() {
         while(true) {
            if (!Thread.currentThread().isInterrupted()) {
               OfflineMessageHelper.this.refreshThreadPoolExecutorProps();

               try {
                  Thread.sleep((long)(1000 * SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.TPOOL_PROPS_REFRESH_INTERVAL_SECS)));
                  continue;
               } catch (InterruptedException var2) {
                  OfflineMessageHelper.log.info("ThreadPoolPropsRefreshThread interrupted and shutting down");
                  Thread.currentThread().interrupt();
               }
            }

            OfflineMessageHelper.log.info("ThreadPoolPropsRefreshThread exiting");
            return;
         }
      }

      // $FF: synthetic method
      ThreadPoolPropsRefreshThread(Object x1) {
         this();
      }
   }

   public static class StorageResult {
      private OfflineMessageHelper.StorageResult.Value value;
      private String error;

      StorageResult(OfflineMessageHelper.StorageResult.Value v) {
         this.value = v;
      }

      StorageResult(String errorArg) {
         this.value = OfflineMessageHelper.StorageResult.Value.FAILURE;
         this.error = errorArg;
      }

      public boolean failed() {
         return this.value == OfflineMessageHelper.StorageResult.Value.FAILURE;
      }

      public OfflineMessageHelper.StorageResult.Value getValue() {
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
      public static final OfflineMessageHelper INSTANCE = new OfflineMessageHelper();
   }
}
