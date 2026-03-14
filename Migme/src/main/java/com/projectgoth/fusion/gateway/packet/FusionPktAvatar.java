package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.fdl.packets.FusionPktDataAvatar;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktAvatar extends FusionPktDataAvatar {
   public FusionPktAvatar() {
   }

   public FusionPktAvatar(short transactionId) {
      super(transactionId);
   }

   public FusionPktAvatar(FusionPacket packet) {
      super(packet);
   }

   public FusionPktAvatar(short transactionId, UserData userData) {
      super(transactionId);
      if (userData.displayPicture != null) {
         this.setDisplayPictureGuid(userData.displayPicture);
      }

      if (userData.avatar != null) {
         this.setAvatarPictureGuid(userData.avatar);
      }

      if (userData.statusMessage != null) {
         this.setStatusMessage(userData.statusMessage);
      }

      if (userData.fullbodyAvatar != null) {
         this.setFullbodyAvatarPictureGuid(userData.fullbodyAvatar);
      }

   }
}
