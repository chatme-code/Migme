package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class CollectedDataIceHolder {
   public CollectedDataIce value;

   public CollectedDataIceHolder() {
   }

   public CollectedDataIceHolder(CollectedDataIce value) {
      this.value = value;
   }

   public CollectedDataIceHolder.Patcher getPatcher() {
      return new CollectedDataIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            CollectedDataIceHolder.this.value = (CollectedDataIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::CollectedDataIce";
      }
   }
}
