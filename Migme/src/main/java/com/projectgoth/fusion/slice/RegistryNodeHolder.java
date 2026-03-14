package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RegistryNodeHolder {
   public RegistryNode value;

   public RegistryNodeHolder() {
   }

   public RegistryNodeHolder(RegistryNode value) {
      this.value = value;
   }

   public RegistryNodeHolder.Patcher getPatcher() {
      return new RegistryNodeHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RegistryNodeHolder.this.value = (RegistryNode)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RegistryNode";
      }
   }
}
