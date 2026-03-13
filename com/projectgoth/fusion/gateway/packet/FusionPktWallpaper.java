package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktWallpaper extends FusionPacket {
   public FusionPktWallpaper() {
      super((short)912);
   }

   public FusionPktWallpaper(short transactionId) {
      super((short)912, transactionId);
   }

   public FusionPktWallpaper(FusionPacket packet) {
      super(packet);
   }

   public Integer getID() {
      return this.getIntField((short)1);
   }

   public void setID(int id) {
      this.setField((short)1, id);
   }

   public byte[] getContent() {
      return this.getByteArrayField((short)2);
   }

   public void setContent(byte[] content) {
      this.setField((short)2, content);
   }
}
