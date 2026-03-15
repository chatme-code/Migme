package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktPreloginMarketingMsg extends FusionPacket {
   public FusionPktPreloginMarketingMsg() {
      super((short)932);
   }

   public FusionPktPreloginMarketingMsg(short transactionId) {
      super((short)932, transactionId);
   }

   public FusionPktPreloginMarketingMsg(FusionPacket packet) {
      super(packet);
   }

   public String getMarketingMsg() {
      return this.getStringField((short)1);
   }

   public void setMarketingMsg(String marketingMsg) {
      this.setField((short)1, marketingMsg);
   }

   public String getURL() {
      return this.getStringField((short)2);
   }

   public void setURL(String url) {
      this.setField((short)2, url);
   }
}
