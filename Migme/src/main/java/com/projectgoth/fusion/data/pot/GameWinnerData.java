package com.projectgoth.fusion.data.pot;

import java.io.Serializable;

public class GameWinnerData implements Serializable {
   private final int winnerUserid;
   private final double winningAmount;
   private final double fundedWinningAmount;
   private final String currency;

   public int getWinnerUserID() {
      return this.winnerUserid;
   }

   public double getWinningAmount() {
      return this.winningAmount;
   }

   public double getFundedWinningAmount() {
      return this.fundedWinningAmount;
   }

   public String getCurrency() {
      return this.currency;
   }

   public GameWinnerData(int winnerUserid, double amount, double fundedAmount, String currency) {
      this.winnerUserid = winnerUserid;
      this.winningAmount = amount;
      this.fundedWinningAmount = fundedAmount;
      this.currency = currency;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("GameWinnerData [winnerUserid=");
      builder.append(this.winnerUserid);
      builder.append(", winningAmount=");
      builder.append(this.winningAmount);
      builder.append(", fundedWinningAmount=");
      builder.append(this.fundedWinningAmount);
      builder.append(", currency=");
      builder.append(this.currency);
      builder.append("]");
      return builder.toString();
   }
}
