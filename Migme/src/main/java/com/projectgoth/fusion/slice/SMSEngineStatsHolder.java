package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class SMSEngineStatsHolder {
   public SMSEngineStats value;

   public SMSEngineStatsHolder() {
   }

   public SMSEngineStatsHolder(SMSEngineStats value) {
      this.value = value;
   }

   public SMSEngineStatsHolder.Patcher getPatcher() {
      return new SMSEngineStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            SMSEngineStatsHolder.this.value = (SMSEngineStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::SMSEngineStats";
      }
   }
}
