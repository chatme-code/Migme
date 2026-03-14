package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class GroupAnnouncementData implements Serializable {
   public Integer id;
   public Integer groupID;
   public Date dateCreated;
   public String createdBy;
   public String title;
   public String text;
   public String smsText;
   public Date lastModifiedDate;
   public String lastModifiedBy;
   public GroupAnnouncementData.StatusEnum status;
   public String picture;

   public GroupAnnouncementData() {
   }

   public GroupAnnouncementData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.groupID = (Integer)rs.getObject("groupID");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.createdBy = rs.getString("createdBy");
      this.title = rs.getString("title");
      this.text = rs.getString("text");
      this.smsText = rs.getString("smsText");
      this.lastModifiedDate = rs.getTimestamp("lastModifiedDate");
      this.lastModifiedBy = rs.getString("lastModifiedBy");
      Integer intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = GroupAnnouncementData.StatusEnum.fromValue(intVal);
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

      public static GroupAnnouncementData.StatusEnum fromValue(int value) {
         GroupAnnouncementData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupAnnouncementData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
