package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum ImDetailType {
   UNREGISTERED((byte)0),
   DISCONNECTED((byte)1),
   CONNECTED((byte)2);

   private byte value;
   private static final HashMap<Byte, ImDetailType> LOOKUP = new HashMap();

   private ImDetailType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static ImDetailType fromValue(int value) {
      return (ImDetailType)LOOKUP.get((byte)value);
   }

   public static ImDetailType fromValue(Byte value) {
      return (ImDetailType)LOOKUP.get(value);
   }

   static {
      ImDetailType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ImDetailType imDetailType = arr$[i$];
         LOOKUP.put(imDetailType.value, imDetailType);
      }

   }
}
