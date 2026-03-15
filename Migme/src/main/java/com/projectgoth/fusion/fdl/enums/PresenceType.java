package com.projectgoth.fusion.fdl.enums;

import com.projectgoth.fusion.packet.ByteValueEnum;
import java.util.HashMap;

public enum PresenceType implements ByteValueEnum {
   AVAILABLE((byte)1),
   ROAMING((byte)2),
   BUSY((byte)3),
   AWAY((byte)4),
   OFFLINE((byte)99);

   private byte value;
   private static final HashMap<Byte, PresenceType> LOOKUP = new HashMap();

   private PresenceType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static PresenceType fromValue(int value) {
      return (PresenceType)LOOKUP.get((byte)value);
   }

   public static PresenceType fromValue(Byte value) {
      return (PresenceType)LOOKUP.get(value);
   }

   public static PresenceType[] fromByteArrayValues(byte[] values) {
      if (values == null) {
         return null;
      } else {
         PresenceType[] result = new PresenceType[values.length];

         for(int i = 0; i < values.length; ++i) {
            result[i] = fromValue(values[i]);
         }

         return result;
      }
   }

   public boolean isOnline() {
      switch(this) {
      case AVAILABLE:
      case ROAMING:
      case BUSY:
      case AWAY:
         return true;
      default:
         return false;
      }
   }

   static {
      PresenceType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         PresenceType presenceType = arr$[i$];
         LOOKUP.put(presenceType.value, presenceType);
      }

   }
}
