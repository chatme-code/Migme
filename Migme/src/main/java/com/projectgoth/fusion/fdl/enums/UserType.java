package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum UserType {
   STANDARD((byte)1),
   MERCHANT((byte)2),
   TOP_MERCHANT((byte)3),
   PREPAID_CARD((byte)4);

   private byte value;
   private static final HashMap<Byte, UserType> LOOKUP = new HashMap();

   private UserType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static UserType fromValue(int value) {
      return (UserType)LOOKUP.get((byte)value);
   }

   public static UserType fromValue(Byte value) {
      return (UserType)LOOKUP.get(value);
   }

   static {
      UserType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         UserType userType = arr$[i$];
         LOOKUP.put(userType.value, userType);
      }

   }
}
