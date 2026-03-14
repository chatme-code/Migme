package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class MessageSwitchboardStatsHolder {
   public MessageSwitchboardStats value;

   public MessageSwitchboardStatsHolder() {
   }

   public MessageSwitchboardStatsHolder(MessageSwitchboardStats value) {
      this.value = value;
   }

   public MessageSwitchboardStatsHolder.Patcher getPatcher() {
      return new MessageSwitchboardStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            MessageSwitchboardStatsHolder.this.value = (MessageSwitchboardStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::MessageSwitchboardStats";
      }
   }
}
