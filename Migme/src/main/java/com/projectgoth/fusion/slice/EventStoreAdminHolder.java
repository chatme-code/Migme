package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EventStoreAdminHolder {
   public EventStoreAdmin value;

   public EventStoreAdminHolder() {
   }

   public EventStoreAdminHolder(EventStoreAdmin value) {
      this.value = value;
   }

   public EventStoreAdminHolder.Patcher getPatcher() {
      return new EventStoreAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EventStoreAdminHolder.this.value = (EventStoreAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EventStoreAdmin";
      }
   }
}
