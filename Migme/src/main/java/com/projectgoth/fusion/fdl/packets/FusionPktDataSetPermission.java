package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.UserPermissionType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataSetPermission extends FusionRequest {
   public FusionPktDataSetPermission() {
      super(PacketType.SET_PERMISSION);
   }

   public FusionPktDataSetPermission(short transactionId) {
      super(PacketType.SET_PERMISSION, transactionId);
   }

   public FusionPktDataSetPermission(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataSetPermission(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getUsername() {
      return this.getStringField((short)1);
   }

   public final void setUsername(String username) {
      this.setField((short)1, username);
   }

   public final UserPermissionType getPermission() {
      return UserPermissionType.fromValue(this.getByteField((short)2));
   }

   public final void setPermission(UserPermissionType permission) {
      this.setField((short)2, permission.value());
   }
}
