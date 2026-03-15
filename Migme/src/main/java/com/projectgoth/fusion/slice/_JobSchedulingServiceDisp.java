package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectImpl;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.OutputStream;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import java.util.Arrays;
import java.util.Map;

public abstract class _JobSchedulingServiceDisp extends ObjectImpl implements JobSchedulingService {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::JobSchedulingService"};
   private static final String[] __all = new String[]{"ice_id", "ice_ids", "ice_isA", "ice_ping", "rescheduleFusionGroupEvent", "scheduleFusionGroupEvent", "scheduleFusionGroupEventNotificationViaAlert", "scheduleFusionGroupEventNotificationViaEmail", "scheduleFusionGroupEventNotificationViaSMS", "triggerJob", "unscheduleFusionGroupEvent"};

   protected void ice_copyStateFrom(Object __obj) throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
   }

   public final void rescheduleFusionGroupEvent(GroupEvent event) throws FusionException {
      this.rescheduleFusionGroupEvent(event, (Current)null);
   }

   public final int scheduleFusionGroupEvent(GroupEvent event) throws FusionException {
      return this.scheduleFusionGroupEvent(event, (Current)null);
   }

   public final String scheduleFusionGroupEventNotificationViaAlert(int eventId, int groupId, long time, String message) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaAlert(eventId, groupId, time, message, (Current)null);
   }

   public final String scheduleFusionGroupEventNotificationViaEmail(int eventId, int groupId, long time, EmailUserNotification note) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaEmail(eventId, groupId, time, note, (Current)null);
   }

   public final String scheduleFusionGroupEventNotificationViaSMS(int eventId, int groupId, long time, SMSUserNotification note) throws FusionException {
      return this.scheduleFusionGroupEventNotificationViaSMS(eventId, groupId, time, note, (Current)null);
   }

   public final void triggerJob(String jobName, String jobGroup, Map<String, String> jobDataMap) throws FusionException {
      this.triggerJob(jobName, jobGroup, jobDataMap, (Current)null);
   }

   public final void unscheduleFusionGroupEvent(int groupEventID) throws FusionException {
      this.unscheduleFusionGroupEvent(groupEventID, (Current)null);
   }

   public static DispatchStatus ___scheduleFusionGroupEventNotificationViaEmail(JobSchedulingService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int eventId = __is.readInt();
      int groupId = __is.readInt();
      long time = __is.readLong();
      EmailUserNotificationHolder note = new EmailUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         String __ret = __obj.scheduleFusionGroupEventNotificationViaEmail(eventId, groupId, time, note.value, __current);
         __os.writeString(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___scheduleFusionGroupEventNotificationViaSMS(JobSchedulingService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int eventId = __is.readInt();
      int groupId = __is.readInt();
      long time = __is.readLong();
      SMSUserNotificationHolder note = new SMSUserNotificationHolder();
      __is.readObject(note.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         String __ret = __obj.scheduleFusionGroupEventNotificationViaSMS(eventId, groupId, time, note.value, __current);
         __os.writeString(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___scheduleFusionGroupEventNotificationViaAlert(JobSchedulingService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int eventId = __is.readInt();
      int groupId = __is.readInt();
      long time = __is.readLong();
      String message = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         String __ret = __obj.scheduleFusionGroupEventNotificationViaAlert(eventId, groupId, time, message, __current);
         __os.writeString(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___scheduleFusionGroupEvent(JobSchedulingService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      GroupEventHolder event = new GroupEventHolder();
      __is.readObject(event.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         int __ret = __obj.scheduleFusionGroupEvent(event.value, __current);
         __os.writeInt(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___unscheduleFusionGroupEvent(JobSchedulingService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int groupEventID = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.unscheduleFusionGroupEvent(groupEventID, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___rescheduleFusionGroupEvent(JobSchedulingService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      GroupEventHolder event = new GroupEventHolder();
      __is.readObject(event.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.rescheduleFusionGroupEvent(event.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___triggerJob(JobSchedulingService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String jobName = __is.readString();
      String jobGroup = __is.readString();
      Map<String, String> jobDataMap = ParamMapHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.triggerJob(jobName, jobGroup, jobDataMap, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___ice_id(this, in, __current);
         case 1:
            return ___ice_ids(this, in, __current);
         case 2:
            return ___ice_isA(this, in, __current);
         case 3:
            return ___ice_ping(this, in, __current);
         case 4:
            return ___rescheduleFusionGroupEvent(this, in, __current);
         case 5:
            return ___scheduleFusionGroupEvent(this, in, __current);
         case 6:
            return ___scheduleFusionGroupEventNotificationViaAlert(this, in, __current);
         case 7:
            return ___scheduleFusionGroupEventNotificationViaEmail(this, in, __current);
         case 8:
            return ___scheduleFusionGroupEventNotificationViaSMS(this, in, __current);
         case 9:
            return ___triggerJob(this, in, __current);
         case 10:
            return ___unscheduleFusionGroupEvent(this, in, __current);
         default:
            assert false;

            throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
         }
      }
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::JobSchedulingService was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::JobSchedulingService was not generated with stream support";
      throw ex;
   }
}
