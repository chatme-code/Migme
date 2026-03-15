package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class GroupInvitationData implements Serializable {
   public Integer id;
   public String username;
   public Integer groupID;
   public Date dateCreated;
   public String inviter;
   public GroupInvitationData.StatusEnum status;

   public GroupInvitationData() {
   }

   public GroupInvitationData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.username = rs.getString("username");
      this.groupID = (Integer)rs.getObject("groupID");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.inviter = rs.getString("inviter");
      Integer intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = GroupInvitationData.StatusEnum.fromValue(intVal);
      }

   }

   public static enum StatusEnum {
      PENDING(0),
      ACCEPTED(1),
      DECLINED(2);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static GroupInvitationData.StatusEnum fromValue(int value) {
         GroupInvitationData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupInvitationData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
