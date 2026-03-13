package com.projectgoth.fusion.data;

import com.projectgoth.fusion.slice.ContactGroupDataIce;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactGroupData implements Serializable {
   public static final int DEFAULT_GROUP_ID = -1;
   public static final String DEFAULT_GROUP_NAME = "migme";
   public static final int AIM_GROUP_ID = -2;
   public static final String AIM_GROUP_NAME = "AIM";
   public static final int FACEBOOK_GROUP_ID = -3;
   public static final String FACEBOOK_GROUP_NAME = "Facebook";
   public static final int GTALK_GROUP_ID = -4;
   public static final String GTALK_GROUP_NAME = "Google Talk";
   public static final int MSN_GROUP_ID = -5;
   public static final String MSN_GROUP_NAME = "MSN";
   public static final int YAHOO_GROUP_ID = -6;
   public static final String YAHOO_GROUP_NAME = "Yahoo!";
   public Integer id;
   public String username;
   public String name;

   public ContactGroupData() {
   }

   public ContactGroupData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.username = rs.getString("username");
      this.name = rs.getString("name");
   }

   public ContactGroupData(ContactGroupDataIce contactGroupIce) {
      this.id = contactGroupIce.id == Integer.MIN_VALUE ? null : contactGroupIce.id;
      this.username = contactGroupIce.username.equals("\u0000") ? null : contactGroupIce.username;
      this.name = contactGroupIce.name.equals("\u0000") ? null : contactGroupIce.name;
   }

   public ContactGroupDataIce toIceObject() {
      ContactGroupDataIce contactGroupIce = new ContactGroupDataIce();
      contactGroupIce.id = this.id == null ? Integer.MIN_VALUE : this.id;
      contactGroupIce.username = this.username == null ? "\u0000" : this.username;
      contactGroupIce.name = this.name == null ? "\u0000" : this.name;
      return contactGroupIce;
   }
}
