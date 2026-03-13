package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BotHunterHolder {
   public BotHunter value;

   public BotHunterHolder() {
   }

   public BotHunterHolder(BotHunter value) {
      this.value = value;
   }

   public BotHunterHolder.Patcher getPatcher() {
      return new BotHunterHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BotHunterHolder.this.value = (BotHunter)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BotHunter";
      }
   }
}
