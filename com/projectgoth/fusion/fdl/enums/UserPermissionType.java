package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum UserPermissionType {
   ALLOW((byte)1),
   BLOCK((byte)2);

   private byte value;
   private static final HashMap<Byte, UserPermissionType> LOOKUP = new HashMap();

   private UserPermissionType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static UserPermissionType fromValue(int value) {
      return (UserPermissionType)LOOKUP.get((byte)value);
   }

   public static UserPermissionType fromValue(Byte value) {
      return (UserPermissionType)LOOKUP.get(value);
   }

   static {
      UserPermissionType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         UserPermissionType userPermissionType = arr$[i$];
         LOOKUP.put(userPermissionType.value, userPermissionType);
      }

   }
}
