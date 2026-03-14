package com.projectgoth.fusion.data;

import java.io.Serializable;

public class DiscountTierData implements Serializable {
   public Integer id;
   public String name;
   public DiscountTierData.TypeEnum type;
   public Double actualMin;
   public Double displayMin;
   public Double max;
   public String currency;
   public Double percentageDiscount;
   public boolean applyToCreditCard;
   public boolean applyToBankTransfer;
   public boolean applyToWesternUnion;
   public boolean applyToVoucher;
   public boolean applyToTelegraphicTransfer;
   public DiscountTierData.StatusEnum status;
   public Double discountAmount;
   public Double adjustmentAmount;
   public boolean canBeApplied;
   public boolean appliedThisMonth;
   public Double dbActualMin;
   public Double dbDisplayMin;
   public Double dbMax;
   public String dbCurrency;

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

      public static DiscountTierData.StatusEnum fromValue(int value) {
         DiscountTierData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            DiscountTierData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      FIRST_TIME_ONLY(1),
      RECURRING(2);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static DiscountTierData.TypeEnum fromValue(int value) {
         DiscountTierData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            DiscountTierData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
