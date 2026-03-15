package com.projectgoth.fusion.slice;

import Ice.Current;
import java.util.Map;

public interface _UserNotificationServiceOperations {
   void notifyFusionGroupViaAlert(int var1, String var2, Current var3) throws FusionException;

   void notifyFusionUserViaAlert(String var1, String var2, Current var3) throws FusionException;

   void notifyFusionGroupAnnouncementViaEmail(int var1, EmailUserNotification var2, Current var3) throws FusionException;

   void notifyFusionGroupPostSubscribersViaEmail(int var1, EmailUserNotification var2, Current var3) throws FusionException;

   void notifyFusionUserViaEmail(String var1, EmailUserNotification var2, Current var3) throws FusionException;

   void notifyUserViaEmail(EmailUserNotification var1, Current var2) throws FusionException;

   void notifyUsersViaFusionEmail(String var1, String var2, String[] var3, EmailUserNotification var4, Current var5) throws FusionException;

   void sendEmailFromNoReply(String var1, String var2, String var3, Current var4) throws FusionException;

   void sendEmailFromNoReplyWithType(String var1, String var2, String var3, String var4, Current var5) throws FusionException;

   void notifyFusionGroupAnnouncementViaSMS(int var1, SMSUserNotification var2, Current var3) throws FusionException;

   void notifyFusionGroupEventViaSMS(int var1, SMSUserNotification var2, Current var3) throws FusionException;

   void notifyFusionUserViaSMS(String var1, SMSUserNotification var2, Current var3) throws FusionException;

   void notifyFusionUser(Message var1, Current var2) throws FusionException;

   Map<Integer, Integer> getPendingNotificationsForUser(int var1, Current var2) throws FusionException;

   void clearNotificationsForUser(int var1, int var2, String[] var3, Current var4) throws FusionException;

   void clearAllNotificationsForUser(int var1, Current var2) throws FusionException;

   void clearAllNotificationsByTypeForUser(int var1, int var2, Current var3) throws FusionException;

   Map<Integer, Integer> getUnreadNotificationCountForUser(int var1, Current var2) throws FusionException;

   void clearAllUnreadNotificationCountForUser(int var1, boolean var2, Current var3) throws FusionException;

   Map<Integer, Map<String, Map<String, String>>> getPendingNotificationDataForUser(int var1, Current var2) throws FusionException;

   Map<Integer, Map<String, Map<String, String>>> getUnreadPendingNotificationDataForUser(int var1, Current var2) throws FusionException;

   Map<String, Map<String, String>> getPendingNotificationDataForUserByType(int var1, int var2, Current var3) throws FusionException;

   void sendNotificationCounterToUser(int var1, Current var2);

   void sendTemplatizedEmailFromNoReply(String var1, int var2, Map<String, String> var3, Current var4) throws FusionException;
}
