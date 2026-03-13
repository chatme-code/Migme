package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EventQueueWorkerHolder {
   public EventQueueWorker value;

   public EventQueueWorkerHolder() {
   }

   public EventQueueWorkerHolder(EventQueueWorker value) {
      this.value = value;
   }

   public EventQueueWorkerHolder.Patcher getPatcher() {
      return new EventQueueWorkerHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EventQueueWorkerHolder.this.value = (EventQueueWorker)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EventQueueWorker";
      }
   }
}
