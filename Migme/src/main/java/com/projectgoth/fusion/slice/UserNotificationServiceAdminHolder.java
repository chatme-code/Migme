package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class UserNotificationServiceAdminHolder {
   public UserNotificationServiceAdmin value;

   public UserNotificationServiceAdminHolder() {
   }

   public UserNotificationServiceAdminHolder(UserNotificationServiceAdmin value) {
      this.value = value;
   }

   public UserNotificationServiceAdminHolder.Patcher getPatcher() {
      return new UserNotificationServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            UserNotificationServiceAdminHolder.this.value = (UserNotificationServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::UserNotificationServiceAdmin";
      }
   }
}
