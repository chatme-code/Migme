package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class AuthenticationServiceHolder {
   public AuthenticationService value;

   public AuthenticationServiceHolder() {
   }

   public AuthenticationServiceHolder(AuthenticationService value) {
      this.value = value;
   }

   public AuthenticationServiceHolder.Patcher getPatcher() {
      return new AuthenticationServiceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            AuthenticationServiceHolder.this.value = (AuthenticationService)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::AuthenticationService";
      }
   }
}
