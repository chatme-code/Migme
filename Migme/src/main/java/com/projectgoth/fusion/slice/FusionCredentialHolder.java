package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class FusionCredentialHolder {
   public FusionCredential value;

   public FusionCredentialHolder() {
   }

   public FusionCredentialHolder(FusionCredential value) {
      this.value = value;
   }

   public FusionCredentialHolder.Patcher getPatcher() {
      return new FusionCredentialHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            FusionCredentialHolder.this.value = (FusionCredential)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::FusionCredential";
      }
   }
}
