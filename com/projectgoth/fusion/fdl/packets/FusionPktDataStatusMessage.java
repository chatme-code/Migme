package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataStatusMessage extends FusionPacket {
   public FusionPktDataStatusMessage() {
      super(PacketType.STATUS_MESSAGE);
   }

   public FusionPktDataStatusMessage(short transactionId) {
      super(PacketType.STATUS_MESSAGE, transactionId);
   }

   public FusionPktDataStatusMessage(FusionPacket packet) {
      super(packet);
   }

   public final Integer getContactId() {
      return this.getIntField((short)1);
   }

   public final void setContactId(int contactId) {
      this.setField((short)1, contactId);
   }

   public final String getStatusMessage() {
      return this.getStringField((short)2);
   }

   public final void setStatusMessage(String statusMessage) {
      this.setField((short)2, statusMessage);
   }

   public final Long getTimestamp() {
      return this.getLongField((short)3);
   }

   public final void setTimestamp(long timestamp) {
      this.setField((short)3, timestamp);
   }
}
