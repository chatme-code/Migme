package com.projectgoth.fusion.slice;

import Ice.Current;
import java.util.Map;

public interface _JobSchedulingServiceOperations {
   String scheduleFusionGroupEventNotificationViaEmail(int var1, int var2, long var3, EmailUserNotification var5, Current var6) throws FusionException;

   String scheduleFusionGroupEventNotificationViaSMS(int var1, int var2, long var3, SMSUserNotification var5, Current var6) throws FusionException;

   String scheduleFusionGroupEventNotificationViaAlert(int var1, int var2, long var3, String var5, Current var6) throws FusionException;

   int scheduleFusionGroupEvent(GroupEvent var1, Current var2) throws FusionException;

   void unscheduleFusionGroupEvent(int var1, Current var2) throws FusionException;

   void rescheduleFusionGroupEvent(GroupEvent var1, Current var2) throws FusionException;

   void triggerJob(String var1, String var2, Map<String, String> var3, Current var4) throws FusionException;
}
