package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataImSessionStatus;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktImSessionStatus extends FusionPktDataImSessionStatus {
   public FusionPktImSessionStatus() {
   }

   public FusionPktImSessionStatus(short transactionId) {
      super(transactionId);
   }

   public FusionPktImSessionStatus(FusionPacket packet) {
      super(packet);
   }

   public FusionPktImSessionStatus(ImType imType, FusionPktDataImSessionStatus.StatusType status, String reason) {
      this((short)0, imType, status, reason);
   }

   public FusionPktImSessionStatus(short transactionId, ImType imType, FusionPktDataImSessionStatus.StatusType status, String reason) {
      super(transactionId);
      this.setImType(imType);
      this.setStatus(status);
      if (reason != null) {
         this.setReason(reason);
      }

      this.setSupportsConference(imType == ImType.MSN || imType == ImType.YAHOO);
   }
}
