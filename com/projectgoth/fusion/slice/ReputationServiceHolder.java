package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ReputationServiceHolder {
   public ReputationService value;

   public ReputationServiceHolder() {
   }

   public ReputationServiceHolder(ReputationService value) {
      this.value = value;
   }

   public ReputationServiceHolder.Patcher getPatcher() {
      return new ReputationServiceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ReputationServiceHolder.this.value = (ReputationService)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ReputationService";
      }
   }
}
