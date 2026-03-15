package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EmailUserNotificationHolder {
   public EmailUserNotification value;

   public EmailUserNotificationHolder() {
   }

   public EmailUserNotificationHolder(EmailUserNotification value) {
      this.value = value;
   }

   public EmailUserNotificationHolder.Patcher getPatcher() {
      return new EmailUserNotificationHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EmailUserNotificationHolder.this.value = (EmailUserNotification)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EmailUserNotification";
      }
   }
}
