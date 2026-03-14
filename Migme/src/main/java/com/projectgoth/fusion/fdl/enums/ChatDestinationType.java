package com.projectgoth.fusion.fdl.enums;

import com.projectgoth.fusion.packet.ByteValueEnum;
import java.util.HashMap;

public enum ChatDestinationType implements ByteValueEnum {
   PRIVATE((byte)1),
   GROUP_CHAT((byte)2),
   CHATROOM((byte)3),
   DISTRIBUTION_LIST((byte)4);

   private byte value;
   private static final HashMap<Byte, ChatDestinationType> LOOKUP = new HashMap();

   private ChatDestinationType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static ChatDestinationType fromValue(int value) {
      return (ChatDestinationType)LOOKUP.get((byte)value);
   }

   public static ChatDestinationType fromValue(Byte value) {
      return (ChatDestinationType)LOOKUP.get(value);
   }

   public static ChatDestinationType[] fromByteArrayValues(byte[] values) {
      if (values == null) {
         return null;
      } else {
         ChatDestinationType[] result = new ChatDestinationType[values.length];

         for(int i = 0; i < values.length; ++i) {
            result[i] = fromValue(values[i]);
         }

         return result;
      }
   }

   static {
      ChatDestinationType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatDestinationType chatDestinationType = arr$[i$];
         LOOKUP.put(chatDestinationType.value, chatDestinationType);
      }

   }
}
