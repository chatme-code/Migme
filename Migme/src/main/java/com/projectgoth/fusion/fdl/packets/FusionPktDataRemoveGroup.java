package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataRemoveGroup extends FusionRequest {
   public FusionPktDataRemoveGroup() {
      super(PacketType.REMOVE_GROUP);
   }

   public FusionPktDataRemoveGroup(short transactionId) {
      super(PacketType.REMOVE_GROUP, transactionId);
   }

   public FusionPktDataRemoveGroup(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataRemoveGroup(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final Integer getGroupId() {
      return this.getIntField((short)1);
   }

   public final void setGroupId(int groupId) {
      this.setField((short)1, groupId);
   }
}
