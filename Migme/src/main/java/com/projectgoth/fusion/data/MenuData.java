package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuData implements Serializable {
   public Integer id;
   public Integer type;
   public Integer position;
   public String title;
   public String url;
   public Integer countryID;
   public Integer minVersion;
   public Integer maxVersion;
   public Integer clientType;
   public String location;

   public MenuData() {
   }

   public MenuData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.type = (Integer)rs.getObject("type");
      this.position = (Integer)rs.getObject("position");
      this.title = rs.getString("title");
      this.url = rs.getString("url");
      this.countryID = (Integer)rs.getObject("countryID");
      this.minVersion = (Integer)rs.getObject("minVersion");
      this.maxVersion = (Integer)rs.getObject("maxVersion");
      this.clientType = (Integer)rs.getObject("clientType");
      this.location = rs.getString("location");
   }
}
