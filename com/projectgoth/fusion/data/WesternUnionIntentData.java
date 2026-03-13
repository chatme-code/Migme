package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class WesternUnionIntentData implements Serializable {
   public Integer id;
   public String username;
   public Date dateCreated;
   public Integer countryID;
   public Integer paymentProductID;
   public String surname;
   public Double amount;
   public String currency;
   public String returnMAC;
   public Integer statusID;
   public String additionalReference;
   public String ref;
   public String formMethod;
   public String externalReference;
   public Integer effortID;
   public String mac;
   public String paymentReference;
   public String formAction;
   public Integer attemptID;
   public Integer merchantID;
   public String statusDate;
   public Integer orderID;
   public WesternUnionIntentData.StatusEnum status;
   public String companyName;
   public String companyCode;
   public String countryCode;

   public static enum StatusEnum {
      OPEN(0),
      MATCHED(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static WesternUnionIntentData.StatusEnum fromValue(int value) {
         WesternUnionIntentData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            WesternUnionIntentData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
