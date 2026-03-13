package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataUpdateGroup extends FusionRequest {
   public FusionPktDataUpdateGroup() {
      super(PacketType.UPDATE_GROUP);
   }

   public FusionPktDataUpdateGroup(short transactionId) {
      super(PacketType.UPDATE_GROUP, transactionId);
   }

   public FusionPktDataUpdateGroup(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataUpdateGroup(FusionPacket packet) {
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

   public final String getGroupName() {
      return this.getStringField((short)2);
   }

   public final void setGroupName(String groupName) {
      this.setField((short)2, groupName);
   }
}
