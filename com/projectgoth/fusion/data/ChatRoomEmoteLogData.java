package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ChatRoomEmoteLogData implements Serializable {
   private static final long serialVersionUID = -8191311459459579006L;
   private int id;
   private String instigator;
   private String target;
   private String emote;
   private int chatroomId;
   private int groupId;
   private int reasonCode;
   private Date dateCreated;
   private String parameters;

   public ChatRoomEmoteLogData() {
   }

   public ChatRoomEmoteLogData(String instigator, String target, String emote, int chatroomId, int groupId, int reasonCode, String parameters) {
      this.instigator = instigator;
      this.target = target;
      this.emote = emote;
      this.chatroomId = chatroomId;
      this.groupId = groupId;
      this.reasonCode = reasonCode;
      this.dateCreated = new Date(System.currentTimeMillis());
      this.parameters = parameters;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getInstigator() {
      return this.instigator;
   }

   public void setInstigator(String instigator) {
      this.instigator = instigator;
   }

   public String getTarget() {
      return this.target;
   }

   public void setTarget(String target) {
      this.target = target;
   }

   public String getEmote() {
      return this.emote;
   }

   public void setEmote(String emote) {
      this.emote = emote;
   }

   public int getChatroomId() {
      return this.chatroomId;
   }

   public void setChatroomId(int chatroomId) {
      this.chatroomId = chatroomId;
   }

   public int getGroupId() {
      return this.groupId;
   }

   public void setGroupId(int groupId) {
      this.groupId = groupId;
   }

   public int getReasonCode() {
      return this.reasonCode;
   }

   public void setReasonCode(int reasonCode) {
      this.reasonCode = reasonCode;
   }

   public Date getDateCreated() {
      return this.dateCreated;
   }

   public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
   }

   public String getParameters() {
      return this.parameters;
   }

   public void setParameters(String parameters) {
      this.parameters = parameters;
   }

   public static ChatRoomEmoteLogData fromResultSet(ResultSet rs) throws SQLException {
      ChatRoomEmoteLogData data = new ChatRoomEmoteLogData();
      data.setId(rs.getInt("id"));
      data.setInstigator(rs.getString("instigator"));
      data.setTarget(rs.getString("target"));
      data.setEmote("emote");
      data.setChatroomId(rs.getInt("chatroomid"));
      data.setGroupId(rs.getInt("groupid"));
      data.setReasonCode(rs.getInt("reasoncode"));
      data.setDateCreated(rs.getDate("datecreated"));
      data.setParameters(rs.getString("parameters"));
      return data;
   }

   public String toString() {
      return this.instigator + ", " + this.target + ", " + this.emote + ", " + this.chatroomId + ", " + this.groupId + ", " + this.reasonCode + ", " + this.parameters;
   }
}
