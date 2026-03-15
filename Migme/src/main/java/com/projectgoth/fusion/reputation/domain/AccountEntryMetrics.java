package com.projectgoth.fusion.reputation.domain;

public class AccountEntryMetrics implements Metrics {
   public static final int USERNAME_INDEX = 0;
   public static final int KICKS_INDEX = 1;
   public static final int AUTHENTICATED_REFERRAL_INDEX = 2;
   public static final int RECHARGED_AMOUNT_INDEX = 3;
   private String username;
   private int kicksInitiated;
   private int authenticatedReferrals;
   private double rechargedAmount;

   public void reset(String username) {
      this.username = username;
      this.kicksInitiated = 0;
      this.authenticatedReferrals = 0;
      this.rechargedAmount = 0.0D;
   }

   public String getUsername() {
      return this.username;
   }

   public int getKicksInitiated() {
      return this.kicksInitiated;
   }

   public void addKicksInitiated(int kicksInitiated) {
      this.kicksInitiated += kicksInitiated;
   }

   public int getAuthenticatedReferrals() {
      return this.authenticatedReferrals;
   }

   public void addAuthenticatedReferrals(int authenticatedReferrals) {
      this.authenticatedReferrals += authenticatedReferrals;
   }

   public double getRechargedAmount() {
      return this.rechargedAmount;
   }

   public void addRechargedAmount(double rechargedAmount) {
      this.rechargedAmount += rechargedAmount;
   }

   public boolean hasMetrics() {
      return this.authenticatedReferrals != 0 || this.kicksInitiated != 0 || this.rechargedAmount != 0.0D;
   }

   public String toLine() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.username).append(',').append(this.kicksInitiated).append(',').append(this.authenticatedReferrals).append(',').append((long)(this.rechargedAmount * 100.0D));
      return builder.toString();
   }
}
