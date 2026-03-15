package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataImLogin extends FusionRequest {
   public FusionPktDataImLogin() {
      super(PacketType.IM_LOGIN);
   }

   public FusionPktDataImLogin(short transactionId) {
      super(PacketType.IM_LOGIN, transactionId);
   }

   public FusionPktDataImLogin(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataImLogin(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)1));
   }

   public final void setImType(ImType imType) {
      this.setField((short)1, imType.value());
   }

   public final PresenceType getInitialPresence() {
      return PresenceType.fromValue(this.getByteField((short)2));
   }

   public final void setInitialPresence(PresenceType initialPresence) {
      this.setField((short)2, initialPresence.value());
   }

   public final Boolean getShowOfflineContacts() {
      return this.getBooleanField((short)3);
   }

   public final void setShowOfflineContacts(boolean showOfflineContacts) {
      this.setField((short)3, showOfflineContacts);
   }
}
