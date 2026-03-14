package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class WesternUnionReceivedData implements Serializable {
   public Integer id;
   public Integer westernUnionIntentID;
   public Date dateCreated;
   public WesternUnionReceivedData.TypeEnum type;
   public String fileName;
   public Integer row;
   public String paymentReference;
   public String invoiceNumber;
   public String customerID;
   public String additionalReference;
   public Integer effortNumber;
   public String invoiceCurrencyDeliv;
   public Double invoiceAmountDeliv;
   public String invoiceCurrencyLocal;
   public Double invoiceAmountLocal;
   public String paymentMethod;
   public String creditCardCompany;
   public String uncleanIndicator;
   public String paymentCurrency;
   public Double paymentAmount;
   public String currencyDue;
   public Double amountDue;
   public Integer dateDue;
   public String reversalCurrency;
   public Double reversalAmount;
   public String reversalReasonID;
   public String reversalReasonDescription;
   public Integer dateCollect;
   public WesternUnionReceivedData.StatusEnum status;

   public static enum StatusEnum {
      NEW(0),
      PROCESSED(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static WesternUnionReceivedData.StatusEnum fromValue(int value) {
         WesternUnionReceivedData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            WesternUnionReceivedData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      PAYMENT(1),
      CORRECTION(2),
      REVERSAL(3),
      REVERSAL_CORRECTION(4);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static WesternUnionReceivedData.TypeEnum fromValue(int value) {
         WesternUnionReceivedData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            WesternUnionReceivedData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
