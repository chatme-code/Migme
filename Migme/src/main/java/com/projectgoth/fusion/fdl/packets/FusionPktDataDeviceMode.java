package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.DeviceModeType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataDeviceMode extends FusionRequest {
   public FusionPktDataDeviceMode() {
      super(PacketType.DEVICE_MODE);
   }

   public FusionPktDataDeviceMode(short transactionId) {
      super(PacketType.DEVICE_MODE, transactionId);
   }

   public FusionPktDataDeviceMode(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataDeviceMode(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final DeviceModeType getDeviceMode() {
      return DeviceModeType.fromValue(this.getIntField((short)1));
   }

   public final void setDeviceMode(DeviceModeType deviceMode) {
      this.setField((short)1, deviceMode.value());
   }
}
