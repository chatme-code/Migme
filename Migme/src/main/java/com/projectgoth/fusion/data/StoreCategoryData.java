package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StoreCategoryData implements Serializable {
   public Integer id;
   public String name;
   public Integer parentStoreCategoryID;
   public StoreCategoryData parentStoreCategory = null;
   public Integer sortOrder;
   public Integer totalItems;

   public StoreCategoryData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("ID");
      this.name = rs.getString("Name");
      this.parentStoreCategoryID = rs.getInt("ParentStoreCategoryID");
      this.sortOrder = rs.getInt("SortOrder");
   }
}
