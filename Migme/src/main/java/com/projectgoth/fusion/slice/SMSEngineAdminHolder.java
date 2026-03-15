package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class SMSEngineAdminHolder {
   public SMSEngineAdmin value;

   public SMSEngineAdminHolder() {
   }

   public SMSEngineAdminHolder(SMSEngineAdmin value) {
      this.value = value;
   }

   public SMSEngineAdminHolder.Patcher getPatcher() {
      return new SMSEngineAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            SMSEngineAdminHolder.this.value = (SMSEngineAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::SMSEngineAdmin";
      }
   }
}
