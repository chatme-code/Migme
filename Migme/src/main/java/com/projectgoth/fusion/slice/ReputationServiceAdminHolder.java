package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ReputationServiceAdminHolder {
   public ReputationServiceAdmin value;

   public ReputationServiceAdminHolder() {
   }

   public ReputationServiceAdminHolder(ReputationServiceAdmin value) {
      this.value = value;
   }

   public ReputationServiceAdminHolder.Patcher getPatcher() {
      return new ReputationServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ReputationServiceAdminHolder.this.value = (ReputationServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ReputationServiceAdmin";
      }
   }
}
