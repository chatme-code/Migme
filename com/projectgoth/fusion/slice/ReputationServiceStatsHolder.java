package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ReputationServiceStatsHolder {
   public ReputationServiceStats value;

   public ReputationServiceStatsHolder() {
   }

   public ReputationServiceStatsHolder(ReputationServiceStats value) {
      this.value = value;
   }

   public ReputationServiceStatsHolder.Patcher getPatcher() {
      return new ReputationServiceStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ReputationServiceStatsHolder.this.value = (ReputationServiceStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ReputationServiceStats";
      }
   }
}
