package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.MidletPropertyType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataGetMidletProperty extends FusionPacket {
   public FusionPktDataGetMidletProperty() {
      super(PacketType.GET_MIDLET_PROPERTY);
   }

   public FusionPktDataGetMidletProperty(short transactionId) {
      super(PacketType.GET_MIDLET_PROPERTY, transactionId);
   }

   public FusionPktDataGetMidletProperty(FusionPacket packet) {
      super(packet);
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
}
