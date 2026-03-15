package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BotServiceHolder {
   public BotService value;

   public BotServiceHolder() {
   }

   public BotServiceHolder(BotService value) {
      this.value = value;
   }

   public BotServiceHolder.Patcher getPatcher() {
      return new BotServiceHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BotServiceHolder.this.value = (BotService)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BotService";
      }
   }
}
