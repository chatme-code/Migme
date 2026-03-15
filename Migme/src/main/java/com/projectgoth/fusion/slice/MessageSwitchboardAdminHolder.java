package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class MessageSwitchboardAdminHolder {
   public MessageSwitchboardAdmin value;

   public MessageSwitchboardAdminHolder() {
   }

   public MessageSwitchboardAdminHolder(MessageSwitchboardAdmin value) {
      this.value = value;
   }

   public MessageSwitchboardAdminHolder.Patcher getPatcher() {
      return new MessageSwitchboardAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            MessageSwitchboardAdminHolder.this.value = (MessageSwitchboardAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::MessageSwitchboardAdmin";
      }
   }
}
