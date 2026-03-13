package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class PremiumSMSPaymentData implements Serializable {
   public Integer id;
   public String username;
   public Integer systemSMSID;
   public Date dateCreated;
   public Double amount;
   public Double fee;
   public String responseCode;
   public PremiumSMSPaymentData.StatusEnum status;

   public static enum StatusEnum {
      PENDING(0),
      SUCCEEDED(1),
      FAILED(2);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static PremiumSMSPaymentData.StatusEnum fromValue(int value) {
         PremiumSMSPaymentData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            PremiumSMSPaymentData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
