package com.projectgoth.fusion.data;

import java.io.Serializable;

public class SMSRouteData implements Serializable, Comparable<SMSRouteData> {
   public Integer iddCode;
   public String areaCode;
   public SMSRouteData.TypeEnum type;
   public Integer gatewayID;
   public Integer priority;

   public int compareTo(SMSRouteData o) {
      return this.priority.compareTo(o.priority);
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("SMS Route: IDD Code=");
      builder.append(this.iddCode);
      builder.append(" Area Code=");
      builder.append(this.areaCode);
      builder.append(" Type=");
      builder.append(this.type);
      builder.append(" Gateway=");
      builder.append(this.gatewayID);
      builder.append(" Priority=");
      builder.append(this.priority);
      return builder.toString();
   }

   public static enum TypeEnum {
      SYSTEM_SMS(1),
      SYSTEM_WAP_PUSH(2),
      SYSTEM_PREMIUM_SMS(3),
      USER_SMS(4);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SMSRouteData.TypeEnum fromValue(int value) {
         SMSRouteData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SMSRouteData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
