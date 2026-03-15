package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectImpl;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.OutputStream;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import java.util.Arrays;
import java.util.Map;

public abstract class _UserNotificationServiceDisp extends ObjectImpl implements UserNotificationService {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::UserNotificationService"};
   private static final String[] __all = new String[]{"clearAllNotificationsByTypeForUser", "clearAllNotificationsForUser", "clearAllUnreadNotificationCountForUser", "clearNotificationsForUser", "getPendingNotificationDataForUser", "getPendingNotificationDataForUserByType", "getPendingNotificationsForUser", "getUnreadNotificationCountForUser", "getUnreadPendingNotificationDataForUser", "ice_id", "ice_ids", "ice_isA", "ice_ping", "notifyFusionGroupAnnouncementViaEmail", "notifyFusionGroupAnnouncementViaSMS", "notifyFusionGroupEventViaSMS", "notifyFusionGroupPostSubscribersViaEmail", "notifyFusionGroupViaAlert", "notifyFusionUser", "notifyFusionUserViaAlert", "notifyFusionUserViaEmail", "notifyFusionUserViaSMS", "notifyUserViaEmail", "notifyUsersViaFusionEmail", "sendEmailFromNoReply", "sendEmailFromNoReplyWithType", "sendNotificationCounterToUser", "sendTemplatizedEmailFromNoReply"};

