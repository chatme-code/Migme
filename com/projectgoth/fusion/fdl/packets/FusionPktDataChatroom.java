package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataChatroom extends FusionPacket {
   public FusionPktDataChatroom() {
      super(PacketType.CHATROOM);
   }

   public FusionPktDataChatroom(short transactionId) {
      super(PacketType.CHATROOM, transactionId);
   }

   public FusionPktDataChatroom(FusionPacket packet) {
      super(packet);
   }

   public final String getChatroomName() {
      return this.getStringField((short)1);
   }

   public final void setChatroomName(String chatroomName) {
      this.setField((short)1, chatroomName);
   }

   public final String getDescription() {
      return this.getStringField((short)2);
   }

   public final void setDescription(String description) {
      this.setField((short)2, description);
   }

   public final Integer getMaxmiumNumberOfParticipants() {
      return this.getIntField((short)3);
   }

   public final void setMaxmiumNumberOfParticipants(int maxmiumNumberOfParticipants) {
      this.setField((short)3, maxmiumNumberOfParticipants);
   }

   public final Integer getNumberOfParticipants() {
      return this.getIntField((short)4);
   }

   public final void setNumberOfParticipants(int numberOfParticipants) {
      this.setField((short)4, numberOfParticipants);
   }

   public final Boolean getAdultOnly() {
      return this.getBooleanField((short)5);
   }

   public final void setAdultOnly(boolean adultOnly) {
      this.setField((short)5, adultOnly);
   }

   public final Short getCategoryId() {
      return this.getShortField((short)6);
   }

   public final void setCategoryId(short categoryId) {
      this.setField((short)6, categoryId);
   }

   public final Boolean getUserOwned() {
      return this.getBooleanField((short)7);
   }

   public final void setUserOwned(boolean userOwned) {
      this.setField((short)7, userOwned);
   }

   public final Integer getGroupId() {
      return this.getIntField((short)8);
   }

   public final void setGroupId(int groupId) {
      this.setField((short)8, groupId);
   }

   public final Integer getChatroomId() {
      return this.getIntField((short)9);
   }

   public final void setChatroomId(int chatroomId) {
      this.setField((short)9, chatroomId);
   }

   public final String getCreatorName() {
      return this.getStringField((short)10);
   }

   public final void setCreatorName(String creatorName) {
      this.setField((short)10, creatorName);
   }

   public final Integer getThemeId() {
      return this.getIntField((short)11);
   }

   public final void setThemeId(int themeId) {
      this.setField((short)11, themeId);
   }

   public final String getNewOwnerName() {
      return this.getStringField((short)12);
   }

   public final void setNewOwnerName(String newOwnerName) {
      this.setField((short)12, newOwnerName);
   }

   public final String getIconUrl() {
      return this.getStringField((short)13);
   }

   public final void setIconUrl(String iconUrl) {
      this.setField((short)13, iconUrl);
   }
}
