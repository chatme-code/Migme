package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EventQueueWorkerServiceAdminHolder {
   public EventQueueWorkerServiceAdmin value;

   public EventQueueWorkerServiceAdminHolder() {
   }

   public EventQueueWorkerServiceAdminHolder(EventQueueWorkerServiceAdmin value) {
      this.value = value;
   }

   public EventQueueWorkerServiceAdminHolder.Patcher getPatcher() {
      return new EventQueueWorkerServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EventQueueWorkerServiceAdminHolder.this.value = (EventQueueWorkerServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EventQueueWorkerServiceAdmin";
      }
   }
}
