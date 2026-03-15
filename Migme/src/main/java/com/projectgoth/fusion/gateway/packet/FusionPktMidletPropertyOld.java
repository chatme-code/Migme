package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktMidletPropertyOld extends FusionRequest {
   public static Byte RMS = 1;
   public static Byte COOKIE = 2;
   public static String DEVICE_TOKEN = "DT";

   public FusionPktMidletPropertyOld() {
      super((short)11);
   }

   public FusionPktMidletPropertyOld(short transactionId) {
      super((short)11, transactionId);
   }

   public FusionPktMidletPropertyOld(FusionPacket packet) {
      super(packet);
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

   public String getValue() {
      return this.getStringField((short)3);
   }

   public void setValue(String value) {
      this.setField((short)3, value);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return (new FusionPktOk(this.transactionId)).toArray();
   }
}