   protected void ice_copyStateFrom(Object __obj) throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
   }

   public final void clearAllNotificationsByTypeForUser(int userId, int notfnType) throws FusionException {
      this.clearAllNotificationsByTypeForUser(userId, notfnType, (Current)null);
   }

   public final void clearAllNotificationsForUser(int userId) throws FusionException {
      this.clearAllNotificationsForUser(userId, (Current)null);
   }

   public final void clearAllUnreadNotificationCountForUser(int userId, boolean resetAll) throws FusionException {
      this.clearAllUnreadNotificationCountForUser(userId, resetAll, (Current)null);
   }

   public final void clearNotificationsForUser(int userId, int notfnType, String[] keys) throws FusionException {
      this.clearNotificationsForUser(userId, notfnType, keys, (Current)null);
   }

   public final Map<Integer, Map<String, Map<String, String>>> getPendingNotificationDataForUser(int userId) throws FusionException {
      return this.getPendingNotificationDataForUser(userId, (Current)null);
   }

   public final Map<String, Map<String, String>> getPendingNotificationDataForUserByType(int userId, int notificationType) throws FusionException {
      return this.getPendingNotificationDataForUserByType(userId, notificationType, (Current)null);
   }

   public final Map<Integer, Integer> getPendingNotificationsForUser(int userId) throws FusionException {
      return this.getPendingNotificationsForUser(userId, (Current)null);
   }

   public final Map<Integer, Integer> getUnreadNotificationCountForUser(int userId) throws FusionException {
      return this.getUnreadNotificationCountForUser(userId, (Current)null);
   }

   public final Map<Integer, Map<String, Map<String, String>>> getUnreadPendingNotificationDataForUser(int userId) throws FusionException {
      return this.getUnreadPendingNotificationDataForUser(userId, (Current)null);
   }

   public final void notifyFusionGroupAnnouncementViaEmail(int groupId, EmailUserNotification note) throws FusionException {
      this.notifyFusionGroupAnnouncementViaEmail(groupId, note, (Current)null);
   }

   public final void notifyFusionGroupAnnouncementViaSMS(int groupId, SMSUserNotification note) throws FusionException {
      this.notifyFusionGroupAnnouncementViaSMS(groupId, note, (Current)null);
   }

   public final void notifyFusionGroupEventViaSMS(int groupId, SMSUserNotification note) throws FusionException {
      this.notifyFusionGroupEventViaSMS(groupId, note, (Current)null);
   }

   public final void notifyFusionGroupPostSubscribersViaEmail(int userPostId, EmailUserNotification note) throws FusionException {
      this.notifyFusionGroupPostSubscribersViaEmail(userPostId, note, (Current)null);
   }

   public final void notifyFusionGroupViaAlert(int groupId, String message) throws FusionException {
      this.notifyFusionGroupViaAlert(groupId, message, (Current)null);
   }

   public final void notifyFusionUser(Message msg) throws FusionException {
      this.notifyFusionUser(msg, (Current)null);
   }

   public final void notifyFusionUserViaAlert(String username, String message) throws FusionException {
      this.notifyFusionUserViaAlert(username, message, (Current)null);
   }

   public final void notifyFusionUserViaEmail(String username, EmailUserNotification note) throws FusionException {
      this.notifyFusionUserViaEmail(username, note, (Current)null);
   }

   public final void notifyFusionUserViaSMS(String username, SMSUserNotification note) throws FusionException {
      this.notifyFusionUserViaSMS(username, note, (Current)null);
   }

   public final void notifyUserViaEmail(EmailUserNotification note) throws FusionException {
      this.notifyUserViaEmail(note, (Current)null);
   }

   public final void notifyUsersViaFusionEmail(String sender, String senderPassword, String[] recipients, EmailUserNotification note) throws FusionException {
      this.notifyUsersViaFusionEmail(sender, senderPassword, recipients, note, (Current)null);
   }

   public final void sendEmailFromNoReply(String destinationAddress, String subject, String body) throws FusionException {
      this.sendEmailFromNoReply(destinationAddress, subject, body, (Current)null);
   }

   public final void sendEmailFromNoReplyWithType(String destinationAddress, String subject, String body, String mimeType) throws FusionException {
      this.sendEmailFromNoReplyWithType(destinationAddress, subject, body, mimeType, (Current)null);
   }

   public final void sendNotificationCounterToUser(int userId) {
      this.sendNotificationCounterToUser(userId, (Current)null);
   }

   public final void sendTemplatizedEmailFromNoReply(String destinationEmailAddress, int templateId, Map<String, String> templateParam) throws FusionException {
      this.sendTemplatizedEmailFromNoReply(destinationEmailAddress, templateId, templateParam, (Current)null);
   }

   public static DispatchStatus ___notifyFusionGroupViaAlert(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int groupId = __is.readInt();
      String message = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionGroupViaAlert(groupId, message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyFusionUserViaAlert(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String message = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionUserViaAlert(username, message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyFusionGroupAnnouncementViaEmail(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int groupId = __is.readInt();
      EmailUserNotificationHolder note = new EmailUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionGroupAnnouncementViaEmail(groupId, note.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyFusionGroupPostSubscribersViaEmail(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userPostId = __is.readInt();
      EmailUserNotificationHolder note = new EmailUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionGroupPostSubscribersViaEmail(userPostId, note.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyFusionUserViaEmail(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      EmailUserNotificationHolder note = new EmailUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionUserViaEmail(username, note.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyUserViaEmail(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      EmailUserNotificationHolder note = new EmailUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyUserViaEmail(note.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyUsersViaFusionEmail(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String sender = __is.readString();
      String senderPassword = __is.readString();
      String[] recipients = StringArrayHelper.read(__is);
      EmailUserNotificationHolder note = new EmailUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyUsersViaFusionEmail(sender, senderPassword, recipients, note.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendEmailFromNoReply(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String destinationAddress = __is.readString();
      String subject = __is.readString();
      String body = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendEmailFromNoReply(destinationAddress, subject, body, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendEmailFromNoReplyWithType(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String destinationAddress = __is.readString();
      String subject = __is.readString();
      String body = __is.readString();
      String mimeType = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendEmailFromNoReplyWithType(destinationAddress, subject, body, mimeType, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var10) {
         __os.writeUserException(var10);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyFusionGroupAnnouncementViaSMS(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int groupId = __is.readInt();
      SMSUserNotificationHolder note = new SMSUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionGroupAnnouncementViaSMS(groupId, note.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyFusionGroupEventViaSMS(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int groupId = __is.readInt();
      SMSUserNotificationHolder note = new SMSUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionGroupEventViaSMS(groupId, note.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyFusionUserViaSMS(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      SMSUserNotificationHolder note = new SMSUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionUserViaSMS(username, note.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___notifyFusionUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageHolder msg = new MessageHolder();
      __is.readObject(msg.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.notifyFusionUser(msg.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getPendingNotificationsForUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         Map<Integer, Integer> __ret = __obj.getPendingNotificationsForUser(userId, __current);
         NotificationMapHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___clearNotificationsForUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      int notfnType = __is.readInt();
      String[] keys = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.clearNotificationsForUser(userId, notfnType, keys, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___clearAllNotificationsForUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.clearAllNotificationsForUser(userId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___clearAllNotificationsByTypeForUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      int notfnType = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.clearAllNotificationsByTypeForUser(userId, notfnType, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getUnreadNotificationCountForUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         Map<Integer, Integer> __ret = __obj.getUnreadNotificationCountForUser(userId, __current);
         NotificationMapHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___clearAllUnreadNotificationCountForUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      boolean resetAll = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.clearAllUnreadNotificationCountForUser(userId, resetAll, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getPendingNotificationDataForUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         Map<Integer, Map<String, Map<String, String>>> __ret = __obj.getPendingNotificationDataForUser(userId, __current);
         NotificationDataMapHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getUnreadPendingNotificationDataForUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         Map<Integer, Map<String, Map<String, String>>> __ret = __obj.getUnreadPendingNotificationDataForUser(userId, __current);
         NotificationDataMapHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getPendingNotificationDataForUserByType(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      int notificationType = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         Map<String, Map<String, String>> __ret = __obj.getPendingNotificationDataForUserByType(userId, notificationType, __current);
         NotificationDataEntryHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___sendNotificationCounterToUser(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userId = __is.readInt();
      __is.endReadEncaps();
      __obj.sendNotificationCounterToUser(userId, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___sendTemplatizedEmailFromNoReply(UserNotificationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String destinationEmailAddress = __is.readString();
      int templateId = __is.readInt();
      Map<String, String> templateParam = ParamMapHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.sendTemplatizedEmailFromNoReply(destinationEmailAddress, templateId, templateParam, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___clearAllNotificationsByTypeForUser(this, in, __current);
         case 1:
            return ___clearAllNotificationsForUser(this, in, __current);
         case 2:
            return ___clearAllUnreadNotificationCountForUser(this, in, __current);
         case 3:
            return ___clearNotificationsForUser(this, in, __current);
         case 4:
            return ___getPendingNotificationDataForUser(this, in, __current);
         case 5:
            return ___getPendingNotificationDataForUserByType(this, in, __current);
         case 6:
            return ___getPendingNotificationsForUser(this, in, __current);
         case 7:
            return ___getUnreadNotificationCountForUser(this, in, __current);
         case 8:
            return ___getUnreadPendingNotificationDataForUser(this, in, __current);
         case 9:
            return ___ice_id(this, in, __current);
         case 10:
            return ___ice_ids(this, in, __current);
         case 11:
            return ___ice_isA(this, in, __current);
         case 12:
            return ___ice_ping(this, in, __current);
         case 13:
            return ___notifyFusionGroupAnnouncementViaEmail(this, in, __current);
         case 14:
            return ___notifyFusionGroupAnnouncementViaSMS(this, in, __current);
         case 15:
            return ___notifyFusionGroupEventViaSMS(this, in, __current);
         case 16:
            return ___notifyFusionGroupPostSubscribersViaEmail(this, in, __current);
         case 17:
            return ___notifyFusionGroupViaAlert(this, in, __current);
         case 18:
            return ___notifyFusionUser(this, in, __current);
         case 19:
            return ___notifyFusionUserViaAlert(this, in, __current);
         case 20:
            return ___notifyFusionUserViaEmail(this, in, __current);
         case 21:
            return ___notifyFusionUserViaSMS(this, in, __current);
         case 22:
            return ___notifyUserViaEmail(this, in, __current);
         case 23:
            return ___notifyUsersViaFusionEmail(this, in, __current);
         case 24:
            return ___sendEmailFromNoReply(this, in, __current);
         case 25:
            return ___sendEmailFromNoReplyWithType(this, in, __current);
         case 26:
            return ___sendNotificationCounterToUser(this, in, __current);
         case 27:
            return ___sendTemplatizedEmailFromNoReply(this, in, __current);
         default:
            assert false;

            throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
         }
      }
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::UserNotificationService was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::UserNotificationService was not generated with stream support";
      throw ex;
   }
}
