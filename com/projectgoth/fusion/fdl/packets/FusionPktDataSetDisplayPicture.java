package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataSetDisplayPicture extends FusionRequest {
   public FusionPktDataSetDisplayPicture() {
      super(PacketType.SET_DISPLAY_PICTURE);
   }

   public FusionPktDataSetDisplayPicture(short transactionId) {
      super(PacketType.SET_DISPLAY_PICTURE, transactionId);
   }

   public FusionPktDataSetDisplayPicture(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataSetDisplayPicture(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getDisplayPictureGuid() {
      return this.getStringField((short)1);
   }

   public final void setDisplayPictureGuid(String displayPictureGuid) {
      this.setField((short)1, displayPictureGuid);
   }
}
