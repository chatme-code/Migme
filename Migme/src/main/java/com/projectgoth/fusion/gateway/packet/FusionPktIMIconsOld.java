package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktIMIconsOld extends FusionPacket {
   public FusionPktIMIconsOld() {
      super((short)927);
   }

   public FusionPktIMIconsOld(short transactionId) {
      super((short)927, transactionId);
   }

   public FusionPktIMIconsOld(FusionPacket packet) {
      super(packet);
   }

   public Byte getIMType() {
      return this.getByteField((short)1);
   }

   public void setIMType(byte imType) {
      this.setField((short)1, imType);
   }

   public byte[] getOnline() {
      return this.getByteArrayField((short)2);
   }

   public void setOnline(byte[] online) {
      this.setField((short)2, online);
   }

   public byte[] getRoaming() {
      return this.getByteArrayField((short)3);
   }

   public void setRoaming(byte[] roaming) {
      this.setField((short)3, roaming);
   }

   public byte[] getBusy() {
      return this.getByteArrayField((short)4);
   }

   public void setBusy(byte[] busy) {
      this.setField((short)4, busy);
   }

   public byte[] getAway() {
      return this.getByteArrayField((short)5);
   }

   public void setAway(byte[] away) {
      this.setField((short)5, away);
   }

   public byte[] getOffline() {
      return this.getByteArrayField((short)6);
   }

   public void setOffline(byte[] offline) {
      this.setField((short)6, offline);
   }
}
