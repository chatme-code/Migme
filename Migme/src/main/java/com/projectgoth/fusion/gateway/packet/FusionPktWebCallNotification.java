package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktWebCallNotification extends FusionPacket {
   public FusionPktWebCallNotification() {
      super((short)802);
   }

   public FusionPktWebCallNotification(short transactionId) {
      super((short)802, transactionId);
   }

   public FusionPktWebCallNotification(FusionPacket packet) {
      super(packet);
   }

   public String getSource() {
      return this.getStringField((short)1);
   }

   public void setSource(String source) {
      this.setField((short)1, source);
   }

   public String getDestination() {
      return this.getStringField((short)2);
   }

   public void setDestination(String destination) {
      this.setField((short)2, destination);
   }

   public Integer getGateway() {
      return this.getIntField((short)3);
   }

   public void setGateway(int gateway) {
      this.setField((short)3, gateway);
   }

   public String getGatewayName() {
      return this.getStringField((short)4);
   }

   public void setGatewayName(String gatewayName) {
      this.setField((short)4, gatewayName);
   }

   public Byte getSourceProtocol() {
      return this.getByteField((short)5);
   }

   public void setSourceProtocol(byte sourceProtocol) {
      this.setField((short)5, sourceProtocol);
   }
}
