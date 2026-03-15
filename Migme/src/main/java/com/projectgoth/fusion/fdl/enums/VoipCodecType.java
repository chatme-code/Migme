package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum VoipCodecType {
   ULAW((byte)1),
   ALAW((byte)2),
   GSM((byte)3),
   G729((byte)4);

   private byte value;
   private static final HashMap<Byte, VoipCodecType> LOOKUP = new HashMap();

   private VoipCodecType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static VoipCodecType fromValue(int value) {
      return (VoipCodecType)LOOKUP.get((byte)value);
   }

   public static VoipCodecType fromValue(Byte value) {
      return (VoipCodecType)LOOKUP.get(value);
   }

   static {
      VoipCodecType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         VoipCodecType voipCodecType = arr$[i$];
         LOOKUP.put(voipCodecType.value, voipCodecType);
      }

   }
}
