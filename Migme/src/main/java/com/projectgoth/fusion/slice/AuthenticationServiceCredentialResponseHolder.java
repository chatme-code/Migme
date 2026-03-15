package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class AuthenticationServiceCredentialResponseHolder {
   public AuthenticationServiceCredentialResponse value;

   public AuthenticationServiceCredentialResponseHolder() {
   }

   public AuthenticationServiceCredentialResponseHolder(AuthenticationServiceCredentialResponse value) {
      this.value = value;
   }

   public AuthenticationServiceCredentialResponseHolder.Patcher getPatcher() {
      return new AuthenticationServiceCredentialResponseHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            AuthenticationServiceCredentialResponseHolder.this.value = (AuthenticationServiceCredentialResponse)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::AuthenticationServiceCredentialResponse";
      }
   }
}
