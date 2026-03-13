package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktAvatarOld extends FusionPacket {
   public FusionPktAvatarOld() {
      super((short)603);
   }

   public FusionPktAvatarOld(short transactionId) {
      super((short)603, transactionId);
   }

   public FusionPktAvatarOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktAvatarOld(short transactionId, UserData userData) {
      super((short)603, transactionId);
      if (userData.displayPicture != null) {
         this.setDisplayPicture(userData.displayPicture);
      }

      if (userData.avatar != null) {
         this.setAvatar(userData.avatar);
      }

      if (userData.statusMessage != null) {
         this.setStatusMessage(userData.statusMessage);
      }

      if (userData.fullbodyAvatar != null) {
         this.setFullbodyAvatar(userData.fullbodyAvatar);
      }

   }

   public String getStatusMessage() {
      return this.getStringField((short)1);
   }

   public void setStatusMessage(String statusMessage) {
      this.setField((short)1, statusMessage);
   }

   public String getDisplayPicture() {
      return this.getStringField((short)2);
   }

   public void setDisplayPicture(String displayPicture) {
      this.setField((short)2, displayPicture);
   }

   public String getBadgeHotKey() {
      return this.getStringField((short)3);
   }

   public void setBadgeHotKey(String badgeHotKey) {
      this.setField((short)3, badgeHotKey);
   }

   public String getAvatar() {
      return this.getStringField((short)4);
   }

   public void setAvatar(String avatar) {
      this.setField((short)4, avatar);
   }

   public String getFullbodyAvatar() {
      return this.getStringField((short)5);
   }

   public void setFullbodyAvatar(String fullbodyAvatar) {
      this.setField((short)5, fullbodyAvatar);
   }
}
