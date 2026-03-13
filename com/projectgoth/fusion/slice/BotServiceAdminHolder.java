package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BotServiceAdminHolder {
   public BotServiceAdmin value;

   public BotServiceAdminHolder() {
   }

   public BotServiceAdminHolder(BotServiceAdmin value) {
      this.value = value;
   }

   public BotServiceAdminHolder.Patcher getPatcher() {
      return new BotServiceAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BotServiceAdminHolder.this.value = (BotServiceAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BotServiceAdmin";
      }
   }
}
