package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class UserPotEligibilityData implements Serializable {
   private int userType;
   private boolean eligible;
   private double costInBaseCurrency;

   public UserPotEligibilityData() {
   }

   public UserPotEligibilityData(int userType, boolean eligible, double costInBaseCurrency) {
      this.userType = userType;
      this.eligible = eligible;
      this.costInBaseCurrency = costInBaseCurrency;
   }

   public int getUserType() {
      return this.userType;
   }

   public void setUserType(int type) {
      this.userType = type;
   }

   public boolean isEligible() {
      return this.eligible;
   }

   public void setEligible(boolean eligible) {
      this.eligible = eligible;
   }

   public double getCostInBaseCurrency() {
      return this.costInBaseCurrency;
   }

   public void setCostInBaseCurrency(double costInBaseCurrency) {
      this.costInBaseCurrency = costInBaseCurrency;
   }

   public static UserPotEligibilityData fromResultSet(ResultSet rs) throws SQLException {
      DecimalFormat twoDForm = new DecimalFormat("#.##");
      UserPotEligibilityData data = new UserPotEligibilityData();
      data.userType = rs.getInt("type");
      data.eligible = rs.getBoolean("eligible");
      data.costInBaseCurrency = Double.valueOf(twoDForm.format(rs.getDouble("costInBaseCurrency")));
      return data;
   }
}
