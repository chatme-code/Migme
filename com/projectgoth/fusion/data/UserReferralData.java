package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UserReferralData implements Serializable {
   public Integer id;
   public String username;
   public String referrerName;
   public Date dateCreated;
   public String mobilePhone;
   public Double amount;
   public Boolean paid;
   public Integer activationID;
   public String referredUsername;

   public UserReferralData() {
   }

   public UserReferralData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.username = rs.getString("username");
      this.referrerName = rs.getString("referrerName");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.mobilePhone = rs.getString("mobilePhone");
      this.amount = (Double)rs.getObject("amount");
      Integer intVal = (Integer)rs.getObject("paid");
      if (intVal != null) {
         this.paid = intVal != 0;
      }

      try {
         this.activationID = (Integer)rs.getObject("activationID");
      } catch (Exception var5) {
         this.activationID = null;
      }

      try {
         this.referredUsername = rs.getString("referredUsername");
      } catch (Exception var4) {
         this.referredUsername = null;
      }

   }
}
