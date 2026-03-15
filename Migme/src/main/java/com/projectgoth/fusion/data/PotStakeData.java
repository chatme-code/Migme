package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class PotStakeData implements Serializable {
   private Integer id;
   private Integer potID;
   private Integer userID;
   private Date dateCreated;
   private Double amount;
   private Double fundedAmount;
   private String currency;
   private Double exchangeRate;
   private boolean eligible;
   private String username;

   public PotStakeData() {
   }

   public PotStakeData(int potID, int userID, double amount, double fundedAmount, String currency, double exchangeRate) {
      this.potID = potID;
      this.userID = userID;
      this.dateCreated = new Date();
      this.amount = amount;
      this.fundedAmount = fundedAmount;
      this.currency = currency;
      this.exchangeRate = exchangeRate;
      this.eligible = true;
   }

   public static PotStakeData fromResultSet(ResultSet rs) throws SQLException {
      PotStakeData potStakeData = new PotStakeData();
      potStakeData.id = rs.getInt("id");
      potStakeData.potID = rs.getInt("potid");
      potStakeData.userID = rs.getInt("userid");
      potStakeData.dateCreated = rs.getDate("datecreated");
      potStakeData.amount = rs.getDouble("amount");
      potStakeData.fundedAmount = rs.getDouble("fundedamount");
      potStakeData.currency = rs.getString("currency");
      potStakeData.exchangeRate = rs.getDouble("exchangerate");
      potStakeData.eligible = rs.getBoolean("eligible");
      return potStakeData;
   }

   public static PotStakeData fromResultSetWithUserName(ResultSet rs) throws SQLException {
      PotStakeData potStakeData = fromResultSet(rs);
      potStakeData.username = rs.getString("username");
      return potStakeData;
   }

   public String toString() {
      return String.format("%.2f %s (%.2f %s)", this.amount, this.currency, this.getAmountInBaseCurrency(), "base");
   }

   public double getAmountInBaseCurrency() {
      return this.amount / this.exchangeRate;
   }

   public double getFundedAmountInBaseCurrency() {
      return this.fundedAmount / this.exchangeRate;
   }

   public Integer getId() {
      return this.id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Integer getPotID() {
      return this.potID;
   }

   public void setPotID(Integer potID) {
      this.potID = potID;
   }

   public Integer getUserID() {
      return this.userID;
   }

   public void setUserID(Integer userID) {
      this.userID = userID;
   }

   public Date getDateCreated() {
      return this.dateCreated;
   }

   public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
   }

   public Double getAmount() {
      return this.amount;
   }

   public void setAmount(Double amount) {
      this.amount = amount;
   }

   public void addToAmount(double amount, double fundedAmount) {
      this.amount = this.amount + amount;
      this.fundedAmount = this.fundedAmount + fundedAmount;
   }

   public Double getFundedAmount() {
      return this.fundedAmount;
   }

   public void setFundedAmount(Double fundedAmount) {
      this.fundedAmount = fundedAmount;
   }

   public String getCurrency() {
      return this.currency;
   }

   public void setCurrency(String currency) {
      this.currency = currency;
   }

   public Double getExchangeRate() {
      return this.exchangeRate;
   }

   public void setExchangeRate(Double exchangeRate) {
      this.exchangeRate = exchangeRate;
   }

   public boolean isEligible() {
      return this.eligible;
   }

   public void setEligible(boolean eligible) {
      this.eligible = eligible;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }
}
