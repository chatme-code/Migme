package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataChatroomNotification extends FusionPacket {
   public FusionPktDataChatroomNotification() {
      super(PacketType.CHATROOM_NOTIFICATION);
   }

   public FusionPktDataChatroomNotification(short transactionId) {
      super(PacketType.CHATROOM_NOTIFICATION, transactionId);
   }

   public FusionPktDataChatroomNotification(FusionPacket packet) {
      super(packet);
   }

   public final String getText() {
      return this.getStringField((short)1);
   }

   public final void setText(String text) {
      this.setField((short)1, text);
   }

   public final String getUrl() {
      return this.getStringField((short)2);
   }

   public final void setUrl(String url) {
      this.setField((short)2, url);
   }
}
