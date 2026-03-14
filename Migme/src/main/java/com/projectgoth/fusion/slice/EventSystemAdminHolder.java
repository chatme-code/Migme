package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class EventSystemAdminHolder {
   public EventSystemAdmin value;

   public EventSystemAdminHolder() {
   }

   public EventSystemAdminHolder(EventSystemAdmin value) {
      this.value = value;
   }

   public EventSystemAdminHolder.Patcher getPatcher() {
      return new EventSystemAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            EventSystemAdminHolder.this.value = (EventSystemAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::EventSystemAdmin";
      }
   }
}
