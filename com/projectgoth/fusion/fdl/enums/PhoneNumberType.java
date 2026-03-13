package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum PhoneNumberType {
   MOBILE((byte)1),
   HOME((byte)2),
   OFFICE((byte)3);

   private byte value;
   private static final HashMap<Byte, PhoneNumberType> LOOKUP = new HashMap();

   private PhoneNumberType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static PhoneNumberType fromValue(int value) {
      return (PhoneNumberType)LOOKUP.get((byte)value);
   }

   public static PhoneNumberType fromValue(Byte value) {
      return (PhoneNumberType)LOOKUP.get(value);
   }

   static {
      PhoneNumberType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         PhoneNumberType phoneNumberType = arr$[i$];
         LOOKUP.put(phoneNumberType.value, phoneNumberType);
      }

   }
}
