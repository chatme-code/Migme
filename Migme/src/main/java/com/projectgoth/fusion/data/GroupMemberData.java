package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class GroupMemberData implements Serializable {
   public Integer id;
   public String username;
   public Integer groupID;
   public Integer locationID;
   public Date dateCreated;
   public GroupMemberData.TypeEnum type;
   public Date dateLeft;
   public Boolean smsNotification;
   public Boolean emailNotification;
   public Boolean eventNotification;
   public Boolean smsGroupEventNotification;
   public Boolean emailThreadUpdateNotification;
   public Boolean eventThreadUpdateNotification;
   public GroupMemberData.StatusEnum status;
   public Boolean vip;
   public String displayPicture;

   public GroupMemberData() {
   }

   public GroupMemberData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.username = rs.getString("username");
      this.groupID = (Integer)rs.getObject("groupID");
      this.locationID = (Integer)rs.getObject("locationID");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.dateLeft = rs.getTimestamp("dateLeft");
      Integer intVal = (Integer)rs.getObject("type");
      if (intVal != null) {
         this.type = GroupMemberData.TypeEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("smsNotification");
      if (intVal != null) {
         this.smsNotification = intVal != 0;
      }

      intVal = (Integer)rs.getObject("emailNotification");
      if (intVal != null) {
         this.emailNotification = intVal != 0;
      }

      intVal = (Integer)rs.getObject("eventNotification");
      if (intVal != null) {
         this.eventNotification = intVal != 0;
      }

      intVal = (Integer)rs.getObject("smsGroupEventNotification");
      if (intVal != null) {
         this.smsGroupEventNotification = intVal != 0;
      }

      intVal = (Integer)rs.getObject("emailThreadUpdateNotification");
      if (intVal != null) {
         this.emailThreadUpdateNotification = intVal != 0;
      }

      intVal = (Integer)rs.getObject("eventThreadUpdateNotification");
      if (intVal != null) {
         this.eventThreadUpdateNotification = intVal != 0;
      }

      intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = GroupMemberData.StatusEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("vipsubscriptionid");
      this.vip = intVal != null && intVal > 0;
   }

   public boolean isModerator() {
      return this.type == GroupMemberData.TypeEnum.MODERATOR;
   }

   public boolean isAdministrator() {
      return this.type == GroupMemberData.TypeEnum.ADMINISTRATOR;
   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1),
      BANNED(2);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static GroupMemberData.StatusEnum fromValue(int value) {
         GroupMemberData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupMemberData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      REGULAR(1),
      ADMINISTRATOR(2),
      MODERATOR(3);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static GroupMemberData.TypeEnum fromValue(int value) {
         GroupMemberData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupMemberData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
