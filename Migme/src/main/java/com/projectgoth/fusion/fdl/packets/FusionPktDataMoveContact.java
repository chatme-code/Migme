package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataMoveContact extends FusionRequest {
   public FusionPktDataMoveContact() {
      super(PacketType.MOVE_CONTACT);
   }

   public FusionPktDataMoveContact(short transactionId) {
      super(PacketType.MOVE_CONTACT, transactionId);
   }

   public FusionPktDataMoveContact(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataMoveContact(FusionPacket packet) {
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

   public final Integer getGroupId() {
      return this.getIntField((short)2);
   }

   public final void setGroupId(int groupId) {
      this.setField((short)2, groupId);
   }
}
