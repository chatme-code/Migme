package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BlueLabelServiceAdminHolder {
   public BlueLabelServiceAdmin value;

   public BlueLabelServiceAdminHolder() {
   }

   public BlueLabelServiceAdminHolder(BlueLabelServiceAdmin value) {
      this.value = value;
   }

   public BlueLabelServiceAdminHolder.Patcher getPatcher() {
      return new BlueLabelServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BlueLabelServiceAdminHolder.this.value = (BlueLabelServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BlueLabelServiceAdmin";
      }
   }
}
