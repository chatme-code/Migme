package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EventStoreStatsHolder {
   public EventStoreStats value;

   public EventStoreStatsHolder() {
   }

   public EventStoreStatsHolder(EventStoreStats value) {
      this.value = value;
   }

   public EventStoreStatsHolder.Patcher getPatcher() {
      return new EventStoreStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EventStoreStatsHolder.this.value = (EventStoreStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EventStoreStats";
      }
   }
}
