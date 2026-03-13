package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BlueLabelServiceStatsHolder {
   public BlueLabelServiceStats value;

   public BlueLabelServiceStatsHolder() {
   }

   public BlueLabelServiceStatsHolder(BlueLabelServiceStats value) {
      this.value = value;
   }

   public BlueLabelServiceStatsHolder.Patcher getPatcher() {
      return new BlueLabelServiceStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BlueLabelServiceStatsHolder.this.value = (BlueLabelServiceStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BlueLabelServiceStats";
      }
   }
}
