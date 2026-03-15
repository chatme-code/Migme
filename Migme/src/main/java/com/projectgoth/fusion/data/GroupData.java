package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.springframework.util.StringUtils;

public class GroupData implements Serializable, Comparable<GroupData> {
   public Integer id;
   public GroupData.TypeEnum type;
   public Integer countryID;
   public String name;
   public String description;
   public String about;
   public Date dateCreated;
   public String createdBy;
   public String picture;
   public String emailAddress;
   public String referralSMS;
   public Boolean allowNonMembersToJoinRooms;
   public Integer groupCategoryID;
   public Integer vipServiceID;
   public Integer numOfMembers;
   public GroupData.StatusEnum status;
   public Boolean pendingInvitation;
   public String categoryName;
   public Boolean supportsVIPs;

   public GroupData() {
   }

   public GroupData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.type = GroupData.TypeEnum.fromValue(rs.getByte("type"));
      this.countryID = (Integer)rs.getObject("countryID");
      this.name = rs.getString("name");
      this.description = rs.getString("description");
      this.about = rs.getString("about");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.createdBy = rs.getString("createdBy");
      this.picture = rs.getString("picture");
      this.emailAddress = rs.getString("emailAddress");
      this.referralSMS = rs.getString("referralSMS");
      this.allowNonMembersToJoinRooms = (Boolean)rs.getObject("AllowNonMembersToJoinRooms");
      this.groupCategoryID = (Integer)rs.getObject("groupCategoryID");
      this.vipServiceID = (Integer)rs.getObject("vipServiceID");
      this.numOfMembers = (Integer)rs.getObject("NumMembers");
      Integer intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = GroupData.StatusEnum.fromValue(intVal);
      }

      try {
         intVal = (Integer)rs.getObject("vipservicestatus");
         this.supportsVIPs = intVal != null && intVal == ServiceData.StatusEnum.ACTIVE.value();
      } catch (SQLException var5) {
      }

      try {
         String categoryName = rs.getString("categoryname");
         if (StringUtils.hasLength(categoryName)) {
            this.categoryName = categoryName;
         }
      } catch (SQLException var4) {
      }

   }

   public int compareTo(GroupData groupData) {
      return groupData == null ? 1 : this.id.compareTo(groupData.id);
   }

   public boolean isOpenGroup() {
      return this.type == GroupData.TypeEnum.OPEN;
   }

   public boolean isClosedGroup() {
      return this.type == GroupData.TypeEnum.CLOSED;
   }

   public static enum TypeEnum {
      OPEN((byte)0),
      CLOSED((byte)1),
      UNLISTED((byte)2);

      private byte value;

      private TypeEnum(byte value) {
         this.value = value;
      }

      public byte value() {
         return this.value;
      }

      public static GroupData.TypeEnum fromValue(byte value) {
         GroupData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static GroupData.StatusEnum fromValue(int value) {
         GroupData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
