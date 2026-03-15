package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataRemoveContact extends FusionRequest {
   public FusionPktDataRemoveContact() {
      super(PacketType.REMOVE_CONTACT);
   }

   public FusionPktDataRemoveContact(short transactionId) {
      super(PacketType.REMOVE_CONTACT, transactionId);
   }

   public FusionPktDataRemoveContact(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataRemoveContact(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final Integer getContactId() {
      return this.getIntField((short)1);
   }

   public final void setContactId(int contactId) {
      this.setField((short)1, contactId);
   }
}
