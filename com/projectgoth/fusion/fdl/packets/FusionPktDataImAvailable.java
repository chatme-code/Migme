package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImDetailType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataImAvailable extends FusionPacket {
   public FusionPktDataImAvailable() {
      super(PacketType.IM_AVAILABLE);
   }

   public FusionPktDataImAvailable(short transactionId) {
      super(PacketType.IM_AVAILABLE, transactionId);
   }

   public FusionPktDataImAvailable(FusionPacket packet) {
      super(packet);
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)1));
   }

   public final void setImType(ImType imType) {
      this.setField((short)1, imType.value());
   }

   public final String getName() {
      return this.getStringField((short)2);
   }

   public final void setName(String name) {
      this.setField((short)2, name);
   }

   public final MessageType getMessageType() {
      return MessageType.fromValue(this.getByteField((short)3));
   }

   public final void setMessageType(MessageType messageType) {
      this.setField((short)3, messageType.value());
   }

   public final ImDetailType getImDetail() {
      return ImDetailType.fromValue(this.getByteField((short)4));
   }

   public final void setImDetail(ImDetailType imDetail) {
      this.setField((short)4, imDetail.value());
   }

   public final Boolean getSupportsGroupChat() {
      return this.getBooleanField((short)5);
   }

   public final void setSupportsGroupChat(boolean supportsGroupChat) {
      this.setField((short)5, supportsGroupChat);
   }
}
