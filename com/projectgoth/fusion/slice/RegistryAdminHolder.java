package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RegistryAdminHolder {
   public RegistryAdmin value;

   public RegistryAdminHolder() {
   }

   public RegistryAdminHolder(RegistryAdmin value) {
      this.value = value;
   }

   public RegistryAdminHolder.Patcher getPatcher() {
      return new RegistryAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RegistryAdminHolder.this.value = (RegistryAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RegistryAdmin";
      }
   }
}
