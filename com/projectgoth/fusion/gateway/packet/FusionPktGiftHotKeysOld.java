package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGiftHotKeysOld extends FusionPacket {
   public FusionPktGiftHotKeysOld() {
      super((short)936);
   }

   public FusionPktGiftHotKeysOld(short transactionId) {
      super((short)936, transactionId);
   }

   public String[] getHotKeys() {
      return this.getStringArrayField((short)1);
   }

   public void setHotKeys(String[] hotKeys) {
      this.setField((short)1, hotKeys);
   }

   public String[] getGiftNames() {
      return this.getStringArrayField((short)2);
   }

   public void setGiftNames(String[] giftNames) {
      this.setField((short)2, giftNames);
   }

   public String[] getGiftPrice() {
      return this.getStringArrayField((short)3);
   }

   public void setGiftPrice(String[] giftPrice) {
      this.setField((short)3, giftPrice);
   }
}
