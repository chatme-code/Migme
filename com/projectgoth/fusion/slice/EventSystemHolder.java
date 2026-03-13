package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EventSystemHolder {
   public EventSystem value;

   public EventSystemHolder() {
   }

   public EventSystemHolder(EventSystem value) {
      this.value = value;
   }

   public EventSystemHolder.Patcher getPatcher() {
      return new EventSystemHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EventSystemHolder.this.value = (EventSystem)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EventSystem";
      }
   }
}
