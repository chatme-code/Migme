package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BlueLabelServiceHolder {
   public BlueLabelService value;

   public BlueLabelServiceHolder() {
   }

   public BlueLabelServiceHolder(BlueLabelService value) {
      this.value = value;
   }

   public BlueLabelServiceHolder.Patcher getPatcher() {
      return new BlueLabelServiceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BlueLabelServiceHolder.this.value = (BlueLabelService)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BlueLabelService";
      }
   }
}
