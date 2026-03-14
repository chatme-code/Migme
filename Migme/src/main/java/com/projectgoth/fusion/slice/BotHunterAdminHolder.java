package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BotHunterAdminHolder {
   public BotHunterAdmin value;

   public BotHunterAdminHolder() {
   }

   public BotHunterAdminHolder(BotHunterAdmin value) {
      this.value = value;
   }

   public BotHunterAdminHolder.Patcher getPatcher() {
      return new BotHunterAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BotHunterAdminHolder.this.value = (BotHunterAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BotHunterAdmin";
      }
   }
}
