package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.fdl.enums.ClientType;

public class SSOEnums {
   public static enum View {
      UNKNOWN(0, "UNKNOWN"),
      MIGBO_WEB(1, "MIGBO_WEB"),
      MIGBO_WAP(2, "MIGBO_WAP"),
      MIG33_AJAX(3, "MIG33_AJAX"),
      MIG33_MIDLET(4, "MIG33_MIDLET"),
      MIG33_TOUCH(5, "MIG33_TOUCH"),
      MIG33_WAP(6, "MIG33_WAP"),
      MIG33_CORPORATE(7, "MIG33_CORPORATE"),
      MIG33_WINDOWS_MOBILE(8, "MIG33_WINDOWS_MOBILE"),
      MIGBO_MIDLET(9, "MIGBO_MIDLET"),
      MIGBO_TOUCH(10, "MIGBO_TOUCH"),
      MIGBO_WINDOWS_MOBILE(11, "MIGBO_WINDOWS_MOBILE"),
      MIG33_BLACKBERRY(12, "MIG33_BLACKBERRY"),
      MIGBO_BLACKBERRY(13, "MIGBO_BLACKBERRY"),
      MIG33_BLAAST(14, "MIG33_BLAAST"),
      MIGBO_BLAAST(15, "MIGBO_BLAAST"),
      MIG33_MRE(16, "MIG33_MRE"),
      MIGBO_MRE(17, "MIGBO_MRE"),
      MIG33_AJAXV2(18, "MIG33_AJAXV2"),
      MIGBO_AJAXV2(19, "MIGBO_AJAXV2"),
      MIG33_IOS(20, "MIG33_IOS"),
      MIGBO_IOS(21, "MIGBO_IOS");

      private int value;
      private String name;

      private View(int value, String name) {
         this.value = value;
         this.name = name;
      }

      public int value() {
         return this.value;
      }

      public static SSOEnums.View fromValue(int value) {
         SSOEnums.View[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SSOEnums.View e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return UNKNOWN;
      }

      public static SSOEnums.View fromString(String name) {
         SSOEnums.View[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SSOEnums.View e = arr$[i$];
            if (e.toString().equals(name.toUpperCase())) {
               return e;
            }
         }

         return UNKNOWN;
      }

      public String toString(String view) {
         return this.name;
      }

      public boolean isMigboView() {
         return this.name.startsWith("MIGBO_");
      }

      public ClientType toFusionDeviceEnum() {
         switch(this) {
         case MIG33_AJAX:
            return ClientType.AJAX1;
         case MIG33_AJAXV2:
            return ClientType.AJAX2;
         case MIG33_WAP:
            return ClientType.WAP;
         case MIGBO_WEB:
         case MIGBO_WAP:
            return ClientType.MIGBO;
         case MIG33_TOUCH:
         case MIGBO_TOUCH:
            return ClientType.ANDROID;
         case MIG33_MIDLET:
         case MIGBO_MIDLET:
            return ClientType.MIDP2;
         case MIG33_WINDOWS_MOBILE:
         case MIGBO_WINDOWS_MOBILE:
            return ClientType.WINDOWS_MOBILE;
         case MIG33_BLACKBERRY:
         case MIGBO_BLACKBERRY:
            return ClientType.BLACKBERRY;
         case MIG33_BLAAST:
         case MIGBO_BLAAST:
            return ClientType.BLAAST;
         case MIG33_MRE:
         case MIGBO_MRE:
            return ClientType.MRE;
         case MIG33_IOS:
         case MIGBO_IOS:
            return ClientType.IOS;
         default:
            return null;
         }
      }

      public static SSOEnums.View fromFusionDeviceEnum(ClientType deviceTypeEnum) {
         switch(deviceTypeEnum) {
         case AJAX1:
            return MIG33_AJAX;
         case AJAX2:
            return MIG33_AJAXV2;
         case WAP:
            return MIG33_WAP;
         case MIGBO:
            return MIGBO_WEB;
         case ANDROID:
            return MIG33_TOUCH;
         case MIDP1:
         case MIDP2:
            return MIG33_MIDLET;
         case WINDOWS_MOBILE:
            return MIG33_WINDOWS_MOBILE;
         case BLACKBERRY:
            return MIG33_BLACKBERRY;
         case BLAAST:
            return MIG33_BLAAST;
         case MRE:
            return MIG33_MRE;
         case IOS:
            return MIG33_IOS;
         default:
            return UNKNOWN;
         }
      }
   }
}
