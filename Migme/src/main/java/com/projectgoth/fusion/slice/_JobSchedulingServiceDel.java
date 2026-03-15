package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _JobSchedulingServiceDel extends _ObjectDel {
   String scheduleFusionGroupEventNotificationViaEmail(int var1, int var2, long var3, EmailUserNotification var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   String scheduleFusionGroupEventNotificationViaSMS(int var1, int var2, long var3, SMSUserNotification var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   String scheduleFusionGroupEventNotificationViaAlert(int var1, int var2, long var3, String var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   int scheduleFusionGroupEvent(GroupEvent var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void unscheduleFusionGroupEvent(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void rescheduleFusionGroupEvent(GroupEvent var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void triggerJob(String var1, String var2, Map<String, String> var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;
}
