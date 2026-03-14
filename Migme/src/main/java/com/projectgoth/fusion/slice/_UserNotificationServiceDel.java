package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _UserNotificationServiceDel extends _ObjectDel {
   void notifyFusionGroupViaAlert(int var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyFusionUserViaAlert(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyFusionGroupAnnouncementViaEmail(int var1, EmailUserNotification var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyFusionGroupPostSubscribersViaEmail(int var1, EmailUserNotification var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyFusionUserViaEmail(String var1, EmailUserNotification var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyUserViaEmail(EmailUserNotification var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void notifyUsersViaFusionEmail(String var1, String var2, String[] var3, EmailUserNotification var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void sendEmailFromNoReply(String var1, String var2, String var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void sendEmailFromNoReplyWithType(String var1, String var2, String var3, String var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void notifyFusionGroupAnnouncementViaSMS(int var1, SMSUserNotification var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyFusionGroupEventViaSMS(int var1, SMSUserNotification var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyFusionUserViaSMS(String var1, SMSUserNotification var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyFusionUser(Message var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   Map<Integer, Integer> getPendingNotificationsForUser(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void clearNotificationsForUser(int var1, int var2, String[] var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void clearAllNotificationsForUser(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void clearAllNotificationsByTypeForUser(int var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   Map<Integer, Integer> getUnreadNotificationCountForUser(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void clearAllUnreadNotificationCountForUser(int var1, boolean var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   Map<Integer, Map<String, Map<String, String>>> getPendingNotificationDataForUser(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   Map<Integer, Map<String, Map<String, String>>> getUnreadPendingNotificationDataForUser(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   Map<String, Map<String, String>> getPendingNotificationDataForUserByType(int var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void sendNotificationCounterToUser(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void sendTemplatizedEmailFromNoReply(String var1, int var2, Map<String, String> var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;
}
