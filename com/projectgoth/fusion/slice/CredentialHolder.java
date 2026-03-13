package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class CredentialHolder {
   public Credential value;

   public CredentialHolder() {
   }

   public CredentialHolder(Credential value) {
      this.value = value;
   }

   public CredentialHolder.Patcher getPatcher() {
      return new CredentialHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            CredentialHolder.this.value = (Credential)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::Credential";
      }
   }
}
