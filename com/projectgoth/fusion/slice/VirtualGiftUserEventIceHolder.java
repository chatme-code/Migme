package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class VirtualGiftUserEventIceHolder {
   public VirtualGiftUserEventIce value;

   public VirtualGiftUserEventIceHolder() {
   }

   public VirtualGiftUserEventIceHolder(VirtualGiftUserEventIce value) {
      this.value = value;
   }

   public VirtualGiftUserEventIceHolder.Patcher getPatcher() {
      return new VirtualGiftUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            VirtualGiftUserEventIceHolder.this.value = (VirtualGiftUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::VirtualGiftUserEventIce";
      }
   }
}
