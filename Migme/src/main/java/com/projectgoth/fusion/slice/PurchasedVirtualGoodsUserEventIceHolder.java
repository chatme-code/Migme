package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class PurchasedVirtualGoodsUserEventIceHolder {
   public PurchasedVirtualGoodsUserEventIce value;

   public PurchasedVirtualGoodsUserEventIceHolder() {
   }

   public PurchasedVirtualGoodsUserEventIceHolder(PurchasedVirtualGoodsUserEventIce value) {
      this.value = value;
   }

   public PurchasedVirtualGoodsUserEventIceHolder.Patcher getPatcher() {
      return new PurchasedVirtualGoodsUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            PurchasedVirtualGoodsUserEventIceHolder.this.value = (PurchasedVirtualGoodsUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::PurchasedVirtualGoodsUserEventIce";
      }
   }
}
