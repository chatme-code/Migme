package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class MessageSwitchboardHolder {
   public MessageSwitchboard value;

   public MessageSwitchboardHolder() {
   }

   public MessageSwitchboardHolder(MessageSwitchboard value) {
      this.value = value;
   }

   public MessageSwitchboardHolder.Patcher getPatcher() {
      return new MessageSwitchboardHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            MessageSwitchboardHolder.this.value = (MessageSwitchboard)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::MessageSwitchboard";
      }
   }
}
