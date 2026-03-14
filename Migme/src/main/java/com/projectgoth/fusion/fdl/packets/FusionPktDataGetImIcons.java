package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetImIcons extends FusionRequest {
   public FusionPktDataGetImIcons() {
      super(PacketType.GET_IM_ICONS);
   }

   public FusionPktDataGetImIcons(short transactionId) {
      super(PacketType.GET_IM_ICONS, transactionId);
   }

   public FusionPktDataGetImIcons(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetImIcons(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final ImType[] getImTypeList() {
      return ImType.fromByteArrayValues(this.getByteArrayField((short)1));
   }

   public final void setImTypeList(ImType[] imTypeList) {
      this.setByteEnumArrayField((short)1, imTypeList);
   }
}
