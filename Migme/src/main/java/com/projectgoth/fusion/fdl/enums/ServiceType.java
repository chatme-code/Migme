package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum ServiceType {
   X_TXT((byte)1),
   X_TXT_ASIA((byte)2);

   private byte value;
   private static final HashMap<Byte, ServiceType> LOOKUP = new HashMap();

   private ServiceType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static ServiceType fromValue(int value) {
      return (ServiceType)LOOKUP.get((byte)value);
   }

   public static ServiceType fromValue(Byte value) {
      return (ServiceType)LOOKUP.get(value);
   }

   static {
      ServiceType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ServiceType serviceType = arr$[i$];
         LOOKUP.put(serviceType.value, serviceType);
      }

   }
}
