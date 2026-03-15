package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum MessageType {
   FUSION((byte)1),
   SMS((byte)2),
   EMAIL((byte)3),
   MSN((byte)4),
   AIM((byte)5),
   YAHOO((byte)6),
   FACEBOOK((byte)7),
   GTALK((byte)8),
   OFFLINE_MESSAGE((byte)9),
   SERVER_INFO((byte)98);

   private byte value;
   private static final HashMap<Byte, MessageType> LOOKUP = new HashMap();

   private MessageType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static MessageType fromValue(int value) {
      return (MessageType)LOOKUP.get((byte)value);
   }

   public static MessageType fromValue(Byte value) {
      return (MessageType)LOOKUP.get(value);
   }

   static {
      MessageType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MessageType messageType = arr$[i$];
         LOOKUP.put(messageType.value, messageType);
      }

   }
}
