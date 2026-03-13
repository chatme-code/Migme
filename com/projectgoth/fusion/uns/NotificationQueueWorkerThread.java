package com.projectgoth.fusion.uns;

import com.google.gson.Gson;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class NotificationQueueWorkerThread implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(NotificationQueueWorkerThread.class));
   private static final AtomicInteger redisErrorCounter = new AtomicInteger();
   private Message message;
   private RegistryPrx registryPrx;
   private boolean dryRun;
   private UserNotificationServiceI parentUserNotificationService;
   private Gson gson;

   public NotificationQueueWorkerThread(Message msg, RegistryPrx registryProxy, boolean dryRun, Gson gson, UserNotificationServiceI parentUserNotificationService) {
      this.message = msg;
      this.registryPrx = registryProxy;
      this.dryRun = dryRun;
      this.parentUserNotificationService = parentUserNotificationService;
      this.gson = gson;
   }

   public void run() {
      int totalPending = 0;
      if (!this.dryRun) {
         boolean isForMigCore = Enums.NotificationTypeEnum.isForMigCore(this.message.notificationType);
         boolean isForMigBo = Enums.NotificationTypeEnum.isForMigBo(this.message.notificationType);
         boolean isForCollapse = Enums.NotificationTypeEnum.isForCollapse(this.message.notificationType);
         boolean needToIncreaseUnreadCount = true;
         Jedis handle = null;
         boolean redisErrorEncountered = false;

         String previousInviter;
         try {
            handle = Redis.getMasterInstanceForUserID(this.message.toUserId);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.USER_NOTIFICATION_TRIM_ON_WRITE_ENABLED)) {
               UserNotificationPurger.trimUserNotificationItemsIfNeededOnWrite(handle, handle, this.message.toUserId);
            }

            String messageToStore;
            if (isForCollapse) {
               messageToStore = handle.hget(UserNotificationServiceI.getUnsKey(this.message.toUserId, this.message.notificationType), this.message.key);
               if (messageToStore != null && SystemProperty.getBool("Notifications.storeJson", false)) {
                  needToIncreaseUnreadCount = false;
                  Message previousMsg = (Message)this.gson.fromJson(messageToStore, Message.class);
                  if (previousMsg.parameters.containsKey("inviterUserId") && this.message.parameters.containsKey("inviterUserId")) {
                     previousInviter = (String)previousMsg.parameters.get("inviterUserId");
                     String currentInviter = (String)this.message.parameters.get("inviterUserId");
                     HashSet invitersSet;
                     String[] inviters;
                     if (previousMsg.parameters.containsKey("collapseInviterUserIdKey") && !StringUtil.isBlank((String)previousMsg.parameters.get("collapseInviterUserIdKey"))) {
                        String invitersRaw = (String)previousMsg.parameters.get("collapseInviterUserIdKey");
                        inviters = invitersRaw.split(":");
                        invitersSet = new HashSet(Arrays.asList(inviters));
                     } else {
                        invitersSet = new HashSet();
                     }

                     invitersSet.add(previousInviter);
                     invitersSet.add(currentInviter);
                     inviters = (String[])invitersSet.toArray(new String[0]);
                     this.message.parameters.put("collapseInviterUserIdKey", StringUtil.join((Object[])inviters, ":"));
                  }
               }
            }

            messageToStore = SystemProperty.getBool("Notifications.storeJson", false) ? this.gson.toJson(this.message) : Integer.toString(this.message.toUserId);
            Long resultLong = handle.hset(UserNotificationServiceI.getUnsKey(this.message.toUserId, this.message.notificationType), this.message.key, messageToStore);
            Integer ret = resultLong == null ? null : resultLong.intValue();
            if (log.isDebugEnabled()) {
               log.debug("Store JSON: " + SystemProperty.getBool("Notifications.storeJson", false));
               log.debug("messateToStore: " + messageToStore);
               log.debug("uns: type " + this.message.notificationType + " for: " + this.message.toUsername + " rows created: " + ret);
               log.debug("uns key: " + UserNotificationServiceI.getUnsKey(this.message.toUserId, this.message.notificationType));
            }

            if (needToIncreaseUnreadCount && isForMigBo) {
               Long resultLong2 = handle.incr(UserNotificationServiceI.getUnreadCountUnsKey(this.message.toUserId, this.message.notificationType));
               Integer unreadCount = resultLong2 == null ? null : resultLong2.intValue();
               if (log.isDebugEnabled()) {
                  log.debug(String.format("incremented unread count of migalert type %s of user %s (%d) to %d", Enums.NotificationTypeEnum.fromType(this.message.notificationType).name(), this.message.toUsername, this.message.toUserId, unreadCount));
               }
            }

            if (isForMigCore) {
               Iterator i$ = Enums.NotificationTypeEnum.MIGCORE_SET.iterator();

               while(i$.hasNext()) {
                  Enums.NotificationTypeEnum notfnType = (Enums.NotificationTypeEnum)i$.next();

                  try {
                     totalPending = (int)((long)totalPending + handle.hlen(Redis.KeySpace.USER_NOTIFICATION.append(this.message.toUserId) + ":" + notfnType.getType()));
                  } catch (JedisException var26) {
                  }
               }
            }

            redisErrorCounter.set(0);
         } catch (Exception var27) {
            log.error("Failed to insert notification type [" + this.message.notificationType + "] for user " + this.message.toUsername, var27);
            redisErrorEncountered = true;
         } finally {
            Redis.disconnect(handle, log);
         }

         if (redisErrorEncountered) {
            long maxSleepTimeInSeconds = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_NOTIFICATION_THREAD_BACKOFF_IN_SECONDS);
            long sleepTimeInMs = Math.min(10L * (long)Math.pow(2.0D, (double)redisErrorCounter.incrementAndGet()), 1000L * maxSleepTimeInSeconds);
            log.info("Redis error encountered, sleeping for " + sleepTimeInMs + " ms before next run.");

            try {
               Thread.sleep(sleepTimeInMs);
            } catch (InterruptedException var25) {
               log.warn("Thread sleep interrupted :" + var25.getMessage());
            }
         } else if (isForMigCore) {
            try {
               UserPrx user = this.registryPrx.findUserObject(this.message.toUsername);
               if (user != null) {
                  String url = "";
                  previousInviter = "migAlerts (%1)";
                  if (this.message.parameters != null) {
                     if (this.message.parameters.containsKey("message")) {
                        previousInviter = (String)this.message.parameters.get("message");
                     }

                     if (this.message.parameters.containsKey("url")) {
                        url = (String)this.message.parameters.get("url");
                     } else {
                        url = SystemProperty.get("NotificationsURL", "");
                     }
                  } else {
                     this.message.parameters = new HashMap();
                     url = SystemProperty.get("NotificationsURL", "");
                  }

                  previousInviter = previousInviter.replaceAll("%1", totalPending + "");
                  this.message.parameters.put("message", previousInviter);
                  this.message.parameters.put("totalPending", Integer.toString(totalPending));
                  this.message.parameters.put("url", url);
                  user.pushNotification(this.message);
                  if (log.isDebugEnabled()) {
                     log.debug("Notification " + this.message.notificationType + " pushed for user:" + this.message.toUsername);
                  }
               }
            } catch (ObjectNotFoundException var23) {
               log.warn("Failed to send notification type [" + this.message.notificationType + "] for user " + this.message.toUsername + "... user isn't online?!? ");
            } catch (FusionException var24) {
               log.error("Failed to send notification type [" + this.message.notificationType + "] for user " + this.message.toUsername, var24);
            }
         }
      } else {
         log.debug("Pushing notification " + this.gson.toJson(this.message) + " to user [" + this.message.toUsername + "]");
      }

      this.parentUserNotificationService.doneNotifyFusionUser();
   }
}
