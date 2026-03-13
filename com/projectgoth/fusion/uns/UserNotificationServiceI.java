package com.projectgoth.fusion.uns;

import Ice.Current;
import com.google.gson.Gson;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EmailUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.TemplateStringProcessor;
import com.projectgoth.fusion.dao.GroupMembershipDAO;
import com.projectgoth.fusion.dao.SystemDAO;
import com.projectgoth.fusion.dao.UserDAO;
import com.projectgoth.fusion.data.EmailTemplateData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SMSUserNotification;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice._UserNotificationServiceDisp;
import com.projectgoth.fusion.smsengine.SMSControl;
import com.projectgoth.fusion.uns.domain.AlertNote;
import com.projectgoth.fusion.uns.domain.EmailNote;
import com.projectgoth.fusion.uns.domain.SMSNote;
import com.projectgoth.fusion.uns.task.AlertGroupTask;
import com.projectgoth.fusion.uns.task.EmailGroupAnnouncementTask;
import com.projectgoth.fusion.uns.task.EmailGroupPostSubscribersTask;
import com.projectgoth.fusion.uns.task.SMSGroupAnnouncementTask;
import com.projectgoth.fusion.uns.task.SMSGroupEventTask;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.MailSender;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class UserNotificationServiceI extends _UserNotificationServiceDisp implements InitializingBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserNotificationServiceI.class));
   private static final Logger emailNoteEnqueueLog = Logger.getLogger("EmailNoteEnqueueLog");
   private static final String emailNoteEnqueueLogMSG = "|%s|%s|%s|%s|%s|%s";
   private static final int TASK_THREAD_POOL_SIZE = 5;
   private GroupMembershipDAO groupMembershipDAO;
   private SystemDAO systemDAO;
   private UserDAO userDAO;
   private RegistryPrx registryProxy;
   private MailSender mailSender;
   private int emailQueueWorkerThreadCount = 20;
   private int smsQueueWorkerThreadCount = 10;
   private int alertQueueWorkerThreadCount = 10;
   private int notificationQueueWorkerThreadCount = 10;
   private int groupEmailBlockSize = 100;
   private String defaultMigEmailDomain;
   private int alertBlockSize = 100;
   private boolean dryRun = false;
   private String mailServer;
   private String twoWaySMSNumber;
   private AtomicLong emailsSent = new AtomicLong(0L);
   private AtomicLong smsSent = new AtomicLong(0L);
   private AtomicLong alertsSent = new AtomicLong(0L);
   private AtomicLong notificationsSent = new AtomicLong(0L);
   private EmailQueueWorkerThread[] emailQueueWorkerThreads;
   private SMSQueueWorkerThread[] smsQueueWorkerThreads;
   private AlertQueueWorkerThread[] alertQueueWorkerThreads;
   private AtomicInteger notificationServiceQueueSize = new AtomicInteger(0);
   private ExecutorService notificationService;
   private ExecutorService taskService = (ThreadPoolExecutor)Executors.newFixedThreadPool(5);
   private BlockingQueue<EmailNote> emailQueue = new LinkedBlockingQueue();
   private BlockingQueue<SMSNote> smsQueue = new LinkedBlockingQueue();
   private BlockingQueue<AlertNote> alertQueue = new LinkedBlockingQueue();
   public static final String ALERT_KEY = "alert_key";
   public static final String TEXT_HTML_MIME_TYPE = "text/html";
   private Gson gson = new Gson();

   public void afterPropertiesSet() throws Exception {
      log.info("Dry run? [" + this.dryRun + "]");
      this.mailServer = this.systemDAO.getSystemProperty("MailServer");
      if (!StringUtils.hasLength(this.mailServer)) {
         log.fatal("unable to load MailServer from system table");
         throw new Exception("unable to load MailServer from system table");
      } else {
         this.twoWaySMSNumber = this.systemDAO.getSystemProperty("TwoWaySMSNumber");
         if (!StringUtils.hasLength(this.twoWaySMSNumber)) {
            log.fatal("unable to load TwoWaySMSNumber from system table");
            throw new Exception("unable to load TwoWaySMSNumber from system table");
         }
      }
   }

   public void initializeQueues() throws Exception {
      log.info("creating worker threads...");
      this.emailQueueWorkerThreads = new EmailQueueWorkerThread[this.emailQueueWorkerThreadCount];

      int i;
      for(i = 0; i < this.emailQueueWorkerThreadCount; ++i) {
         this.emailQueueWorkerThreads[i] = new EmailQueueWorkerThread(this.emailQueue, this.mailSender, this.mailServer, this.dryRun, this.emailsSent);
         this.emailQueueWorkerThreads[i].start();
      }

      log.info("created " + this.emailQueueWorkerThreadCount + " email queue worker threads");
      this.smsQueueWorkerThreads = new SMSQueueWorkerThread[this.smsQueueWorkerThreadCount];

      for(i = 0; i < this.smsQueueWorkerThreadCount; ++i) {
         this.smsQueueWorkerThreads[i] = new SMSQueueWorkerThread(this.smsQueue, this.dryRun, this.twoWaySMSNumber, this.smsSent);
         this.smsQueueWorkerThreads[i].start();
      }

      log.info("created " + this.smsQueueWorkerThreadCount + " sms queue worker threads");
      this.alertQueueWorkerThreads = new AlertQueueWorkerThread[this.alertQueueWorkerThreadCount];

      for(i = 0; i < this.alertQueueWorkerThreadCount; ++i) {
         this.alertQueueWorkerThreads[i] = new AlertQueueWorkerThread(this.alertQueue, this.registryProxy, this.dryRun, this.alertsSent);
         this.alertQueueWorkerThreads[i].start();
      }

      log.info("created " + this.alertQueueWorkerThreadCount + " alert queue worker threads");
      this.notificationService = (ThreadPoolExecutor)Executors.newFixedThreadPool(this.notificationQueueWorkerThreadCount);
      log.info("created " + this.notificationQueueWorkerThreadCount + " notification queue worker threads");
   }

   void shutdown() {
   }

   public void shutdownExecutorThreads() {
      this.notificationService.shutdown();
   }

   @Required
   public void setRegistryProxy(RegistryPrx registryProxy) {
      this.registryProxy = registryProxy;
   }

   public void notifyFusionGroupAnnouncementViaEmail(int groupId, EmailUserNotification note, Current __current) throws FusionException {
      this.taskService.execute(new EmailGroupAnnouncementTask(groupId, this.groupMembershipDAO, note, this.emailQueue, this.defaultMigEmailDomain, this.groupEmailBlockSize));
   }

   public void notifyFusionGroupAnnouncementViaSMS(int groupId, SMSUserNotification note, Current __current) throws FusionException {
      if (note.smsSubType == 0) {
         note.smsSubType = SystemSMSData.SubTypeEnum.GROUP_ANNOUNCEMENT_NOTIFICATION.value();
      }

      if (SMSControl.isSendEnabledForSubtype(note.smsSubType)) {
         this.taskService.execute(new SMSGroupAnnouncementTask(groupId, this.groupMembershipDAO, note, this.smsQueue));
      }

   }

   public void notifyFusionGroupEventViaSMS(int groupId, SMSUserNotification note, Current __current) throws FusionException {
      if (note.smsSubType == 0) {
         note.smsSubType = SystemSMSData.SubTypeEnum.GROUP_EVENT_NOTIFICATION.value();
      }

      if (SMSControl.isSendEnabledForSubtype(note.smsSubType)) {
         this.taskService.execute(new SMSGroupEventTask(groupId, this.groupMembershipDAO, note, this.smsQueue));
      }

   }

   public void notifyFusionGroupViaAlert(int groupId, String message, Current __current) throws FusionException {
      this.taskService.execute(new AlertGroupTask(groupId, message, this.alertQueue, this.groupMembershipDAO, this.alertBlockSize));
   }

   public void notifyFusionUserViaEmail(String username, EmailUserNotification note, Current __current) throws FusionException {
      EmailNote emailNote = new EmailNote(note.message, note.subject);
      emailNote.addRecipient(username + this.defaultMigEmailDomain);
      this.addEmailNoteIntoQueue(emailNote);
   }

   public void notifyUsersViaFusionEmail(String sender, String senderPassword, String[] recipients, EmailUserNotification note, Current current) throws FusionException {
      EmailNote emailNote = new EmailNote(sender + this.defaultMigEmailDomain, senderPassword, note.message, note.subject);
      String[] arr$ = recipients;
      int len$ = recipients.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String recipient = arr$[i$];
         emailNote.addRecipient(recipient);
      }

      this.addEmailNoteIntoQueue(emailNote);
   }

   public void notifyFusionGroupPostSubscribersViaEmail(int userPostId, EmailUserNotification note, Current __current) throws FusionException {
      this.taskService.execute(new EmailGroupPostSubscribersTask(userPostId, this.groupMembershipDAO, note, this.emailQueue, this.defaultMigEmailDomain, this.groupEmailBlockSize));
   }

   public void sendEmailFromNoReply(String destinationAddress, String subject, String body, Current __current) throws FusionException {
      this.sendEmailFromNoReplyWithType(destinationAddress, subject, body, (String)null);
   }

   public void sendEmailFromNoReplyWithType(String destinationAddress, String subject, String body, String mimeType, Current __current) throws FusionException {
      this.sendEmailFromNoReplyWithTypeAndParts(destinationAddress, subject, body, mimeType, (Collection)null);
   }

   private void sendEmailFromNoReplyWithTypeAndParts(String destinationAddress, String subject, String body, String mimeType, Collection<EmailNote.EmailPart> extraParts) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("sendEmailFromNoReplyWithTypeAndParts(destinationAddress=[" + destinationAddress + "],subject=[" + subject + "],body=[" + body + "],mimeType=[" + mimeType + "],parts=[" + extraParts + "]");
      }

      EmailNote emailNote = new EmailNote(body, subject, mimeType);
      emailNote.addRecipient(destinationAddress);
      if (extraParts != null) {
         Iterator i$ = extraParts.iterator();

         while(i$.hasNext()) {
            EmailNote.EmailPart extraPart = (EmailNote.EmailPart)i$.next();
            emailNote.addExtraPart(extraPart);
         }
      }

      this.addEmailNoteIntoQueue(emailNote);
   }

   public void notifyFusionUserViaSMS(String username, SMSUserNotification note, Current __current) throws FusionException {
      if (SMSControl.isSendEnabledForSubtype(note.smsSubType, username)) {
         String phoneNumber;
         if (StringUtils.hasLength(note.phoneNumber)) {
            phoneNumber = note.phoneNumber;
         } else {
            phoneNumber = this.userDAO.getMobileNumberForUser(username);
         }

         if (!StringUtils.hasLength(phoneNumber)) {
            log.error("failed to find a mobile phone for user [" + username + "]");
            throw new FusionException("failed to find a mobile phone for user [" + username + "]");
         } else if (note.smsSubType < 1) {
            log.error("failed to find a sms subtype for user [" + username + "]");
            throw new FusionException("failed to find a sms subtype for user [" + username + "]");
         } else {
            SMSNote smsNote = new SMSNote(note.message, phoneNumber, username, note.smsSubType);
            this.smsQueue.add(smsNote);
         }
      }
   }

   public void notifyUserViaEmail(EmailUserNotification note, Current __current) throws FusionException {
      EmailNote emailNote = new EmailNote(note.message, note.subject);
      emailNote.addRecipient(note.emailAddress);
      this.addEmailNoteIntoQueue(emailNote);
   }

   public void notifyFusionUserViaAlert(String username, String message, Current __current) throws FusionException {
      AlertNote note = new AlertNote(message);
      note.addUser(username);
      this.alertQueue.add(note);
   }

   @Required
   public void setGroupMembershipDAO(GroupMembershipDAO groupMembershipDAO) {
      this.groupMembershipDAO = groupMembershipDAO;
   }

   @Required
   public void setSystemDAO(SystemDAO systemDAO) {
      this.systemDAO = systemDAO;
   }

   @Required
   public void setUserDAO(UserDAO userDAO) {
      this.userDAO = userDAO;
   }

   @Required
   public void setMailSender(MailSender mailSender) {
      this.mailSender = mailSender;
   }

   public void setEmailQueueWorkerThreadCount(int emailQueueWorkerThreadCount) {
      this.emailQueueWorkerThreadCount = emailQueueWorkerThreadCount;
   }

   public void setSmsQueueWorkerThreadCount(int smsQueueWorkerThreadCount) {
      this.smsQueueWorkerThreadCount = smsQueueWorkerThreadCount;
   }

   @Required
   public void setDefaultMigEmailDomain(String defaultMigEmailDomain) {
      this.defaultMigEmailDomain = defaultMigEmailDomain;
   }

   public void setGroupEmailBlockSize(int groupEmailBlockSize) {
      this.groupEmailBlockSize = groupEmailBlockSize;
   }

   public void setDryRun(boolean dryRun) {
      this.dryRun = dryRun;
   }

   public void setNotificationQueueWorkerThreadCount(int notificationQueueWorkerThreadCount) {
      this.notificationQueueWorkerThreadCount = notificationQueueWorkerThreadCount;
   }

   public void setAlertQueueWorkerThreadCount(int alertQueueWorkerThreadCount) {
      this.alertQueueWorkerThreadCount = alertQueueWorkerThreadCount;
   }

   public long getEmailsSent() {
      return this.emailsSent.longValue();
   }

   public long getSmsSent() {
      return this.smsSent.longValue();
   }

   public long getAlertsSent() {
      return this.alertsSent.longValue();
   }

   public long getNotificationsSent() {
      return this.notificationsSent.longValue();
   }

   public int getEmailQueueSize() {
      return this.emailQueue.size();
   }

   public int getSmsQueueSize() {
      return this.smsQueue.size();
   }

   public int getAlertQueueSize() {
      return this.alertQueue.size();
   }

   public int getNotificationQueueSize() {
      return this.notificationServiceQueueSize.get();
   }

   public void notifyFusionUser(Message msg, Current __current) throws FusionException {
      if (msg != null) {
         if (StringUtil.isBlank(msg.key)) {
            throw new FusionException("key is required");
         }

         if (msg.toUserId < 0) {
            throw new FusionException("Invalid userId provided");
         }

         if (StringUtil.isBlank(msg.toUsername)) {
            throw new FusionException("toUsername is required");
         }

         if (!Enums.NotificationTypeEnum.isValid(msg.notificationType)) {
            throw new FusionException("Invalid notification type " + msg.notificationType);
         }

         long notificationServiceMaxQueueSize = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_NOTIFICATION_SERVICE_QUEUE_SIZE);
         if ((long)this.notificationServiceQueueSize.incrementAndGet() <= notificationServiceMaxQueueSize) {
            this.notificationService.execute(new NotificationQueueWorkerThread(msg, this.registryProxy, this.dryRun, this.gson, this));
         } else {
            log.warn("NotificationServiceMaxQueueSize[" + notificationServiceMaxQueueSize + "] exceeded. Dropping message type [" + msg.notificationType + "] for user [" + msg.toUsername + "]");
            this.notificationServiceQueueSize.decrementAndGet();
         }
      }

   }

   public void doneNotifyFusionUser() {
      this.notificationsSent.incrementAndGet();
      this.notificationServiceQueueSize.decrementAndGet();
   }

   private static String getReadUnsKey(int userId, int notfnType) {
      return getUnsKey(userId, notfnType) + ":read";
   }

   public static String getUnsKey(int userId, int notfnType) {
      return Redis.KeySpace.USER_NOTIFICATION.append(userId) + ":" + notfnType;
   }

   public static String getUnreadCountUnsKey(int userId, int notfnType) {
      return getUnsKey(userId, notfnType) + ":unreadc";
   }

   public void sendNotificationCounterToUser(int userId, Current __current) {
      String url = SystemProperty.get("NotificationsURL", "");
      String msgString = SystemProperty.get("NotificationsMessage", "migAlerts (%1)");
      Message message = new Message();

      try {
         Map<Integer, Integer> mapNtfn = this.getPendingNotificationsForUser(userId);
         int notificationCount = 0;

         int type;
         for(Iterator i$ = mapNtfn.keySet().iterator(); i$.hasNext(); notificationCount += (Integer)mapNtfn.get(type)) {
            type = (Integer)i$.next();
         }

         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         String username = userBean.getUsernameByUserid(userId, (Connection)null);
         if (log.isDebugEnabled()) {
            log.debug("Sending notification count to user:" + username);
         }

         UserPrx user = this.registryProxy.findUserObject(username);
         if (user != null) {
            message.toUsername = username;
            message.parameters = new HashMap();
            msgString = msgString.replaceAll("%1", notificationCount + "");
            message.parameters.put("message", msgString);
            message.parameters.put("totalPending", Integer.toString(notificationCount));
            message.parameters.put("url", url);
            message.notificationType = Enums.NotificationTypeEnum.TOTAL_COUNT.getType();
            user.pushNotification(message);
            if (log.isDebugEnabled()) {
               log.debug("Notification " + message.notificationType + " pushed for user:" + message.toUsername);
            }
         }
      } catch (ObjectNotFoundException var11) {
         log.info("Unable to push notification for userid:" + userId + " user not online");
      } catch (Exception var12) {
         log.error("Unable to push notification for user:" + message.toUsername, var12);
      }

   }

   public void clearNotificationsForUser(int userId, int notfnType, String[] keys, Current __current) throws FusionException {
      log.debug("Clearing multiple type[" + notfnType + "] notifications for user:" + userId);
      Jedis handle = null;

      try {
         handle = Redis.getMasterInstanceForUserID(userId);
         Map<String, String> backup = new TreeMap();
         String[] arr$ = keys;
         int len$ = keys.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String field = arr$[i$];
            String value = handle.hget(getUnsKey(userId, notfnType), field);
            if (value != null) {
               backup.put(field, value);
               handle.hdel(getUnsKey(userId, notfnType), new String[]{field});
            }
         }

         if (SystemProperty.getBool("Notifications.storeRead", false) && backup != null && backup.size() > 0) {
            handle.hmset(getReadUnsKey(userId, notfnType), backup);
         }

         this.sendNotificationCounterToUser(userId);
      } catch (Exception var16) {
         log.error("Failed to clear pending notifications for user[" + userId + "], for type: " + notfnType, var16);
      } finally {
         Redis.disconnect(handle, log);
      }

   }

   public void clearAllNotificationsByTypeForUser(int userId, int notfnType, Current __current) throws FusionException {
      log.debug("Clearing all type[" + notfnType + "] notifications for user:" + userId);
      Jedis handle = null;

      try {
         handle = Redis.getMasterInstanceForUserID(userId);
         Map<String, String> backup = handle.hgetAll(getUnsKey(userId, notfnType));
         handle.del(new String[]{getUnsKey(userId, notfnType)});
         if (SystemProperty.getBool("Notifications.storeRead", false) && backup != null && backup.size() > 0) {
            handle.hmset(getReadUnsKey(userId, notfnType), backup);
         }

         this.sendNotificationCounterToUser(userId);
      } catch (Exception var10) {
         log.error("Failed to clear pending notifications for user[" + userId + "], for type: " + notfnType, var10);
      } finally {
         Redis.disconnect(handle, log);
      }

   }

   public Map<Integer, Integer> getPendingNotificationsForUser(int userId, Current __current) throws FusionException {
      Jedis handle = null;
      TreeMap notfnMap = new TreeMap();

      try {
         Jedis master = null;
         handle = Redis.getSlaveInstanceForUserID(userId);
         if (handle == null) {
            master = handle = Redis.getMasterInstanceForUserID(userId);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.USER_NOTIFICATION_TRIM_ON_READ_ENABLED)) {
            UserNotificationPurger.trimUserNotificationItemsIfNeeded(handle, master, userId);
         }

         Enums.NotificationTypeEnum[] arr$ = Enums.NotificationTypeEnum.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.NotificationTypeEnum notfnType = arr$[i$];

            try {
               Long resultLong = handle.hlen(getUnsKey(userId, notfnType.getType()));
               Integer result = resultLong == null ? null : resultLong.intValue();
               notfnMap.put(notfnType.getType(), result);
            } catch (JedisException var17) {
               log.warn("Failed to retrieve pending notifications for user[" + userId + "], for type: " + notfnType.getType(), var17);
            }
         }
      } catch (Exception var18) {
         log.error("Failed to retrieve pending notifications for user[" + userId + "]", var18);
      } finally {
         Redis.disconnect(handle, log);
      }

      return notfnMap;
   }

   public void clearAllNotificationsForUser(int userId, Current __current) throws FusionException {
      log.debug("Clearing all notifications for user:" + userId);
      Jedis handle = null;

      try {
         handle = Redis.getMasterInstanceForUserID(userId);
         List<String> keys = new LinkedList();
         Enums.NotificationTypeEnum[] arr$ = Enums.NotificationTypeEnum.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.NotificationTypeEnum notfnType = arr$[i$];
            String key = getUnsKey(userId, notfnType.getType());
            Map<String, String> backup = handle.hgetAll(key);
            if (SystemProperty.getBool("Notifications.storeRead", false) && backup != null && backup.size() > 0) {
               handle.hmset(getReadUnsKey(userId, notfnType.getType()), backup);
            }

            keys.add(key);
         }

         handle.del((String[])keys.toArray(new String[keys.size()]));
      } catch (JedisException var16) {
         log.error("Failed to clear pending notifications for user[" + userId + "]", var16);
      } catch (Exception var17) {
         log.error("Failed to clear pending notifications for user[" + userId + "]", var17);
      } finally {
         Redis.disconnect(handle, log);
      }

   }

   public void clearAllUnreadNotificationCountForUser(int userId, boolean resetAll, Current __current) throws FusionException {
      log.debug("Clearing all notification unread counts for user:" + userId);
      Jedis handle = null;

      try {
         handle = Redis.getMasterInstanceForUserID(userId);
         List<String> keys = new LinkedList();
         EnumSet<Enums.NotificationTypeEnum> resetSet = resetAll ? Enums.NotificationTypeEnum.MIGBO_SET : Enums.NotificationTypeEnum.MIGBO_NON_PERSISTENT_SET;

         String key;
         for(Iterator i$ = resetSet.iterator(); i$.hasNext(); keys.add(key)) {
            Enums.NotificationTypeEnum notfnType = (Enums.NotificationTypeEnum)i$.next();
            key = getUnreadCountUnsKey(userId, notfnType.getType());
            if (log.isDebugEnabled()) {
               log.debug(String.format(" clearing notification unread count %s", key));
            }
         }

         handle.del((String[])keys.toArray(new String[keys.size()]));
      } catch (JedisException var15) {
         log.error("Failed to clear notification unread counts for user[" + userId + "]", var15);
      } catch (Exception var16) {
         log.error("Failed to clear notification unread counts for user[" + userId + "]", var16);
      } finally {
         Redis.disconnect(handle, log);
      }

   }

   public Map<Integer, Integer> getUnreadNotificationCountForUser(int userId, Current __current) throws FusionException {
      log.debug("Getting all notification unread counts for user:" + userId);
      Jedis handle = null;
      TreeMap notfnMap = new TreeMap();

      try {
         handle = Redis.getSlaveInstanceForUserID(userId);
         if (handle == null) {
            handle = Redis.getMasterInstanceForUserID(userId);
         }

         Iterator i$ = Enums.NotificationTypeEnum.MIGBO_SET.iterator();

         while(i$.hasNext()) {
            Enums.NotificationTypeEnum notfnType = (Enums.NotificationTypeEnum)i$.next();

            try {
               String v = handle.get(getUnreadCountUnsKey(userId, notfnType.getType()));
               if (v != null) {
                  int vInt = StringUtil.toIntOrDefault(v, -1);
                  if (vInt == -1) {
                     log.warn(String.format("Failed to retrieve notification unread counts for user[%d], for type: %d - value of unread count '%s' is not an integer", userId, notfnType.getType(), v));
                  } else {
                     notfnMap.put(notfnType.getType(), vInt);
                  }
               }
            } catch (JedisException var14) {
               log.warn("Failed to retrieve notification unread counts for user[" + userId + "], for type: " + notfnType.getType(), var14);
            }
         }
      } catch (Exception var15) {
         log.error("Failed to retrieve notification unread counts for user[" + userId + "]", var15);
      } finally {
         Redis.disconnect(handle, log);
      }

      return notfnMap;
   }

   public Map<Integer, Map<String, Map<String, String>>> getPendingNotificationDataForUser(int userId, Current __current) throws FusionException {
      log.debug("Getting all notification alerts for user:" + userId);
      Jedis handle = null;
      TreeMap notfnMap = new TreeMap();

      try {
         handle = Redis.getMasterInstanceForUserID(userId);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.USER_NOTIFICATION_TRIM_ON_READ_ENABLED)) {
            UserNotificationPurger.trimUserNotificationItemsIfNeeded(handle, handle, userId);
         }

         Iterator i$ = Enums.NotificationTypeEnum.MIGBO_SET.iterator();

         while(true) {
            Enums.NotificationTypeEnum notfnType;
            Map data;
            HashMap finaldata;
            boolean vInt;
            String v;
            do {
               do {
                  while(true) {
                     while(true) {
                        if (!i$.hasNext()) {
                           return notfnMap;
                        }

                        notfnType = (Enums.NotificationTypeEnum)i$.next();
                        data = null;
                        finaldata = new HashMap();
                        vInt = false;
                        v = null;

                        try {
                           if (!Enums.NotificationTypeEnum.isForPersistent(notfnType.getType())) {
                              v = handle.getSet(getUnreadCountUnsKey(userId, notfnType.getType()), "0");
                           }
                           break;
                        } catch (JedisException var26) {
                           log.warn("Failed to retrieve notification unread counts for user[" + userId + "], for type: " + notfnType.getType(), var26);
                        }
                     }

                     try {
                        data = handle.hgetAll(getUnsKey(userId, notfnType.getType()));
                        break;
                     } catch (JedisException var27) {
                        log.warn("Failed to retrieve notification alerts for user[" + userId + "], for type: " + notfnType.getType(), var27);
                     }
                  }
               } while(data == null);
            } while(data.isEmpty());

            if (v != null) {
               int vInt = StringUtil.toIntOrDefault(v, -1);
               if (vInt == -1) {
                  vInt = false;
                  log.warn(String.format("Failed to convert notification unread counts for user[%d], for type: %d - value of unread count '%s' is not an integer", userId, notfnType.getType(), v));
               }
            }

            Iterator i$ = data.keySet().iterator();

            while(i$.hasNext()) {
               String key = (String)i$.next();
               String jsonMessage = (String)data.get(key);
               int x = StringUtil.toIntOrDefault(jsonMessage, -1);
               if (x != -1) {
                  finaldata.put(key, StringUtil.EMPTY_STRING_MAP);
               } else {
                  log.debug(String.format("key [%s] json[%s]", key, jsonMessage));

                  try {
                     Message message = (Message)this.gson.fromJson(jsonMessage, Message.class);
                     if (Enums.NotificationTypeEnum.isForPersistent(message.notificationType)) {
                        message.parameters.put("timestamp", Long.toString(message.dateCreated));
                        finaldata.put(key, message.parameters);
                     } else {
                        int pendingNotificationsExpiryInSeconds = SystemProperty.getInt("pendingNotificationsExpiryInSeconds", 7776000);
                        Date expiryDate = new Date(message.dateCreated + (long)pendingNotificationsExpiryInSeconds * 1000L);
                        if (expiryDate.before(new Date())) {
                           log.debug(String.format("Deleting old pending notification [%s] since it has expired datecreated[%s] expiryInSeconds[%d] expirydate[%s]", key, new Date(message.dateCreated), pendingNotificationsExpiryInSeconds, expiryDate));
                           handle.hdel(getUnsKey(userId, notfnType.getType()), new String[]{key});
                        } else {
                           message.parameters.put("timestamp", Long.toString(message.dateCreated));
                           finaldata.put(key, message.parameters);
                        }
                     }
                  } catch (Exception var25) {
                     log.error(String.format("Error while parsing json message [%s]", jsonMessage), var25);
                  }
               }
            }

            notfnMap.put(notfnType.getType(), finaldata);
         }
      } catch (Exception var28) {
         log.error("Failed to retrieve notification alerts for user[" + userId + "]", var28);
      } finally {
         Redis.disconnect(handle, log);
      }

      return notfnMap;
   }

   public Map<Integer, Map<String, Map<String, String>>> getUnreadPendingNotificationDataForUser(int userId, Current __current) throws FusionException {
      log.debug("Getting all notification alerts for user:" + userId);
      Jedis handle = null;
      TreeMap notfnMap = new TreeMap();

      try {
         handle = Redis.getMasterInstanceForUserID(userId);
         Iterator i$ = Enums.NotificationTypeEnum.MIGBO_SET.iterator();

         while(true) {
            Enums.NotificationTypeEnum notfnType;
            Map data;
            HashMap finaldata;
            int vInt;
            String v;
            do {
               do {
                  while(true) {
                     while(true) {
                        if (!i$.hasNext()) {
                           return notfnMap;
                        }

                        notfnType = (Enums.NotificationTypeEnum)i$.next();
                        data = null;
                        finaldata = new HashMap();
                        vInt = 0;
                        v = null;

                        try {
                           v = handle.get(getUnreadCountUnsKey(userId, notfnType.getType()));
                           break;
                        } catch (JedisException var26) {
                           log.warn("Failed to retrieve notification unread counts for user[" + userId + "], for type: " + notfnType.getType(), var26);
                        }
                     }

                     try {
                        data = handle.hgetAll(getUnsKey(userId, notfnType.getType()));
                        break;
                     } catch (JedisException var27) {
                        log.warn("Failed to retrieve notification alerts for user[" + userId + "], for type: " + notfnType.getType(), var27);
                     }
                  }
               } while(data == null);
            } while(data.isEmpty());

            if (v != null) {
               vInt = StringUtil.toIntOrDefault(v, -1);
               if (vInt == -1) {
                  vInt = 0;
                  log.warn(String.format("Failed to convert notification unread counts for user[%d], for type: %d - value of unread count '%s' is not an integer", userId, notfnType.getType(), v));
               }
            }

            Map<Long, String> timestampToKey = new TreeMap(Collections.reverseOrder());
            Iterator i$ = data.keySet().iterator();

            while(true) {
               while(i$.hasNext()) {
                  String key = (String)i$.next();
                  String jsonMessage = (String)data.get(key);
                  int x = StringUtil.toIntOrDefault(jsonMessage, -1);
                  if (x != -1) {
                     finaldata.put(key, StringUtil.EMPTY_STRING_MAP);
                  } else {
                     log.debug(String.format("key [%s] json[%s]", key, jsonMessage));

                     try {
                        Message message = (Message)this.gson.fromJson(jsonMessage, Message.class);
                        int pendingNotificationsExpiryInSeconds = SystemProperty.getInt("pendingNotificationsExpiryInSeconds", 7776000);
                        Date expiryDate = new Date(message.dateCreated + (long)pendingNotificationsExpiryInSeconds * 1000L);
                        if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Alert.PT72238252_MIGBO_PERSISTENT_ALERTS_EXCLUDE_EXPIRE_ENABLED)) {
                           if (expiryDate.before(new Date()) && !Enums.NotificationTypeEnum.isForPersistent(message.notificationType)) {
                              log.debug(String.format("Deleting old pending notification [%s] since it has expired datecreated[%s] expiryInSeconds[%d] expirydate[%s]", key, new Date(message.dateCreated), pendingNotificationsExpiryInSeconds, expiryDate));
                              handle.hdel(getUnsKey(userId, notfnType.getType()), new String[]{key});
                           } else {
                              message.parameters.put("timestamp", Long.toString(message.dateCreated));
                              finaldata.put(key, message.parameters);
                              timestampToKey.put(message.dateCreated, key);
                           }
                        } else if (expiryDate.before(new Date())) {
                           log.debug(String.format("Deleting old pending notification [%s] since it has expired datecreated[%s] expiryInSeconds[%d] expirydate[%s]", key, new Date(message.dateCreated), pendingNotificationsExpiryInSeconds, expiryDate));
                           handle.hdel(getUnsKey(userId, notfnType.getType()), new String[]{key});
                        } else {
                           message.parameters.put("timestamp", Long.toString(message.dateCreated));
                           finaldata.put(key, message.parameters);
                           timestampToKey.put(message.dateCreated, key);
                        }
                     } catch (Exception var28) {
                        log.error(String.format("Error while parsing json message [%s]", jsonMessage), var28);
                     }
                  }
               }

               Map<String, Map<String, String>> latestUnreadAlerts = new HashMap(vInt);
               int count = 0;

               for(Iterator i$ = timestampToKey.entrySet().iterator(); i$.hasNext(); ++count) {
                  Entry<Long, String> entry = (Entry)i$.next();
                  if (count >= vInt) {
                     break;
                  }

                  String key = (String)entry.getValue();
                  Map<String, String> value = (Map)finaldata.get(key);
                  latestUnreadAlerts.put(key, value);
               }

               notfnMap.put(notfnType.getType(), latestUnreadAlerts);
               break;
            }
         }
      } catch (Exception var29) {
         log.error("Failed to retrieve notification alerts for user[" + userId + "]", var29);
      } finally {
         Redis.disconnect(handle, log);
      }

      return notfnMap;
   }

   public Map<String, Map<String, String>> getPendingNotificationDataForUserByType(int userId, int notificationType, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Getting all notification alerts of type " + notificationType + " for user:" + userId);
      }

      Jedis handle = null;
      Map<String, Map<String, String>> notfnMap = new HashMap();
      Enums.NotificationTypeEnum notfnType = Enums.NotificationTypeEnum.fromType(notificationType);
      if (notfnType == null) {
         log.error("Invalid notificationType provided :" + notificationType);
         return notfnMap;
      } else {
         try {
            handle = Redis.getSlaveInstanceForUserID(userId);
            Jedis master = null;
            if (handle == null) {
               master = handle = Redis.getMasterInstanceForUserID(userId);
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.USER_NOTIFICATION_TRIM_ON_READ_ENABLED)) {
               UserNotificationPurger.trimUserNotificationItemsIfNeeded(handle, master, userId);
            }

            Map data = null;

            try {
               data = handle.hgetAll(getUnsKey(userId, notfnType.getType()));
            } catch (JedisException var23) {
               log.warn("Failed to retrieve notification alerts for user[" + userId + "], for type: " + notfnType.getType(), var23);
               HashMap var10 = notfnMap;
               return var10;
            }

            if (data == null || data.isEmpty()) {
               HashMap var26 = notfnMap;
               return var26;
            }

            Iterator i$ = data.keySet().iterator();

            while(i$.hasNext()) {
               String key = (String)i$.next();
               String jsonMessage = (String)data.get(key);
               boolean oldFormatAlerts = -1 != StringUtil.toIntOrDefault(jsonMessage, -1);
               if (oldFormatAlerts) {
                  notfnMap.put(key, StringUtil.EMPTY_STRING_MAP);
               } else {
                  if (log.isDebugEnabled()) {
                     log.debug(String.format("key [%s] json[%s]", key, jsonMessage));
                  }

                  try {
                     Message message = (Message)this.gson.fromJson(jsonMessage, Message.class);
                     int pendingNotificationsExpiryInSeconds = SystemProperty.getInt("pendingNotificationsExpiryInSeconds", 7776000);
                     Date expiryDate = new Date(message.dateCreated + (long)pendingNotificationsExpiryInSeconds * 1000L);
                     if (expiryDate.before(new Date())) {
                        handle.hdel(getUnsKey(userId, notfnType.getType()), new String[]{key});
                     } else {
                        message.parameters.put("timestamp", Long.toString(message.dateCreated));
                        notfnMap.put(key, message.parameters);
                     }
                  } catch (Exception var22) {
                     log.error(String.format("Error while parsing json message [%s]", jsonMessage), var22);
                  }
               }
            }
         } catch (Exception var24) {
            log.error("Failed to retrieve notification alerts for user[" + userId + "]", var24);
         } finally {
            Redis.disconnect(handle, log);
         }

         return notfnMap;
      }
   }

   private static String replaceTemplateVariables(String templateString, Map<String, String> parameters) throws IOException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.NEW_TEMPLATE_PROCESSOR_ENABLED) ? TemplateStringProcessor.process(templateString, parameters) : StringUtil.replaceNamedValues(templateString, parameters);
   }

   public void sendTemplatizedEmailFromNoReply(String destinationEmailAddress, int templateId, Map<String, String> templateVarValues, Current __current) throws FusionException {
      try {
         EmailTemplateData template = MemCacheOrEJB.getEmailTemplateData(templateId);
         if (template == null) {
            throw new FusionException("Unknown templateId:" + templateId);
         } else {
            String body = replaceTemplateVariables(template.bodyTemplate, templateVarValues);
            String subject = replaceTemplateVariables(template.subjectTemplate, templateVarValues);
            if (template.getExtraPartCount() == 0) {
               if (template.mimeType.equals("text/html")) {
                  this.sendEmailFromNoReplyWithType(destinationEmailAddress, subject, body, template.mimeType);
               } else {
                  this.sendEmailFromNoReply(destinationEmailAddress, subject, body);
               }
            } else {
               List<EmailNote.EmailPart> extraParts = new ArrayList();

               for(int i = 0; i < template.getExtraPartCount(); ++i) {
                  EmailTemplateData.PartTemplateData partTemplate = template.getExtraPart(i);
                  String extraPartContent = replaceTemplateVariables(partTemplate.contentTemplate, templateVarValues);
                  String extraPartMIMEType = partTemplate.mimeType;
                  extraParts.add(new EmailNote.EmailPart(extraPartContent, extraPartMIMEType));
               }

               this.sendEmailFromNoReplyWithTypeAndParts(destinationEmailAddress, subject, body, template.mimeType, extraParts);
            }

         }
      } catch (IOException var13) {
         log.error("Error while sending a templatized email to " + destinationEmailAddress, var13);
         throw new FusionException(var13.getMessage());
      } catch (FusionException var14) {
         log.error("Error while sending a templatized email to " + destinationEmailAddress, var14);
         throw var14;
      } catch (Exception var15) {
         log.error("Unhandled exception while sending a templatized email to " + destinationEmailAddress, var15);
         throw new FusionException(var15.getMessage());
      }
   }

   private void addEmailNoteIntoQueue(EmailNote emailNote) {
      Set<String> recipients = emailNote.getRecipients();
      Iterator i = recipients.iterator();

      while(i.hasNext()) {
         String recipient = (String)i.next();
         if (this.userDAO.isBounceEmailAddress(recipient)) {
            log.info(" Removing email address: " + recipient + " from recepients list. please verify if the receipient is not bounce");
            i.remove();
         } else if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.INTERNAL_EMAIL_ENABLED) && EmailUtils.isInternalEmailAddress(recipient, this.defaultMigEmailDomain)) {
            i.remove();
         }
      }

      if (recipients.size() != 0) {
         long ts = System.currentTimeMillis();
         this.emailQueue.add(emailNote);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_ENQUEUE_EMAIL_ENABLED)) {
            int maxEmailMsgLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_EMAIL_MESSAGE_LENGTH_FOR_ENQUEUE_LOG, 100);
            int maxEmailSubjectLenth = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_EMAIL_SUBJECT_LENGTH_FOR_ENQUEUE_LOG, 50);
            emailNoteEnqueueLog.info(String.format("|%s|%s|%s|%s|%s|%s", ts, emailNote.getID(), emailNote.getSender(), emailNote.getRecipients(), StringUtil.customizeStringForLogging(emailNote.getSubject(), maxEmailSubjectLenth), StringUtil.customizeStringForLogging(emailNote.getText(), maxEmailMsgLength)));
         }

      }
   }
}
