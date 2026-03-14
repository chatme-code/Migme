package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EventStoreHolder {
   public EventStore value;

   public EventStoreHolder() {
   }

   public EventStoreHolder(EventStore value) {
      this.value = value;
   }

   public EventStoreHolder.Patcher getPatcher() {
      return new EventStoreHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EventStoreHolder.this.value = (EventStore)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EventStore";
      }
   }
}
