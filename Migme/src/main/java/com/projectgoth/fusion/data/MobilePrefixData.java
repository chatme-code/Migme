package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MobilePrefixData implements Serializable {
   public Integer IDDCode;
   public Integer prefix;
   public Integer minLength;
   public Integer maxLength;

   public MobilePrefixData() {
   }

   public MobilePrefixData(ResultSet rs) throws SQLException {
      this.IDDCode = rs.getInt("IDDCode");
      this.prefix = rs.getInt("prefix");
      this.minLength = rs.getInt("minLength");
      this.maxLength = rs.getInt("maxLength");
   }
}
