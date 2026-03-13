package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class ShortTextStatusUserEventIceHolder {
   public ShortTextStatusUserEventIce value;

   public ShortTextStatusUserEventIceHolder() {
   }

   public ShortTextStatusUserEventIceHolder(ShortTextStatusUserEventIce value) {
      this.value = value;
   }

   public ShortTextStatusUserEventIceHolder.Patcher getPatcher() {
      return new ShortTextStatusUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            ShortTextStatusUserEventIceHolder.this.value = (ShortTextStatusUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::ShortTextStatusUserEventIce";
      }
   }
}
