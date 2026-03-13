package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class SMSUserNotificationHolder {
   public SMSUserNotification value;

   public SMSUserNotificationHolder() {
   }

   public SMSUserNotificationHolder(SMSUserNotification value) {
      this.value = value;
   }

   public SMSUserNotificationHolder.Patcher getPatcher() {
      return new SMSUserNotificationHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            SMSUserNotificationHolder.this.value = (SMSUserNotification)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::SMSUserNotification";
      }
   }
}
