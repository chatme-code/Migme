package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.List;

public class SMSGatewayData implements Serializable {
   public Integer id;
   public String name;
   public SMSGatewayData.TypeEnum type;
   public String url;
   public Integer port;
   public SMSGatewayData.MethodEnum method;
   public String iddPrefix;
   public String authorization;
   public String usernameParam;
   public String passwordParam;
   public String sourceParam;
   public String destinationParam;
   public String messageParam;
   public String unicodeMessageParam;
   public String unicodeParam;
   public String extraParam;
   public String unicodeCharset;
   public String successPattern;
   public String errorPattern;
   public Boolean deliveryReporting;
   public SMSGatewayData.StatusEnum status;
   public List<SMSRouteData> smsRoutes;

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SMSGatewayData.StatusEnum fromValue(int value) {
         SMSGatewayData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SMSGatewayData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum MethodEnum {
      GET(1),
      POST(2);

      private int value;

      private MethodEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SMSGatewayData.MethodEnum fromValue(int value) {
         SMSGatewayData.MethodEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SMSGatewayData.MethodEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      HTTP(1),
      SMPP_TRANSMITTER(2),
      SMPP_TRANSCEIVER(3);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SMSGatewayData.TypeEnum fromValue(int value) {
         SMSGatewayData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SMSGatewayData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
