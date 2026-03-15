package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class UserNotificationServiceStatsHolder {
   public UserNotificationServiceStats value;

   public UserNotificationServiceStatsHolder() {
   }

   public UserNotificationServiceStatsHolder(UserNotificationServiceStats value) {
      this.value = value;
   }

   public UserNotificationServiceStatsHolder.Patcher getPatcher() {
      return new UserNotificationServiceStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            UserNotificationServiceStatsHolder.this.value = (UserNotificationServiceStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::UserNotificationServiceStats";
      }
   }
}
