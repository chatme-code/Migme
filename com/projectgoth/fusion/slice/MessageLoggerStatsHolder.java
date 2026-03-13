package com.projectgoth.fusion.slice;

import Ice.Object;
import IceInternal.Ex;

public final class MessageLoggerStatsHolder {
   public MessageLoggerStats value;

   public MessageLoggerStatsHolder() {
   }

   public MessageLoggerStatsHolder(MessageLoggerStats value) {
      this.value = value;
   }

   public MessageLoggerStatsHolder.Patcher getPatcher() {
      return new MessageLoggerStatsHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            MessageLoggerStatsHolder.this.value = (MessageLoggerStats)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::MessageLoggerStats";
      }
   }
}
