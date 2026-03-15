package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetMidletPropertyOld extends FusionPacket {
   public FusionPktGetMidletPropertyOld() {
      super((short)12);
   }

   public FusionPktGetMidletPropertyOld(short transactionId) {
      super((short)12, transactionId);
   }

   public FusionPktGetMidletPropertyOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktGetMidletPropertyOld(byte propertyType, String key) {
      super((short)12);
      this.setPropertyType(propertyType);
      this.setKey(key);
   }

   public Byte getPropertyType() {
      return this.getByteField((short)1);
   }

   public void setPropertyType(Byte propertyType) {
      this.setField((short)1, propertyType);
   }

   public String getKey() {
      return this.getStringField((short)2);
   }

   public void setKey(String key) {
      this.setField((short)2, key);
   }
}
