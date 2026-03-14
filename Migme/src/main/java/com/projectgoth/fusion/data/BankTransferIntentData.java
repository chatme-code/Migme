package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class BankTransferIntentData implements Serializable {
   public Integer id;
   public String username;
   public Date dateCreated;
   public Integer countryID;
   public Integer paymentProductID;
   public String surname;
   public String firstname;
   public String middlename;
   public String fiscalNumber;
   public Double amount;
   public String currency;
   public String countryDescription;
   public Integer statusID;
   public String additionalReference;
   public String accountHolder;
   public String bankName;
   public String externalReference;
   public Integer effortID;
   public String paymentReference;
   public Integer attemptID;
   public Integer merchantID;
   public String bankAccountNumber;
   public String statusDate;
   public String city;
   public Integer orderID;
   public String specialID;
   public String swiftCode;
   public BankTransferIntentData.StatusEnum status;

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

      public static BankTransferIntentData.StatusEnum fromValue(int value) {
         BankTransferIntentData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            BankTransferIntentData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
