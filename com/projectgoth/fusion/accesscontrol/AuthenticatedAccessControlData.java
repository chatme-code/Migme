package com.projectgoth.fusion.accesscontrol;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticatedAccessControlData implements Serializable {
   private static final long serialVersionUID = -7406102788975206116L;
   String name;
   boolean isMobileVerifiedAllowed;
   boolean isEmailVerifiedAllowed;
   String mobileVerifiedRateLimit;
   String emailVerifiedRateLimit;

   public AuthenticatedAccessControlData(String name, boolean isMobileVerifiedAllowed, boolean isEmailVerifiedAllowed, String mobileVerifiedRateLimit, String emailVerifiedRateLimit) {
      this.name = name;
      this.isMobileVerifiedAllowed = isMobileVerifiedAllowed;
      this.isEmailVerifiedAllowed = isEmailVerifiedAllowed;
      this.mobileVerifiedRateLimit = mobileVerifiedRateLimit;
      this.emailVerifiedRateLimit = emailVerifiedRateLimit;
   }

   public AuthenticatedAccessControlData(ResultSet rs) throws SQLException {
      this.name = rs.getString("name");
      this.isMobileVerifiedAllowed = rs.getBoolean("mobileVerifiedAllowed");
      this.isEmailVerifiedAllowed = rs.getBoolean("emailVerifiedAllowed");
      this.mobileVerifiedRateLimit = rs.getString("mobileVerifiedRateLimit");
      this.emailVerifiedRateLimit = rs.getString("emailVerifiedRateLimit");
   }

   public String toString() {
      return String.format("AuthenticatedAccessControlData[n=%s,mobileVR=%s,emailVR=%s,mobileRL=%s,emailVR=%s]", this.name, this.isMobileVerifiedAllowed, this.isEmailVerifiedAllowed, this.mobileVerifiedRateLimit, this.emailVerifiedRateLimit);
   }
}
