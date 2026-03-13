package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataAddGroup extends FusionRequest {
   public FusionPktDataAddGroup() {
      super(PacketType.ADD_GROUP);
   }

   public FusionPktDataAddGroup(short transactionId) {
      super(PacketType.ADD_GROUP, transactionId);
   }

   public FusionPktDataAddGroup(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataAddGroup(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getGroupName() {
      return this.getStringField((short)2);
   }

   public final void setGroupName(String groupName) {
      this.setField((short)2, groupName);
   }
}
