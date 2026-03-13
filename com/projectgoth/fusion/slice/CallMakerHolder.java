package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class CallMakerHolder {
   public CallMaker value;

   public CallMakerHolder() {
   }

   public CallMakerHolder(CallMaker value) {
      this.value = value;
   }

   public CallMakerHolder.Patcher getPatcher() {
      return new CallMakerHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            CallMakerHolder.this.value = (CallMaker)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::CallMaker";
      }
   }
}
