package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetContacts extends FusionRequest {
   public FusionPktDataGetContacts() {
      super(PacketType.GET_CONTACTS);
   }

   public FusionPktDataGetContacts(short transactionId) {
      super(PacketType.GET_CONTACTS, transactionId);
   }

   public FusionPktDataGetContacts(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetContacts(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final PresenceType getPresence() {
      return PresenceType.fromValue(this.getByteField((short)1));
   }

   public final void setPresence(PresenceType presence) {
      this.setField((short)1, presence.value());
   }

   public final Integer getContactId() {
      return this.getIntField((short)2);
   }

   public final void setContactId(int contactId) {
      this.setField((short)2, contactId);
   }
}
