package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.DateTimeUtils;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UserPostData implements Serializable {
   public Integer id;
   public String username;
   public String body;
   public Date dateCreated;
   public Integer parentUserPostID;
   public Integer numReplies;
   public Date lastReplyDate;
   public UserPostData.StatusEnum status;
   public String dateCreatedTimeSince;
   public String lastReplyTimeSince;

   public UserPostData() {
   }

   public UserPostData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("ID");
      this.username = rs.getString("Username");
      this.body = rs.getString("Body");
      this.dateCreated = rs.getTimestamp("DateCreated");
      this.parentUserPostID = (Integer)rs.getObject("ParentUserPostID");
      this.numReplies = rs.getInt("NumReplies");
      this.lastReplyDate = rs.getTimestamp("LastReplyDate");
      this.dateCreatedTimeSince = DateTimeUtils.getTimeSince(this.dateCreated);
      this.lastReplyTimeSince = DateTimeUtils.getTimeSince(this.lastReplyDate);
      Integer intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = UserPostData.StatusEnum.fromValue(intVal);
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

      public static UserPostData.StatusEnum fromValue(int value) {
         UserPostData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserPostData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
