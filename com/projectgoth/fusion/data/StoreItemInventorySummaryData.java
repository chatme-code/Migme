package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StoreItemInventorySummaryData extends StoreItemInventoryData implements Serializable {
   private int count;

   public StoreItemInventorySummaryData(ResultSet rs) throws SQLException {
      super(rs);
      this.count = rs.getInt("count");
   }

   public int getCount() {
      return this.count;
   }
}
