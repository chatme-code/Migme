package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class GroupModuleData implements Serializable {
   public Integer id;
   public Integer groupID;
   public String title;
   public Date dateCreated;
   public String createdBy;
   public Date lastModifiedDate;
   public String lastModifiedBy;
   public Integer position;
   public GroupModuleData.TypeEnum type;
   public GroupModuleData.StatusEnum status;

   public GroupModuleData() {
   }

   public GroupModuleData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.groupID = (Integer)rs.getObject("groupID");
      this.title = rs.getString("title");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.createdBy = rs.getString("createdBy");
      this.lastModifiedDate = rs.getTimestamp("lastModifiedDate");
      this.lastModifiedBy = rs.getString("lastModifiedBy");
      this.position = (Integer)rs.getObject("position");
      Integer intVal = (Integer)rs.getObject("type");
      if (intVal != null) {
         this.type = GroupModuleData.TypeEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = GroupModuleData.StatusEnum.fromValue(intVal);
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

      public static GroupModuleData.StatusEnum fromValue(int value) {
         GroupModuleData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupModuleData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      POSTS(1),
      EVENTS(2);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static GroupModuleData.TypeEnum fromValue(int value) {
         GroupModuleData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupModuleData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
