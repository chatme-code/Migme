package com.projectgoth.fusion.fdl.enums;

import com.projectgoth.fusion.packet.ByteValueEnum;
import java.util.HashMap;

public enum ImType implements ByteValueEnum {
   FUSION((byte)1),
   MSN((byte)2),
   AIM((byte)3),
   YAHOO((byte)4),
   ICQ((byte)5),
   GTALK((byte)6),
   FACEBOOK((byte)7);

   private byte value;
   private static final HashMap<Byte, ImType> LOOKUP = new HashMap();

   private ImType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static ImType fromValue(int value) {
      return (ImType)LOOKUP.get((byte)value);
   }

   public static ImType fromValue(Byte value) {
      return (ImType)LOOKUP.get(value);
   }

   public static ImType[] fromByteArrayValues(byte[] values) {
      if (values == null) {
         return null;
      } else {
         ImType[] result = new ImType[values.length];

         for(int i = 0; i < values.length; ++i) {
            result[i] = fromValue(values[i]);
         }

         return result;
      }
   }

   static {
      ImType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ImType imType = arr$[i$];
         LOOKUP.put(imType.value, imType);
      }

   }
}
