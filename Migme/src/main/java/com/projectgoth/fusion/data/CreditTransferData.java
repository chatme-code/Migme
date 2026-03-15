package com.projectgoth.fusion.data;

import java.io.Serializable;

public class CreditTransferData implements Serializable {
   private static final long serialVersionUID = 7242592585963825428L;
   private AccountEntryData accountEntryData;
   private AccountBalanceData accountBalanceData;

   public CreditTransferData(AccountEntryData accountEntryData, AccountBalanceData accountBalanceData) {
      this.accountEntryData = accountEntryData;
      this.accountBalanceData = accountBalanceData;
   }

   public AccountEntryData getAccountEntryData() {
      return this.accountEntryData;
   }

   public void setAccountEntryData(AccountEntryData accountEntryData) {
      this.accountEntryData = accountEntryData;
   }

   public AccountBalanceData getAccountBalanceData() {
      return this.accountBalanceData;
   }

   public void setAccountBalanceData(AccountBalanceData accountBalanceData) {
      this.accountBalanceData = accountBalanceData;
   }

   public static enum CreditTransferFeeEnum {
      NON_TOP_MERCHANT_TO_NON_TOP_MERCHANT(1),
      NON_TOP_MERCHANT_TO_TOP_MERCHANT(2),
      TOP_MERCHANT_TO_NON_TOP_MERCHANT(3),
      TOP_MERCHANT_TO_TOP_MERCHANT(4);

      private int value;

      private CreditTransferFeeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CreditTransferData.CreditTransferFeeEnum fromValue(int value) {
         CreditTransferData.CreditTransferFeeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CreditTransferData.CreditTransferFeeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }

      public static CreditTransferData.CreditTransferFeeEnum fromUserType(UserData.TypeEnum senderUserType, UserData.TypeEnum receipientUserType) {
         if (senderUserType != UserData.TypeEnum.MIG33_TOP_MERCHANT) {
            return receipientUserType != UserData.TypeEnum.MIG33_TOP_MERCHANT ? NON_TOP_MERCHANT_TO_NON_TOP_MERCHANT : NON_TOP_MERCHANT_TO_TOP_MERCHANT;
         } else {
            return receipientUserType != UserData.TypeEnum.MIG33_TOP_MERCHANT ? TOP_MERCHANT_TO_NON_TOP_MERCHANT : TOP_MERCHANT_TO_TOP_MERCHANT;
         }
      }
   }
}
