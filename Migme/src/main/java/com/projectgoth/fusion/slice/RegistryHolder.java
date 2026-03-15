package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RegistryHolder {
   public Registry value;

   public RegistryHolder() {
   }

   public RegistryHolder(Registry value) {
      this.value = value;
   }

   public RegistryHolder.Patcher getPatcher() {
      return new RegistryHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RegistryHolder.this.value = (Registry)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::Registry";
      }
   }
}
