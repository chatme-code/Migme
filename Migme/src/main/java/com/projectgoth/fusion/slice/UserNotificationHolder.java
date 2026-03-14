package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class UserNotificationHolder {
   public UserNotification value;

   public UserNotificationHolder() {
   }

   public UserNotificationHolder(UserNotification value) {
      this.value = value;
   }

   public UserNotificationHolder.Patcher getPatcher() {
      return new UserNotificationHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            UserNotificationHolder.this.value = (UserNotification)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::UserNotification";
      }
   }
}
