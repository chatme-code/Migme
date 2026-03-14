package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ThirdPartyApplicationData implements Serializable {
   public int id;
   public String name;
   public String displayName;
   public String description;
   public String imageUrl;
   public int groupId;
   public List<String> views = new ArrayList();

   public void addView(ResultSet rsRow) throws SQLException {
      if (rsRow.getString("view") != null) {
         this.views.add(rsRow.getString("view"));
      }

   }

   public void initializeMainData(ResultSet rsRow) throws SQLException {
      this.id = rsRow.getInt("id");
      this.name = rsRow.getString("name");
      this.displayName = rsRow.getString("displayname");
      this.description = rsRow.getString("description");
      this.imageUrl = rsRow.getString("picture");
      this.groupId = rsRow.getInt("groupid");
   }
}
