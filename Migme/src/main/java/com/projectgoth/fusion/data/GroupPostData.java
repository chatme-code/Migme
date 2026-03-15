package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class GroupPostData implements Serializable {
   public Integer id;
   public Integer groupModuleID;
   public String teaser;
   public String body;
   public Date dateCreated;
   public String createdBy;
   public Date lastModifiedDate;
   public String lastModifiedBy;
   public GroupPostData.StatusEnum status;

   public GroupPostData() {
   }

   public GroupPostData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.groupModuleID = (Integer)rs.getObject("groupModuleID");
      this.teaser = rs.getString("teaser");
      this.body = rs.getString("body");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.createdBy = rs.getString("createdBy");
      this.lastModifiedDate = rs.getTimestamp("lastModifiedDate");
      this.lastModifiedBy = rs.getString("lastModifiedBy");
      Integer intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = GroupPostData.StatusEnum.fromValue(intVal);
      }

   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1),
      PREVIEW(2);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static GroupPostData.StatusEnum fromValue(int value) {
         GroupPostData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupPostData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
