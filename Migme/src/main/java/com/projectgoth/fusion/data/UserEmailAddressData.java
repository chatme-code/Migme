package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UserEmailAddressData implements Serializable {
   private static final long serialVersionUID = -4388724723576487201L;
   public int userId;
   public String emailAddress;
   public UserEmailAddressData.UserEmailAddressTypeEnum type;
   public Date verifiedDate;
   public boolean verified;

   public UserEmailAddressData(ResultSet rs) throws SQLException {
      this.userId = rs.getInt("userid");
      this.emailAddress = rs.getString("emailaddress");
      this.type = UserEmailAddressData.UserEmailAddressTypeEnum.fromValue(rs.getInt("type"));
      this.verifiedDate = rs.getTimestamp("dateverified");
      this.verified = rs.getBoolean("verified");
   }

   public boolean isVerified() {
      return this.verified;
   }

   public static enum UserEmailAddressTypeEnum {
      PRIMARY(1, "Primary"),
      SECONDARY(2, "Secondary");

      public int value;
      public String label;

      private UserEmailAddressTypeEnum(int code, String label) {
         this.value = code;
         this.label = label;
      }

      public static UserEmailAddressData.UserEmailAddressTypeEnum fromValue(int value) {
         UserEmailAddressData.UserEmailAddressTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserEmailAddressData.UserEmailAddressTypeEnum e = arr$[i$];
            if (e.value == value) {
               return e;
            }
         }

         return null;
      }
   }
}
