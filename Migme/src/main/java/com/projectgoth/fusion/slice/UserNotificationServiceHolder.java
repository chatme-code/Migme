package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class UserNotificationServiceHolder {
   public UserNotificationService value;

   public UserNotificationServiceHolder() {
   }

   public UserNotificationServiceHolder(UserNotificationService value) {
      this.value = value;
   }

   public UserNotificationServiceHolder.Patcher getPatcher() {
      return new UserNotificationServiceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            UserNotificationServiceHolder.this.value = (UserNotificationService)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::UserNotificationService";
      }
   }
}
