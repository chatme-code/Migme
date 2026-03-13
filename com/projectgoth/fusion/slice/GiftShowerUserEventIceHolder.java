package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GiftShowerUserEventIceHolder {
   public GiftShowerUserEventIce value;

   public GiftShowerUserEventIceHolder() {
   }

   public GiftShowerUserEventIceHolder(GiftShowerUserEventIce value) {
      this.value = value;
   }

   public GiftShowerUserEventIceHolder.Patcher getPatcher() {
      return new GiftShowerUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GiftShowerUserEventIceHolder.this.value = (GiftShowerUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GiftShowerUserEventIce";
      }
   }
}
