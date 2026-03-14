package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum ClientType {
   MIDP1((byte)1),
   MIDP2((byte)2),
   DESKTOP((byte)3),
   SYMBIAN((byte)4),
   AJAX1((byte)5),
   TOOLBAR((byte)6),
   WINDOWS_MOBILE((byte)7),
   ANDROID((byte)8),
   WAP((byte)9),
   MIGBO((byte)10),
   VAS((byte)11),
   MERCHANT_CENTER((byte)12),
   BLACKBERRY((byte)13),
   BLAAST((byte)14),
   MRE((byte)15),
   AJAX2((byte)16),
   IOS((byte)17);

   private byte value;
   private static final HashMap<Byte, ClientType> LOOKUP = new HashMap();

   private ClientType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static ClientType fromValue(int value) {
      return (ClientType)LOOKUP.get((byte)value);
   }

   public static ClientType fromValue(Byte value) {
      return (ClientType)LOOKUP.get(value);
   }

   public static final boolean isAjax(ClientType deviceType) {
      return deviceType == AJAX1 || deviceType == AJAX2;
   }

   public static final boolean isMobileClientV2(ClientType deviceType) {
      return deviceType == ANDROID || deviceType == BLACKBERRY || deviceType == MRE || deviceType == IOS;
   }

   public static final boolean isMobileClientV2AndNewVersion(ClientType deviceType, short version) {
      return deviceType == ANDROID && version >= 200 || deviceType == BLACKBERRY && version >= 500 || deviceType == MRE && version >= 100 || deviceType == IOS && version >= 100;
   }

   public static final boolean canShowMidletTabs(ClientType deviceType) {
      if (deviceType == null) {
         return false;
      } else {
         switch(deviceType) {
         case MIDP1:
         case MIDP2:
         case SYMBIAN:
         case BLACKBERRY:
            return true;
         default:
            return false;
         }
      }
   }

   public static final boolean isMobileClientV2AndNewVersionOrAjax(ClientType deviceType, short version) {
      return isMobileClientV2AndNewVersion(deviceType, version) || isAjax(deviceType);
   }

   static {
      ClientType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ClientType clientType = arr$[i$];
         LOOKUP.put(clientType.value, clientType);
      }

   }
}
