package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class SMSSenderHolder {
   public SMSSender value;

   public SMSSenderHolder() {
   }

   public SMSSenderHolder(SMSSender value) {
      this.value = value;
   }

   public SMSSenderHolder.Patcher getPatcher() {
      return new SMSSenderHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            SMSSenderHolder.this.value = (SMSSender)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::SMSSender";
      }
   }
}
