package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.DateTimeUtils;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BasicMerchantTagDetailsData extends MerchantTagData implements Serializable {
   public String merchantUserName;
   public String userName;
   public UserData.TypeEnum userType;
   public String displayPicture;
   public String expiry;

   public BasicMerchantTagDetailsData(ResultSet rs) throws SQLException {
      super(rs);
      this.userName = rs.getString("username");
      this.merchantUserName = rs.getString("merchantusername");
      this.userType = UserData.TypeEnum.fromValue(rs.getInt("usertype"));
      this.displayPicture = rs.getString("displayPicture");
      this.expiry = DateTimeUtils.getStringForMigcore(rs.getTimestamp("expiry"));
   }
}
