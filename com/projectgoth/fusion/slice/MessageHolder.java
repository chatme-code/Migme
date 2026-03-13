package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class MessageHolder {
   public Message value;

   public MessageHolder() {
   }

   public MessageHolder(Message value) {
      this.value = value;
   }

   public MessageHolder.Patcher getPatcher() {
      return new MessageHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            MessageHolder.this.value = (Message)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::Message";
      }
   }
}
