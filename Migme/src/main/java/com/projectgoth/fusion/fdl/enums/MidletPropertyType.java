package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum MidletPropertyType {
   RMS((byte)1),
   COOKIE((byte)2);

   private byte value;
   private static final HashMap<Byte, MidletPropertyType> LOOKUP = new HashMap();

   private MidletPropertyType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static MidletPropertyType fromValue(int value) {
      return (MidletPropertyType)LOOKUP.get((byte)value);
   }

   public static MidletPropertyType fromValue(Byte value) {
      return (MidletPropertyType)LOOKUP.get(value);
   }

   static {
      MidletPropertyType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MidletPropertyType midletPropertyType = arr$[i$];
         LOOKUP.put(midletPropertyType.value, midletPropertyType);
      }

   }
}
