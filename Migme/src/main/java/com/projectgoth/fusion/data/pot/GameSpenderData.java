package com.projectgoth.fusion.data.pot;

import java.io.Serializable;

public class GameSpenderData implements Serializable {
   private final int spenderUserid;
   private final double spendingAmount;
   private final double spendingFundedAmount;
   private final String currency;

   public GameSpenderData(int spenderUserid, double spendingAmount, double spendingFundedAmount, String currency) {
      this.spenderUserid = spenderUserid;
      this.spendingAmount = spendingAmount;
      this.spendingFundedAmount = spendingFundedAmount;
      this.currency = currency;
   }

   public int getSpenderUserid() {
      return this.spenderUserid;
   }

   public double getSpendingAmount() {
      return this.spendingAmount;
   }

   public double getSpendingFundedAmount() {
      return this.spendingFundedAmount;
   }

   public String getCurrency() {
      return this.currency;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("GameSpenderData [spenderUserid=");
      builder.append(this.spenderUserid);
      builder.append(", spendingAmount=");
      builder.append(this.spendingAmount);
      builder.append(", spendingFundedAmount=");
      builder.append(this.spendingFundedAmount);
      builder.append(", currency=");
      builder.append(this.currency);
      builder.append("]");
      return builder.toString();
   }
}
