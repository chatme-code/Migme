package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktReportOld extends FusionRequest {
   public FusionPktReportOld() {
      super((short)10);
   }

   public FusionPktReportOld(short transactionId) {
      super((short)10, transactionId);
   }

   public FusionPktReportOld(FusionPacket packet) {
      super(packet);
   }

   public Short getReportType() {
      return this.getShortField((short)1);
   }

   public void setReportType(Short reportType) {
      this.setField((short)1, reportType);
   }

   public String getDescription() {
      return this.getStringField((short)2);
   }

   public void setDescription(String description) {
      this.setField((short)2, description);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return new FusionPacket[]{new FusionPktOk(this.transactionId)};
   }
}
