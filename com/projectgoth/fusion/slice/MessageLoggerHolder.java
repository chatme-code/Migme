package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class MessageLoggerHolder {
   public MessageLogger value;

   public MessageLoggerHolder() {
   }

   public MessageLoggerHolder(MessageLogger value) {
      this.value = value;
   }

   public MessageLoggerHolder.Patcher getPatcher() {
      return new MessageLoggerHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            MessageLoggerHolder.this.value = (MessageLogger)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::MessageLogger";
      }
   }
}
