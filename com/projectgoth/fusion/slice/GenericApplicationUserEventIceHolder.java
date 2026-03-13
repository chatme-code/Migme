package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GenericApplicationUserEventIceHolder {
   public GenericApplicationUserEventIce value;

   public GenericApplicationUserEventIceHolder() {
   }

   public GenericApplicationUserEventIceHolder(GenericApplicationUserEventIce value) {
      this.value = value;
   }

   public GenericApplicationUserEventIceHolder.Patcher getPatcher() {
      return new GenericApplicationUserEventIceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GenericApplicationUserEventIceHolder.this.value = (GenericApplicationUserEventIce)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GenericApplicationUserEventIce";
      }
   }
}
