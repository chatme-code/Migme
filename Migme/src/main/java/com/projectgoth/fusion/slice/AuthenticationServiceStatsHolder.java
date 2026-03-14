package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class AuthenticationServiceStatsHolder {
   public AuthenticationServiceStats value;

   public AuthenticationServiceStatsHolder() {
   }

   public AuthenticationServiceStatsHolder(AuthenticationServiceStats value) {
      this.value = value;
   }

   public AuthenticationServiceStatsHolder.Patcher getPatcher() {
      return new AuthenticationServiceStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            AuthenticationServiceStatsHolder.this.value = (AuthenticationServiceStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::AuthenticationServiceStats";
      }
   }
}
