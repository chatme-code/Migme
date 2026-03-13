package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class RegistryStatsHolder {
   public RegistryStats value;

   public RegistryStatsHolder() {
   }

   public RegistryStatsHolder(RegistryStats value) {
      this.value = value;
   }

   public RegistryStatsHolder.Patcher getPatcher() {
      return new RegistryStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            RegistryStatsHolder.this.value = (RegistryStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::RegistryStats";
      }
   }
}
