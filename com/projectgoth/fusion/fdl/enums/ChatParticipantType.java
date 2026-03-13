package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum ChatParticipantType {
   NORMAL((byte)0),
   ADMINISTRATOR((byte)1),
   MUTED((byte)2);

   private byte value;
   private static final HashMap<Byte, ChatParticipantType> LOOKUP = new HashMap();

   private ChatParticipantType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static ChatParticipantType fromValue(int value) {
      return (ChatParticipantType)LOOKUP.get((byte)value);
   }

   public static ChatParticipantType fromValue(Byte value) {
      return (ChatParticipantType)LOOKUP.get(value);
   }

   static {
      ChatParticipantType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatParticipantType chatParticipantType = arr$[i$];
         LOOKUP.put(chatParticipantType.value, chatParticipantType);
      }

   }
}
