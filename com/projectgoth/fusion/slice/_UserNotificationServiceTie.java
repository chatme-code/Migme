package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;
import java.util.Map;

public class _UserNotificationServiceTie extends _UserNotificationServiceDisp implements TieBase {
   private _UserNotificationServiceOperations _ice_delegate;

   public _UserNotificationServiceTie() {
   }

   public _UserNotificationServiceTie(_UserNotificationServiceOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_UserNotificationServiceOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _UserNotificationServiceTie) ? false : this._ice_delegate.equals(((_UserNotificationServiceTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void clearAllNotificationsByTypeForUser(int userId, int notfnType, Current __current) throws FusionException {
      this._ice_delegate.clearAllNotificationsByTypeForUser(userId, notfnType, __current);
   }

   public void clearAllNotificationsForUser(int userId, Current __current) throws FusionException {
      this._ice_delegate.clearAllNotificationsForUser(userId, __current);
   }

   public void clearAllUnreadNotificationCountForUser(int userId, boolean resetAll, Current __current) throws FusionException {
      this._ice_delegate.clearAllUnreadNotificationCountForUser(userId, resetAll, __current);
   }

   public void clearNotificationsForUser(int userId, int notfnType, String[] keys, Current __current) throws FusionException {
      this._ice_delegate.clearNotificationsForUser(userId, notfnType, keys, __current);
   }

   public Map<Integer, Map<String, Map<String, String>>> getPendingNotificationDataForUser(int userId, Current __current) throws FusionException {
      return this._ice_delegate.getPendingNotificationDataForUser(userId, __current);
   }

   public Map<String, Map<String, String>> getPendingNotificationDataForUserByType(int userId, int notificationType, Current __current) throws FusionException {
      return this._ice_delegate.getPendingNotificationDataForUserByType(userId, notificationType, __current);
   }

   public Map<Integer, Integer> getPendingNotificationsForUser(int userId, Current __current) throws FusionException {
      return this._ice_delegate.getPendingNotificationsForUser(userId, __current);
   }

   public Map<Integer, Integer> getUnreadNotificationCountForUser(int userId, Current __current) throws FusionException {
      return this._ice_delegate.getUnreadNotificationCountForUser(userId, __current);
   }

   public Map<Integer, Map<String, Map<String, String>>> getUnreadPendingNotificationDataForUser(int userId, Current __current) throws FusionException {
      return this._ice_delegate.getUnreadPendingNotificationDataForUser(userId, __current);
   }

   public void notifyFusionGroupAnnouncementViaEmail(int groupId, EmailUserNotification note, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionGroupAnnouncementViaEmail(groupId, note, __current);
   }

   public void notifyFusionGroupAnnouncementViaSMS(int groupId, SMSUserNotification note, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionGroupAnnouncementViaSMS(groupId, note, __current);
   }

   public void notifyFusionGroupEventViaSMS(int groupId, SMSUserNotification note, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionGroupEventViaSMS(groupId, note, __current);
   }

   public void notifyFusionGroupPostSubscribersViaEmail(int userPostId, EmailUserNotification note, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionGroupPostSubscribersViaEmail(userPostId, note, __current);
   }

   public void notifyFusionGroupViaAlert(int groupId, String message, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionGroupViaAlert(groupId, message, __current);
   }

   public void notifyFusionUser(Message msg, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionUser(msg, __current);
   }

   public void notifyFusionUserViaAlert(String username, String message, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionUserViaAlert(username, message, __current);
   }

   public void notifyFusionUserViaEmail(String username, EmailUserNotification note, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionUserViaEmail(username, note, __current);
   }

   public void notifyFusionUserViaSMS(String username, SMSUserNotification note, Current __current) throws FusionException {
      this._ice_delegate.notifyFusionUserViaSMS(username, note, __current);
   }

   public void notifyUserViaEmail(EmailUserNotification note, Current __current) throws FusionException {
      this._ice_delegate.notifyUserViaEmail(note, __current);
   }

   public void notifyUsersViaFusionEmail(String sender, String senderPassword, String[] recipients, EmailUserNotification note, Current __current) throws FusionException {
      this._ice_delegate.notifyUsersViaFusionEmail(sender, senderPassword, recipients, note, __current);
   }

   public void sendEmailFromNoReply(String destinationAddress, String subject, String body, Current __current) throws FusionException {
      this._ice_delegate.sendEmailFromNoReply(destinationAddress, subject, body, __current);
   }

   public void sendEmailFromNoReplyWithType(String destinationAddress, String subject, String body, String mimeType, Current __current) throws FusionException {
      this._ice_delegate.sendEmailFromNoReplyWithType(destinationAddress, subject, body, mimeType, __current);
   }

   public void sendNotificationCounterToUser(int userId, Current __current) {
      this._ice_delegate.sendNotificationCounterToUser(userId, __current);
   }

   public void sendTemplatizedEmailFromNoReply(String destinationEmailAddress, int templateId, Map<String, String> templateParam, Current __current) throws FusionException {
      this._ice_delegate.sendTemplatizedEmailFromNoReply(destinationEmailAddress, templateId, templateParam, __current);
   }
}
