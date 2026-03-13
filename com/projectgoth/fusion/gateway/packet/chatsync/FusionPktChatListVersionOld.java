package com.projectgoth.fusion.gateway.packet.chatsync;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatListVersionOld extends FusionPacket {
   public FusionPktChatListVersionOld() {
      super((short)561);
   }

   public FusionPktChatListVersionOld(short transactionId) {
      super((short)561, transactionId);
   }

   public FusionPktChatListVersionOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getVersion() {
      return this.getIntField((short)1);
   }

   public void setVersion(int ver) {
      this.setField((short)1, ver);
   }

   public Long getTimeStamp() {
      return this.getLongField((short)2);
   }

   public void setTimeStamp(long ts) {
      this.setField((short)2, ts);
   }
}
