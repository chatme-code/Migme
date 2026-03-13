package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MerchantTagUserData extends MerchantTagData implements Serializable {
   public String userName;
   public String merchantUserName;
   public Integer userType;
   public Integer merchantUserType;

   public MerchantTagUserData(ResultSet rs) throws SQLException {
      super(rs);
      this.userName = rs.getString("username");
      this.merchantUserName = rs.getString("merchantUserName");
      this.userType = rs.getInt("usertype");
      if (this.userType == 0) {
         this.userType = null;
      }

      this.merchantUserType = rs.getInt("merchantusertype");
      if (this.merchantUserType == 0) {
         this.merchantUserType = null;
      }

   }
}
