package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StoreRatingSummaryData implements Serializable {
   public Float average;
   public Integer total;
   public Integer numRatings;

   public StoreRatingSummaryData(ResultSet rs) throws SQLException {
      this.average = rs.getFloat("Average");
      this.total = rs.getInt("Total");
      this.numRatings = rs.getInt("NumRatings");
   }
}
