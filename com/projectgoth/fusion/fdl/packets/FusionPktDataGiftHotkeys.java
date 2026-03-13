package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataGiftHotkeys extends FusionPacket {
   public FusionPktDataGiftHotkeys() {
      super(PacketType.GIFT_HOTKEYS);
   }

   public FusionPktDataGiftHotkeys(short transactionId) {
      super(PacketType.GIFT_HOTKEYS, transactionId);
   }

   public FusionPktDataGiftHotkeys(FusionPacket packet) {
      super(packet);
   }

   public final String[] getHotkeyList() {
      return this.getStringArrayField((short)1);
   }

   public final void setHotkeyList(String[] hotkeyList) {
      this.setField((short)1, hotkeyList);
   }

   public final String[] getNameList() {
      return this.getStringArrayField((short)2);
   }

   public final void setNameList(String[] nameList) {
      this.setField((short)2, nameList);
   }

   public final String[] getPriceList() {
      return this.getStringArrayField((short)3);
   }

   public final void setPriceList(String[] priceList) {
      this.setField((short)3, priceList);
   }
}
