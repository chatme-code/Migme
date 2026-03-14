package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.slice.GroupEvent;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GroupEventData implements Serializable {
   public Integer id;
   public Integer groupID;
   public String description;
   public Date startTime;
   public Integer durationMinutes;
   public String chatRoomName;
   public Integer chatRoomCategoryID;
   public Date dateCreated;
   public boolean alertSent;
   public GroupEventData.StatusEnum status;
   public String remainingTime;
   public String chatRoomCategoryName;

   public static GroupEventData fromResultSet(ResultSet rs) throws SQLException {
      GroupEventData groupEventData = new GroupEventData();
      groupEventData.id = (Integer)rs.getObject("id");
      groupEventData.groupID = (Integer)rs.getObject("groupID");
      groupEventData.description = rs.getString("description");
      groupEventData.startTime = rs.getTimestamp("startTime");
      groupEventData.chatRoomName = rs.getString("chatRoomName");
      groupEventData.chatRoomCategoryID = rs.getInt("chatRoomCategoryID");
      groupEventData.durationMinutes = rs.getInt("DurationMinutes");
      if (groupEventData.durationMinutes == 0) {
         groupEventData.durationMinutes = 60;
      }

      groupEventData.dateCreated = rs.getTimestamp("dateCreated");
      groupEventData.alertSent = rs.getBoolean("alertSent");
      Integer intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         groupEventData.status = GroupEventData.StatusEnum.fromValue(intVal);
      }

      Date now = new Date();
      if (groupEventData.startTime.after(now)) {
         String timeLeft = DateTimeUtils.getRemainingTime(groupEventData.startTime);
         if (timeLeft != null && timeLeft.length() != 0) {
            groupEventData.remainingTime = "Starts in " + timeLeft;
         } else {
            groupEventData.remainingTime = "Starts in less than a minute!";
         }
      } else if (DateTimeUtils.plusMinutes(groupEventData.startTime, groupEventData.durationMinutes).after(now)) {
         groupEventData.remainingTime = "ON NOW!";
      } else {
         SimpleDateFormat df = new SimpleDateFormat("d MMM HH:mm");
         groupEventData.remainingTime = "Started " + df.format(groupEventData.startTime) + " GMT";
      }

      return groupEventData;
   }

   public static GroupEventData fromResultSetWithChatRoomCategoryName(ResultSet rs) throws SQLException {
      GroupEventData groupEventData = fromResultSet(rs);
      groupEventData.chatRoomCategoryName = rs.getString("chatroomcategoryname");
      return groupEventData;
   }

   public static GroupEventData fromGroupEvent(GroupEvent groupEvent) {
      GroupEventData groupEventData = new GroupEventData();
      groupEventData.chatRoomCategoryID = groupEvent.chatRoomCategoryID;
      groupEventData.chatRoomName = groupEvent.chatRoomName;
      groupEventData.dateCreated = new Date(groupEvent.dateCreated);
      groupEventData.description = groupEvent.description;
      groupEventData.durationMinutes = groupEvent.duration;
      groupEventData.groupID = groupEvent.groupId;
      groupEventData.id = groupEvent.id;
      groupEventData.startTime = new Date(groupEvent.startTime);
      groupEventData.status = GroupEventData.StatusEnum.fromValue(groupEvent.status);
      return groupEventData;
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

      public static GroupEventData.StatusEnum fromValue(int value) {
         GroupEventData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupEventData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
