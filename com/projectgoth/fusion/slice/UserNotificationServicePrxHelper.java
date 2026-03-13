package com.projectgoth.fusion.slice;

import Ice.FacetNotExistException;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDel;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.OutgoingAsync;
import java.util.Map;

public final class UserNotificationServicePrxHelper extends ObjectPrxHelperBase implements UserNotificationServicePrx {
   public void clearAllNotificationsByTypeForUser(int userId, int notfnType) throws FusionException {
      this.clearAllNotificationsByTypeForUser(userId, notfnType, (Map)null, false);
   }

   public void clearAllNotificationsByTypeForUser(int userId, int notfnType, Map<String, String> __ctx) throws FusionException {
      this.clearAllNotificationsByTypeForUser(userId, notfnType, __ctx, true);
   }

   private void clearAllNotificationsByTypeForUser(int userId, int notfnType, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("clearAllNotificationsByTypeForUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.clearAllNotificationsByTypeForUser(userId, notfnType, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void clearAllNotificationsForUser(int userId) throws FusionException {
      this.clearAllNotificationsForUser(userId, (Map)null, false);
   }

   public void clearAllNotificationsForUser(int userId, Map<String, String> __ctx) throws FusionException {
      this.clearAllNotificationsForUser(userId, __ctx, true);
   }

   private void clearAllNotificationsForUser(int userId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("clearAllNotificationsForUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.clearAllNotificationsForUser(userId, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void clearAllUnreadNotificationCountForUser(int userId, boolean resetAll) throws FusionException {
      this.clearAllUnreadNotificationCountForUser(userId, resetAll, (Map)null, false);
   }

   public void clearAllUnreadNotificationCountForUser(int userId, boolean resetAll, Map<String, String> __ctx) throws FusionException {
      this.clearAllUnreadNotificationCountForUser(userId, resetAll, __ctx, true);
   }

   private void clearAllUnreadNotificationCountForUser(int userId, boolean resetAll, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("clearAllUnreadNotificationCountForUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.clearAllUnreadNotificationCountForUser(userId, resetAll, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void clearNotificationsForUser(int userId, int notfnType, String[] keys) throws FusionException {
      this.clearNotificationsForUser(userId, notfnType, keys, (Map)null, false);
   }

   public void clearNotificationsForUser(int userId, int notfnType, String[] keys, Map<String, String> __ctx) throws FusionException {
      this.clearNotificationsForUser(userId, notfnType, keys, __ctx, true);
   }

   private void clearNotificationsForUser(int userId, int notfnType, String[] keys, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("clearNotificationsForUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.clearNotificationsForUser(userId, notfnType, keys, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Map<Integer, Map<String, Map<String, String>>> getPendingNotificationDataForUser(int userId) throws FusionException {
      return this.getPendingNotificationDataForUser(userId, (Map)null, false);
   }

   public Map<Integer, Map<String, Map<String, String>>> getPendingNotificationDataForUser(int userId, Map<String, String> __ctx) throws FusionException {
      return this.getPendingNotificationDataForUser(userId, __ctx, true);
   }

   private Map<Integer, Map<String, Map<String, String>>> getPendingNotificationDataForUser(int userId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getPendingNotificationDataForUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            return __del.getPendingNotificationDataForUser(userId, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Map<String, Map<String, String>> getPendingNotificationDataForUserByType(int userId, int notificationType) throws FusionException {
      return this.getPendingNotificationDataForUserByType(userId, notificationType, (Map)null, false);
   }

   public Map<String, Map<String, String>> getPendingNotificationDataForUserByType(int userId, int notificationType, Map<String, String> __ctx) throws FusionException {
      return this.getPendingNotificationDataForUserByType(userId, notificationType, __ctx, true);
   }

   private Map<String, Map<String, String>> getPendingNotificationDataForUserByType(int userId, int notificationType, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getPendingNotificationDataForUserByType");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            return __del.getPendingNotificationDataForUserByType(userId, notificationType, __ctx);
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Map<Integer, Integer> getPendingNotificationsForUser(int userId) throws FusionException {
      return this.getPendingNotificationsForUser(userId, (Map)null, false);
   }

   public Map<Integer, Integer> getPendingNotificationsForUser(int userId, Map<String, String> __ctx) throws FusionException {
      return this.getPendingNotificationsForUser(userId, __ctx, true);
   }

   private Map<Integer, Integer> getPendingNotificationsForUser(int userId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getPendingNotificationsForUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            return __del.getPendingNotificationsForUser(userId, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Map<Integer, Integer> getUnreadNotificationCountForUser(int userId) throws FusionException {
      return this.getUnreadNotificationCountForUser(userId, (Map)null, false);
   }

   public Map<Integer, Integer> getUnreadNotificationCountForUser(int userId, Map<String, String> __ctx) throws FusionException {
      return this.getUnreadNotificationCountForUser(userId, __ctx, true);
   }

   private Map<Integer, Integer> getUnreadNotificationCountForUser(int userId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUnreadNotificationCountForUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            return __del.getUnreadNotificationCountForUser(userId, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public Map<Integer, Map<String, Map<String, String>>> getUnreadPendingNotificationDataForUser(int userId) throws FusionException {
      return this.getUnreadPendingNotificationDataForUser(userId, (Map)null, false);
   }

   public Map<Integer, Map<String, Map<String, String>>> getUnreadPendingNotificationDataForUser(int userId, Map<String, String> __ctx) throws FusionException {
      return this.getUnreadPendingNotificationDataForUser(userId, __ctx, true);
   }

   private Map<Integer, Map<String, Map<String, String>>> getUnreadPendingNotificationDataForUser(int userId, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUnreadPendingNotificationDataForUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            return __del.getUnreadPendingNotificationDataForUser(userId, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionGroupAnnouncementViaEmail(int groupId, EmailUserNotification note) throws FusionException {
      this.notifyFusionGroupAnnouncementViaEmail(groupId, note, (Map)null, false);
   }

   public void notifyFusionGroupAnnouncementViaEmail(int groupId, EmailUserNotification note, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionGroupAnnouncementViaEmail(groupId, note, __ctx, true);
   }

   private void notifyFusionGroupAnnouncementViaEmail(int groupId, EmailUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionGroupAnnouncementViaEmail");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionGroupAnnouncementViaEmail(groupId, note, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionGroupAnnouncementViaSMS(int groupId, SMSUserNotification note) throws FusionException {
      this.notifyFusionGroupAnnouncementViaSMS(groupId, note, (Map)null, false);
   }

   public void notifyFusionGroupAnnouncementViaSMS(int groupId, SMSUserNotification note, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionGroupAnnouncementViaSMS(groupId, note, __ctx, true);
   }

   private void notifyFusionGroupAnnouncementViaSMS(int groupId, SMSUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionGroupAnnouncementViaSMS");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionGroupAnnouncementViaSMS(groupId, note, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionGroupEventViaSMS(int groupId, SMSUserNotification note) throws FusionException {
      this.notifyFusionGroupEventViaSMS(groupId, note, (Map)null, false);
   }

   public void notifyFusionGroupEventViaSMS(int groupId, SMSUserNotification note, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionGroupEventViaSMS(groupId, note, __ctx, true);
   }

   private void notifyFusionGroupEventViaSMS(int groupId, SMSUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionGroupEventViaSMS");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionGroupEventViaSMS(groupId, note, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionGroupPostSubscribersViaEmail(int userPostId, EmailUserNotification note) throws FusionException {
      this.notifyFusionGroupPostSubscribersViaEmail(userPostId, note, (Map)null, false);
   }

   public void notifyFusionGroupPostSubscribersViaEmail(int userPostId, EmailUserNotification note, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionGroupPostSubscribersViaEmail(userPostId, note, __ctx, true);
   }

   private void notifyFusionGroupPostSubscribersViaEmail(int userPostId, EmailUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionGroupPostSubscribersViaEmail");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionGroupPostSubscribersViaEmail(userPostId, note, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionGroupViaAlert(int groupId, String message) throws FusionException {
      this.notifyFusionGroupViaAlert(groupId, message, (Map)null, false);
   }

   public void notifyFusionGroupViaAlert(int groupId, String message, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionGroupViaAlert(groupId, message, __ctx, true);
   }

   private void notifyFusionGroupViaAlert(int groupId, String message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionGroupViaAlert");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionGroupViaAlert(groupId, message, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionUser(Message msg) throws FusionException {
      this.notifyFusionUser(msg, (Map)null, false);
   }

   public void notifyFusionUser(Message msg, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionUser(msg, __ctx, true);
   }

   private void notifyFusionUser(Message msg, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionUser");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionUser(msg, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionUserViaAlert(String username, String message) throws FusionException {
      this.notifyFusionUserViaAlert(username, message, (Map)null, false);
   }

   public void notifyFusionUserViaAlert(String username, String message, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionUserViaAlert(username, message, __ctx, true);
   }

   private void notifyFusionUserViaAlert(String username, String message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionUserViaAlert");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionUserViaAlert(username, message, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionUserViaEmail(String username, EmailUserNotification note) throws FusionException {
      this.notifyFusionUserViaEmail(username, note, (Map)null, false);
   }

   public void notifyFusionUserViaEmail(String username, EmailUserNotification note, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionUserViaEmail(username, note, __ctx, true);
   }

   private void notifyFusionUserViaEmail(String username, EmailUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionUserViaEmail");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionUserViaEmail(username, note, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyFusionUserViaSMS(String username, SMSUserNotification note) throws FusionException {
      this.notifyFusionUserViaSMS(username, note, (Map)null, false);
   }

   public void notifyFusionUserViaSMS(String username, SMSUserNotification note, Map<String, String> __ctx) throws FusionException {
      this.notifyFusionUserViaSMS(username, note, __ctx, true);
   }

   private void notifyFusionUserViaSMS(String username, SMSUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyFusionUserViaSMS");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyFusionUserViaSMS(username, note, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyUserViaEmail(EmailUserNotification note) throws FusionException {
      this.notifyUserViaEmail(note, (Map)null, false);
   }

   public void notifyUserViaEmail(EmailUserNotification note, Map<String, String> __ctx) throws FusionException {
      this.notifyUserViaEmail(note, __ctx, true);
   }

   private void notifyUserViaEmail(EmailUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyUserViaEmail");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyUserViaEmail(note, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyUsersViaFusionEmail(String sender, String senderPassword, String[] recipients, EmailUserNotification note) throws FusionException {
      this.notifyUsersViaFusionEmail(sender, senderPassword, recipients, note, (Map)null, false);
   }

   public void notifyUsersViaFusionEmail(String sender, String senderPassword, String[] recipients, EmailUserNotification note, Map<String, String> __ctx) throws FusionException {
      this.notifyUsersViaFusionEmail(sender, senderPassword, recipients, note, __ctx, true);
   }

   private void notifyUsersViaFusionEmail(String sender, String senderPassword, String[] recipients, EmailUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyUsersViaFusionEmail");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.notifyUsersViaFusionEmail(sender, senderPassword, recipients, note, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendEmailFromNoReply(String destinationAddress, String subject, String body) throws FusionException {
      this.sendEmailFromNoReply(destinationAddress, subject, body, (Map)null, false);
   }

   public void sendEmailFromNoReply(String destinationAddress, String subject, String body, Map<String, String> __ctx) throws FusionException {
      this.sendEmailFromNoReply(destinationAddress, subject, body, __ctx, true);
   }

   private void sendEmailFromNoReply(String destinationAddress, String subject, String body, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendEmailFromNoReply");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.sendEmailFromNoReply(destinationAddress, subject, body, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendEmailFromNoReplyWithType(String destinationAddress, String subject, String body, String mimeType) throws FusionException {
      this.sendEmailFromNoReplyWithType(destinationAddress, subject, body, mimeType, (Map)null, false);
   }

   public void sendEmailFromNoReplyWithType(String destinationAddress, String subject, String body, String mimeType, Map<String, String> __ctx) throws FusionException {
      this.sendEmailFromNoReplyWithType(destinationAddress, subject, body, mimeType, __ctx, true);
   }

   private void sendEmailFromNoReplyWithType(String destinationAddress, String subject, String body, String mimeType, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendEmailFromNoReplyWithType");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.sendEmailFromNoReplyWithType(destinationAddress, subject, body, mimeType, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendNotificationCounterToUser(int userId) {
      this.sendNotificationCounterToUser(userId, (Map)null, false);
   }

   public void sendNotificationCounterToUser(int userId, Map<String, String> __ctx) {
      this.sendNotificationCounterToUser(userId, __ctx, true);
   }

   private void sendNotificationCounterToUser(int userId, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.sendNotificationCounterToUser(userId, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendTemplatizedEmailFromNoReply(String destinationEmailAddress, int templateId, Map<String, String> templateParam) throws FusionException {
      this.sendTemplatizedEmailFromNoReply(destinationEmailAddress, templateId, templateParam, (Map)null, false);
   }

   public void sendTemplatizedEmailFromNoReply(String destinationEmailAddress, int templateId, Map<String, String> templateParam, Map<String, String> __ctx) throws FusionException {
      this.sendTemplatizedEmailFromNoReply(destinationEmailAddress, templateId, templateParam, __ctx, true);
   }

   private void sendTemplatizedEmailFromNoReply(String destinationEmailAddress, int templateId, Map<String, String> templateParam, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendTemplatizedEmailFromNoReply");
            __delBase = this.__getDelegate(false);
            _UserNotificationServiceDel __del = (_UserNotificationServiceDel)__delBase;
            __del.sendTemplatizedEmailFromNoReply(destinationEmailAddress, templateId, templateParam, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static UserNotificationServicePrx checkedCast(ObjectPrx __obj) {
      UserNotificationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserNotificationServicePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::UserNotificationService")) {
               UserNotificationServicePrxHelper __h = new UserNotificationServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (UserNotificationServicePrx)__d;
   }

   public static UserNotificationServicePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      UserNotificationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserNotificationServicePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::UserNotificationService", __ctx)) {
               UserNotificationServicePrxHelper __h = new UserNotificationServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (UserNotificationServicePrx)__d;
   }

   public static UserNotificationServicePrx checkedCast(ObjectPrx __obj, String __facet) {
      UserNotificationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::UserNotificationService")) {
               UserNotificationServicePrxHelper __h = new UserNotificationServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static UserNotificationServicePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      UserNotificationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::UserNotificationService", __ctx)) {
               UserNotificationServicePrxHelper __h = new UserNotificationServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static UserNotificationServicePrx uncheckedCast(ObjectPrx __obj) {
      UserNotificationServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (UserNotificationServicePrx)__obj;
         } catch (ClassCastException var4) {
            UserNotificationServicePrxHelper __h = new UserNotificationServicePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (UserNotificationServicePrx)__d;
   }

   public static UserNotificationServicePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      UserNotificationServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         UserNotificationServicePrxHelper __h = new UserNotificationServicePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _UserNotificationServiceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _UserNotificationServiceDelD();
   }

   public static void __write(BasicStream __os, UserNotificationServicePrx v) {
      __os.writeProxy(v);
   }

   public static UserNotificationServicePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         UserNotificationServicePrxHelper result = new UserNotificationServicePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
