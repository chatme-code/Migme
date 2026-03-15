package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class AuthenticationServiceAdminHolder {
   public AuthenticationServiceAdmin value;

   public AuthenticationServiceAdminHolder() {
   }

   public AuthenticationServiceAdminHolder(AuthenticationServiceAdmin value) {
      this.value = value;
   }

   public AuthenticationServiceAdminHolder.Patcher getPatcher() {
      return new AuthenticationServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            AuthenticationServiceAdminHolder.this.value = (AuthenticationServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::AuthenticationServiceAdmin";
      }
   }
}
