package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BotServiceStatsHolder {
   public BotServiceStats value;

   public BotServiceStatsHolder() {
   }

   public BotServiceStatsHolder(BotServiceStats value) {
      this.value = value;
   }

   public BotServiceStatsHolder.Patcher getPatcher() {
      return new BotServiceStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BotServiceStatsHolder.this.value = (BotServiceStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BotServiceStats";
      }
   }
}
