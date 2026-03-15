package com.projectgoth.fusion.data;

import java.io.Serializable;

public class RewardedMigCreditData implements Serializable {
   private final double amount;
   private final String currency;

   public RewardedMigCreditData(double amount, String currency) {
      this.amount = amount;
      this.currency = currency;
   }

   public double getAmount() {
      return this.amount;
   }

   public String getCurrency() {
      return this.currency;
   }

   public String toString() {
      return this.amount + " " + this.currency;
   }
}
