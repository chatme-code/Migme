package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetChatrooms extends FusionRequest {
   public FusionPktDataGetChatrooms() {
      super(PacketType.GET_CHATROOMS);
   }

   public FusionPktDataGetChatrooms(short transactionId) {
      super(PacketType.GET_CHATROOMS, transactionId);
   }

   public FusionPktDataGetChatrooms(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetChatrooms(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getSearchString() {
      return this.getStringField((short)1);
   }

   public final void setSearchString(String searchString) {
      this.setField((short)1, searchString);
   }

   public final Byte getPage() {
      return this.getByteField((short)2);
   }

   public final void setPage(byte page) {
      this.setField((short)2, page);
   }

   public final String[] getChatroomNameList() {
      return this.getStringArrayField((short)3, ';');
   }

   public final void setChatroomNameList(String[] chatroomNameList) {
      this.setField((short)3, chatroomNameList, ';');
   }
}
