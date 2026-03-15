package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktIMSessionStatusOld extends FusionPacket {
   public FusionPktIMSessionStatusOld() {
      super((short)207);
   }

   public FusionPktIMSessionStatusOld(short transactionId) {
      super((short)207, transactionId);
   }

   public FusionPktIMSessionStatusOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktIMSessionStatusOld(ImType imType, byte status, String reason) {
      this((short)0, imType, status, reason);
   }

   public FusionPktIMSessionStatusOld(short transactionId, ImType imType, byte status, String reason) {
      super((short)207, transactionId);
      this.setIMType(imType.value());
      this.setStatus(status);
      if (reason != null) {
         this.setReason(reason);
      }

      this.setSupportsConference((byte)(imType != ImType.MSN && imType != ImType.YAHOO ? 0 : 1));
   }

   public Byte getIMType() {
      return this.getByteField((short)1);
   }

   public void setIMType(byte imType) {
      this.setField((short)1, imType);
   }

   public Byte getStatus() {
      return this.getByteField((short)2);
   }

   public void setStatus(byte status) {
      this.setField((short)2, status);
   }

   public String getReason() {
      return this.getStringField((short)3);
   }

   public void setReason(String reason) {
      this.setField((short)3, reason);
   }

   public Byte getSupportsConference() {
      return this.getByteField((short)4);
   }

   public void setSupportsConference(byte supportsConference) {
      this.setField((short)4, supportsConference);
   }
}
