package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BotHunterStatsHolder {
   public BotHunterStats value;

   public BotHunterStatsHolder() {
   }

   public BotHunterStatsHolder(BotHunterStats value) {
      this.value = value;
   }

   public BotHunterStatsHolder.Patcher getPatcher() {
      return new BotHunterStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BotHunterStatsHolder.this.value = (BotHunterStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BotHunterStats";
      }
   }
}
