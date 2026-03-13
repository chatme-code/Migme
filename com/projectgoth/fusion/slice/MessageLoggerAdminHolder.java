package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class MessageLoggerAdminHolder {
   public MessageLoggerAdmin value;

   public MessageLoggerAdminHolder() {
   }

   public MessageLoggerAdminHolder(MessageLoggerAdmin value) {
      this.value = value;
   }

   public MessageLoggerAdminHolder.Patcher getPatcher() {
      return new MessageLoggerAdminHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            MessageLoggerAdminHolder.this.value = (MessageLoggerAdmin)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::MessageLoggerAdmin";
      }
   }
}
