package com.projectgoth.fusion.slice;

import java.util.Map;

public interface _JobSchedulingServiceOperationsNC {
   String scheduleFusionGroupEventNotificationViaEmail(int var1, int var2, long var3, EmailUserNotification var5) throws FusionException;

   String scheduleFusionGroupEventNotificationViaSMS(int var1, int var2, long var3, SMSUserNotification var5) throws FusionException;

   String scheduleFusionGroupEventNotificationViaAlert(int var1, int var2, long var3, String var5) throws FusionException;

   int scheduleFusionGroupEvent(GroupEvent var1) throws FusionException;

   void unscheduleFusionGroupEvent(int var1) throws FusionException;

   void rescheduleFusionGroupEvent(GroupEvent var1) throws FusionException;

   void triggerJob(String var1, String var2, Map<String, String> var3) throws FusionException;
}
