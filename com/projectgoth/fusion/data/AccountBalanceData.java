package com.projectgoth.fusion.data;

import java.io.Serializable;

public class AccountBalanceData implements Serializable {
   public CurrencyData currency;
   public double balance;
   public double fundedBalance;

   public double getBaseBalance() {
      return this.balance / this.currency.exchangeRate;
   }

   public double getBaseFundedBalance() {
      return this.fundedBalance / this.currency.exchangeRate;
   }

   public String format() {
      return this.currency.format(this.balance);
   }

   public String formatWithCode() {
      return this.currency.formatWithCode(this.balance);
   }
}
