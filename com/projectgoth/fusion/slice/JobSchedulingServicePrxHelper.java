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

public final class JobSchedulingServicePrxHelper extends ObjectPrxHelperBase implements JobSchedulingServicePrx {
   public void rescheduleFusionGroupEvent(GroupEvent event) throws FusionException {
      this.rescheduleFusionGroupEvent(event, (Map)null, false);
   }

   public void rescheduleFusionGroupEvent(GroupEvent event, Map<String, String> __ctx) throws FusionException {
      this.rescheduleFusionGroupEvent(event, __ctx, true);
   }

   private void rescheduleFusionGroupEvent(GroupEvent event, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("rescheduleFusionGroupEvent");
            __delBase = this.__getDelegate(false);
            _JobSchedulingServiceDel __del = (_JobSchedulingServiceDel)__delBase;
            __del.rescheduleFusionGroupEvent(event, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int scheduleFusionGroupEvent(GroupEvent event) throws FusionException {
      return this.scheduleFusionGroupEvent(event, (Map)null, false);
   }

   public int scheduleFusionGroupEvent(GroupEvent event, Map<String, String> __ctx) throws FusionException {
      return this.scheduleFusionGroupEvent(event, __ctx, true);
   }

   private int scheduleFusionGroupEvent(GroupEvent event, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("scheduleFusionGroupEvent");
            __delBase = this.__getDelegate(false);
            _JobSchedulingServiceDel __del = (_JobSchedulingServiceDel)__delBase;
            return __del.scheduleFusionGroupEvent(event, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String scheduleFusionGroupEventNotificationViaAlert(int eventId, int groupId, long time, String message) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaAlert(eventId, groupId, time, message, (Map)null, false);
   }

   public String scheduleFusionGroupEventNotificationViaAlert(int eventId, int groupId, long time, String message, Map<String, String> __ctx) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaAlert(eventId, groupId, time, message, __ctx, true);
   }

   private String scheduleFusionGroupEventNotificationViaAlert(int eventId, int groupId, long time, String message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("scheduleFusionGroupEventNotificationViaAlert");
            __delBase = this.__getDelegate(false);
            _JobSchedulingServiceDel __del = (_JobSchedulingServiceDel)__delBase;
            return __del.scheduleFusionGroupEventNotificationViaAlert(eventId, groupId, time, message, __ctx);
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String scheduleFusionGroupEventNotificationViaEmail(int eventId, int groupId, long time, EmailUserNotification note) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaEmail(eventId, groupId, time, note, (Map)null, false);
   }

   public String scheduleFusionGroupEventNotificationViaEmail(int eventId, int groupId, long time, EmailUserNotification note, Map<String, String> __ctx) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaEmail(eventId, groupId, time, note, __ctx, true);
   }

   private String scheduleFusionGroupEventNotificationViaEmail(int eventId, int groupId, long time, EmailUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("scheduleFusionGroupEventNotificationViaEmail");
            __delBase = this.__getDelegate(false);
            _JobSchedulingServiceDel __del = (_JobSchedulingServiceDel)__delBase;
            return __del.scheduleFusionGroupEventNotificationViaEmail(eventId, groupId, time, note, __ctx);
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String scheduleFusionGroupEventNotificationViaSMS(int eventId, int groupId, long time, SMSUserNotification note) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaSMS(eventId, groupId, time, note, (Map)null, false);
   }

   public String scheduleFusionGroupEventNotificationViaSMS(int eventId, int groupId, long time, SMSUserNotification note, Map<String, String> __ctx) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaSMS(eventId, groupId, time, note, __ctx, true);
   }

   private String scheduleFusionGroupEventNotificationViaSMS(int eventId, int groupId, long time, SMSUserNotification note, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("scheduleFusionGroupEventNotificationViaSMS");
            __delBase = this.__getDelegate(false);
            _JobSchedulingServiceDel __del = (_JobSchedulingServiceDel)__delBase;
            return __del.scheduleFusionGroupEventNotificationViaSMS(eventId, groupId, time, note, __ctx);
         } catch (LocalExceptionWrapper var11) {
            this.__handleExceptionWrapper(__delBase, var11, (OutgoingAsync)null);
         } catch (LocalException var12) {
            __cnt = this.__handleException(__delBase, var12, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void triggerJob(String jobName, String jobGroup, Map<String, String> jobDataMap) throws FusionException {
      this.triggerJob(jobName, jobGroup, jobDataMap, (Map)null, false);
   }

   public void triggerJob(String jobName, String jobGroup, Map<String, String> jobDataMap, Map<String, String> __ctx) throws FusionException {
      this.triggerJob(jobName, jobGroup, jobDataMap, __ctx, true);
   }

   private void triggerJob(String jobName, String jobGroup, Map<String, String> jobDataMap, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("triggerJob");
            __delBase = this.__getDelegate(false);
            _JobSchedulingServiceDel __del = (_JobSchedulingServiceDel)__delBase;
            __del.triggerJob(jobName, jobGroup, jobDataMap, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void unscheduleFusionGroupEvent(int groupEventID) throws FusionException {
      this.unscheduleFusionGroupEvent(groupEventID, (Map)null, false);
   }

   public void unscheduleFusionGroupEvent(int groupEventID, Map<String, String> __ctx) throws FusionException {
      this.unscheduleFusionGroupEvent(groupEventID, __ctx, true);
   }

   private void unscheduleFusionGroupEvent(int groupEventID, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("unscheduleFusionGroupEvent");
            __delBase = this.__getDelegate(false);
            _JobSchedulingServiceDel __del = (_JobSchedulingServiceDel)__delBase;
            __del.unscheduleFusionGroupEvent(groupEventID, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static JobSchedulingServicePrx checkedCast(ObjectPrx __obj) {
      JobSchedulingServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (JobSchedulingServicePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::JobSchedulingService")) {
               JobSchedulingServicePrxHelper __h = new JobSchedulingServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (JobSchedulingServicePrx)__d;
   }

   public static JobSchedulingServicePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      JobSchedulingServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (JobSchedulingServicePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::JobSchedulingService", __ctx)) {
               JobSchedulingServicePrxHelper __h = new JobSchedulingServicePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (JobSchedulingServicePrx)__d;
   }

   public static JobSchedulingServicePrx checkedCast(ObjectPrx __obj, String __facet) {
      JobSchedulingServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::JobSchedulingService")) {
               JobSchedulingServicePrxHelper __h = new JobSchedulingServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static JobSchedulingServicePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      JobSchedulingServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::JobSchedulingService", __ctx)) {
               JobSchedulingServicePrxHelper __h = new JobSchedulingServicePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static JobSchedulingServicePrx uncheckedCast(ObjectPrx __obj) {
      JobSchedulingServicePrx __d = null;
      if (__obj != null) {
         try {
            __d = (JobSchedulingServicePrx)__obj;
         } catch (ClassCastException var4) {
            JobSchedulingServicePrxHelper __h = new JobSchedulingServicePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (JobSchedulingServicePrx)__d;
   }

   public static JobSchedulingServicePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      JobSchedulingServicePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         JobSchedulingServicePrxHelper __h = new JobSchedulingServicePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _JobSchedulingServiceDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _JobSchedulingServiceDelD();
   }

   public static void __write(BasicStream __os, JobSchedulingServicePrx v) {
      __os.writeProxy(v);
   }

   public static JobSchedulingServicePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         JobSchedulingServicePrxHelper result = new JobSchedulingServicePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
