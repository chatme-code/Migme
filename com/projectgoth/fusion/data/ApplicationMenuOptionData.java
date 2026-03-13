package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApplicationMenuOptionData implements Serializable {
   public Integer menuVersionId;
   public Integer position;
   public Integer textId;
   public String iconURL;
   public String actionURL;

   public ApplicationMenuOptionData() {
   }

   public ApplicationMenuOptionData(ResultSet rs) throws SQLException {
      this.menuVersionId = (Integer)rs.getObject("id");
      this.position = (Integer)rs.getObject("position");
      this.textId = (Integer)rs.getObject("textId");
      this.iconURL = rs.getString("iconURL");
      this.actionURL = rs.getString("actionURL");
   }
}
