package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.MidletPropertyType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataMidletProperty extends FusionRequest {
   public FusionPktDataMidletProperty() {
      super(PacketType.MIDLET_PROPERTY);
   }

   public FusionPktDataMidletProperty(short transactionId) {
      super(PacketType.MIDLET_PROPERTY, transactionId);
   }

   public FusionPktDataMidletProperty(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataMidletProperty(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return false;
   }

   public final MidletPropertyType getPropertyType() {
      return MidletPropertyType.fromValue(this.getByteField((short)1));
   }

   public final void setPropertyType(MidletPropertyType propertyType) {
      this.setField((short)1, propertyType.value());
   }

   public final String getKey() {
      return this.getStringField((short)2);
   }

   public final void setKey(String key) {
      this.setField((short)2, key);
   }

   public final String getValue() {
      return this.getStringField((short)3);
   }

   public final void setValue(String value) {
      this.setField((short)3, value);
   }
}
