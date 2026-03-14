package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataAvatar extends FusionPacket {
   public FusionPktDataAvatar() {
      super(PacketType.AVATAR);
   }

   public FusionPktDataAvatar(short transactionId) {
      super(PacketType.AVATAR, transactionId);
   }

   public FusionPktDataAvatar(FusionPacket packet) {
      super(packet);
   }

   public final String getStatusMessage() {
      return this.getStringField((short)1);
   }

   public final void setStatusMessage(String statusMessage) {
      this.setField((short)1, statusMessage);
   }

   public final String getDisplayPictureGuid() {
      return this.getStringField((short)2);
   }

   public final void setDisplayPictureGuid(String displayPictureGuid) {
      this.setField((short)2, displayPictureGuid);
   }

   public final String getBadgeHotkey() {
      return this.getStringField((short)3);
   }

   public final void setBadgeHotkey(String badgeHotkey) {
      this.setField((short)3, badgeHotkey);
   }

   public final String getAvatarPictureGuid() {
      return this.getStringField((short)4);
   }

   public final void setAvatarPictureGuid(String avatarPictureGuid) {
      this.setField((short)4, avatarPictureGuid);
   }

   public final String getFullbodyAvatarPictureGuid() {
      return this.getStringField((short)5);
   }

   public final void setFullbodyAvatarPictureGuid(String fullbodyAvatarPictureGuid) {
      this.setField((short)5, fullbodyAvatarPictureGuid);
   }
}
