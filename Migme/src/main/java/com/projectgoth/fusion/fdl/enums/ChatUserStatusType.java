package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum ChatUserStatusType {
   JOINED((byte)1),
   LEFT((byte)2);

   private byte value;
   private static final HashMap<Byte, ChatUserStatusType> LOOKUP = new HashMap();

   private ChatUserStatusType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static ChatUserStatusType fromValue(int value) {
      return (ChatUserStatusType)LOOKUP.get((byte)value);
   }

   public static ChatUserStatusType fromValue(Byte value) {
      return (ChatUserStatusType)LOOKUP.get(value);
   }

   static {
      ChatUserStatusType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatUserStatusType chatUserStatusType = arr$[i$];
         LOOKUP.put(chatUserStatusType.value, chatUserStatusType);
      }

   }
}
