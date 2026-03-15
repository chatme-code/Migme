package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class GatewayAdminHolder {
   public GatewayAdmin value;

   public GatewayAdminHolder() {
   }

   public GatewayAdminHolder(GatewayAdmin value) {
      this.value = value;
   }

   public GatewayAdminHolder.Patcher getPatcher() {
      return new GatewayAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            GatewayAdminHolder.this.value = (GatewayAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::GatewayAdmin";
      }
   }
}
