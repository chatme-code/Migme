package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class BotChannelHolder {
   public BotChannel value;

   public BotChannelHolder() {
   }

   public BotChannelHolder(BotChannel value) {
      this.value = value;
   }

   public BotChannelHolder.Patcher getPatcher() {
      return new BotChannelHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            BotChannelHolder.this.value = (BotChannel)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::BotChannel";
      }
   }
}
