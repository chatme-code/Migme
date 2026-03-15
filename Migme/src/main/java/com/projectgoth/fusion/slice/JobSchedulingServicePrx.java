package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface JobSchedulingServicePrx extends ObjectPrx {
   String scheduleFusionGroupEventNotificationViaEmail(int var1, int var2, long var3, EmailUserNotification var5) throws FusionException;

   String scheduleFusionGroupEventNotificationViaEmail(int var1, int var2, long var3, EmailUserNotification var5, Map<String, String> var6) throws FusionException;

   String scheduleFusionGroupEventNotificationViaSMS(int var1, int var2, long var3, SMSUserNotification var5) throws FusionException;

   String scheduleFusionGroupEventNotificationViaSMS(int var1, int var2, long var3, SMSUserNotification var5, Map<String, String> var6) throws FusionException;

   String scheduleFusionGroupEventNotificationViaAlert(int var1, int var2, long var3, String var5) throws FusionException;

   String scheduleFusionGroupEventNotificationViaAlert(int var1, int var2, long var3, String var5, Map<String, String> var6) throws FusionException;

   int scheduleFusionGroupEvent(GroupEvent var1) throws FusionException;

   int scheduleFusionGroupEvent(GroupEvent var1, Map<String, String> var2) throws FusionException;

   void unscheduleFusionGroupEvent(int var1) throws FusionException;

   void unscheduleFusionGroupEvent(int var1, Map<String, String> var2) throws FusionException;

   void rescheduleFusionGroupEvent(GroupEvent var1) throws FusionException;

   void rescheduleFusionGroupEvent(GroupEvent var1, Map<String, String> var2) throws FusionException;

   void triggerJob(String var1, String var2, Map<String, String> var3) throws FusionException;

   void triggerJob(String var1, String var2, Map<String, String> var3, Map<String, String> var4) throws FusionException;
}
