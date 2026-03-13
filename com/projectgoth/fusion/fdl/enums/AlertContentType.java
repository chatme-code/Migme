package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum AlertContentType {
   TEXT((byte)1),
   URL((byte)2),
   URL_WITH_CONFIRMATION((byte)3);

   private byte value;
   private static final HashMap<Byte, AlertContentType> LOOKUP = new HashMap();

   private AlertContentType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static AlertContentType fromValue(int value) {
      return (AlertContentType)LOOKUP.get((byte)value);
   }

   public static AlertContentType fromValue(Byte value) {
      return (AlertContentType)LOOKUP.get(value);
   }

   static {
      AlertContentType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         AlertContentType alertContentType = arr$[i$];
         LOOKUP.put(alertContentType.value, alertContentType);
      }

   }
}
