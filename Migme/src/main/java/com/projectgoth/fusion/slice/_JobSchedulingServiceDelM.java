package com.projectgoth.fusion.slice;

import Ice.LocalException;
import Ice.OperationMode;
import Ice.UnknownUserException;
import Ice.UserException;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.Outgoing;
import java.util.Map;

public final class _JobSchedulingServiceDelM extends _ObjectDelM implements _JobSchedulingServiceDel {
   public void rescheduleFusionGroupEvent(GroupEvent event, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("rescheduleFusionGroupEvent", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(event);
            __os.writePendingObjects();
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public int scheduleFusionGroupEvent(GroupEvent event, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("scheduleFusionGroupEvent", OperationMode.Normal, __ctx);

      int var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(event);
            __os.writePendingObjects();
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public String scheduleFusionGroupEventNotificationViaAlert(int eventId, int groupId, long time, String message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("scheduleFusionGroupEventNotificationViaAlert", OperationMode.Normal, __ctx);

      String var11;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(eventId);
            __os.writeInt(groupId);
            __os.writeLong(time);
            __os.writeString(message);
         } catch (LocalException var21) {
            __og.abort(var21);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var19) {
                  throw var19;
               } catch (UserException var20) {
                  throw new UnknownUserException(var20.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var11 = __ret;
         } catch (LocalException var22) {
            throw new LocalExceptionWrapper(var22, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var11;
   }

   public String scheduleFusionGroupEventNotificationViaEmail(int eventId, int groupId, long time, EmailUserNotification note, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("scheduleFusionGroupEventNotificationViaEmail", OperationMode.Normal, __ctx);

      String var11;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(eventId);
            __os.writeInt(groupId);
            __os.writeLong(time);
            __os.writeObject(note);
            __os.writePendingObjects();
         } catch (LocalException var21) {
            __og.abort(var21);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var19) {
                  throw var19;
               } catch (UserException var20) {
                  throw new UnknownUserException(var20.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var11 = __ret;
         } catch (LocalException var22) {
            throw new LocalExceptionWrapper(var22, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var11;
   }

   public String scheduleFusionGroupEventNotificationViaSMS(int eventId, int groupId, long time, SMSUserNotification note, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("scheduleFusionGroupEventNotificationViaSMS", OperationMode.Normal, __ctx);

      String var11;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(eventId);
            __os.writeInt(groupId);
            __os.writeLong(time);
            __os.writeObject(note);
            __os.writePendingObjects();
         } catch (LocalException var21) {
            __og.abort(var21);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var19) {
                  throw var19;
               } catch (UserException var20) {
                  throw new UnknownUserException(var20.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var11 = __ret;
         } catch (LocalException var22) {
            throw new LocalExceptionWrapper(var22, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var11;
   }

   public void triggerJob(String jobName, String jobGroup, Map<String, String> jobDataMap, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("triggerJob", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(jobName);
            __os.writeString(jobGroup);
            ParamMapHelper.write(__os, jobDataMap);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void unscheduleFusionGroupEvent(int groupEventID, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("unscheduleFusionGroupEvent", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(groupEventID);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }
}
