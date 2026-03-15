package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktContactRequest extends FusionPacket {
   public FusionPktContactRequest() {
      super((short)412);
   }

   public FusionPktContactRequest(short transactionId) {
      super((short)412, transactionId);
   }

   public FusionPktContactRequest(FusionPacket packet) {
      super(packet);
   }

   public FusionPktContactRequest(String username, String profileURL) {
      super((short)412);
      this.setUsername(username);
      this.setProfileURL(profileURL);
   }

   public String getUsername() {
      return this.getStringField((short)1);
   }

   public void setUsername(String username) {
      this.setField((short)1, username);
   }

   public String getProfileURL() {
      return this.getStringField((short)2);
   }

   public void setProfileURL(String profileURL) {
      this.setField((short)2, profileURL);
   }
}
