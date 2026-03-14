package com.projectgoth.fusion.payment;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentSummaryData implements Serializable {
   public int count = 0;
   public double cummValue = 0.0D;

   public void populateFrom(ResultSet rs) throws SQLException {
      Object countObj = rs.getObject("count");
      if (countObj != null) {
         this.count = rs.getInt("count");
      }

      Object cummValueObj = rs.getObject("cummValue");
      if (cummValueObj != null) {
         this.cummValue = rs.getDouble("cummValue");
      }

   }
}
