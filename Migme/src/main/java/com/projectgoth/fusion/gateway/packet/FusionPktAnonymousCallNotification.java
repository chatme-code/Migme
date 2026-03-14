package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktAnonymousCallNotification extends FusionPacket {
   public FusionPktAnonymousCallNotification() {
      super((short)806);
   }

   public FusionPktAnonymousCallNotification(short transactionId) {
      super((short)806, transactionId);
   }

   public FusionPktAnonymousCallNotification(FusionPacket packet) {
      super(packet);
   }

   public String getDescription() {
      return this.getStringField((short)1);
   }

   public void setDescription(String description) {
      this.setField((short)1, description);
   }

   public String getRequestingUsername() {
      return this.getStringField((short)2);
   }

   public void setRequestingUsername(String requestingUsername) {
      this.setField((short)2, requestingUsername);
   }
}
