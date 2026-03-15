package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataAcceptContactRequest extends FusionRequest {
   public FusionPktDataAcceptContactRequest() {
      super(PacketType.ACCEPT_CONTACT_REQUEST);
   }

   public FusionPktDataAcceptContactRequest(short transactionId) {
      super(PacketType.ACCEPT_CONTACT_REQUEST, transactionId);
   }

   public FusionPktDataAcceptContactRequest(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataAcceptContactRequest(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getContactUsername() {
      return this.getStringField((short)1);
   }

   public final void setContactUsername(String contactUsername) {
      this.setField((short)1, contactUsername);
   }

   public final Boolean getAddContact() {
      return this.getBooleanField((short)2);
   }

   public final void setAddContact(boolean addContact) {
      this.setField((short)2, addContact);
   }

   public final Integer getGroupId() {
      return this.getIntField((short)3);
   }

   public final void setGroupId(int groupId) {
      this.setField((short)3, groupId);
   }

   public final Boolean getShareMobilePhone() {
      return this.getBooleanField((short)4);
   }

   public final void setShareMobilePhone(boolean shareMobilePhone) {
      this.setField((short)4, shareMobilePhone);
   }
}
