package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GatewayStatsHolder {
   public GatewayStats value;

   public GatewayStatsHolder() {
   }

   public GatewayStatsHolder(GatewayStats value) {
      this.value = value;
   }

   public GatewayStatsHolder.Patcher getPatcher() {
      return new GatewayStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GatewayStatsHolder.this.value = (GatewayStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GatewayStats";
      }
   }
}
