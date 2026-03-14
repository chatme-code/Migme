package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;
import java.util.Map;

public class _JobSchedulingServiceTie extends _JobSchedulingServiceDisp implements TieBase {
   private _JobSchedulingServiceOperations _ice_delegate;

   public _JobSchedulingServiceTie() {
   }

   public _JobSchedulingServiceTie(_JobSchedulingServiceOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_JobSchedulingServiceOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _JobSchedulingServiceTie) ? false : this._ice_delegate.equals(((_JobSchedulingServiceTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void rescheduleFusionGroupEvent(GroupEvent event, Current __current) throws FusionException {
      this._ice_delegate.rescheduleFusionGroupEvent(event, __current);
   }

   public int scheduleFusionGroupEvent(GroupEvent event, Current __current) throws FusionException {
      return this._ice_delegate.scheduleFusionGroupEvent(event, __current);
   }

   public String scheduleFusionGroupEventNotificationViaAlert(int eventId, int groupId, long time, String message, Current __current) throws FusionException {
      return this._ice_delegate.scheduleFusionGroupEventNotificationViaAlert(eventId, groupId, time, message, __current);
   }

   public String scheduleFusionGroupEventNotificationViaEmail(int eventId, int groupId, long time, EmailUserNotification note, Current __current) throws FusionException {
      return this._ice_delegate.scheduleFusionGroupEventNotificationViaEmail(eventId, groupId, time, note, __current);
   }

   public String scheduleFusionGroupEventNotificationViaSMS(int eventId, int groupId, long time, SMSUserNotification note, Current __current) throws FusionException {
      return this._ice_delegate.scheduleFusionGroupEventNotificationViaSMS(eventId, groupId, time, note, __current);
   }

   public void triggerJob(String jobName, String jobGroup, Map<String, String> jobDataMap, Current __current) throws FusionException {
      this._ice_delegate.triggerJob(jobName, jobGroup, jobDataMap, __current);
   }

   public void unscheduleFusionGroupEvent(int groupEventID, Current __current) throws FusionException {
      this._ice_delegate.unscheduleFusionGroupEvent(groupEventID, __current);
   }
}
