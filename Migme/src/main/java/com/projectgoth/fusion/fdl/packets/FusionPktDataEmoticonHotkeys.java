package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataEmoticonHotkeys extends FusionPacket {
   public FusionPktDataEmoticonHotkeys() {
      super(PacketType.EMOTICON_HOTKEYS);
   }

   public FusionPktDataEmoticonHotkeys(short transactionId) {
      super(PacketType.EMOTICON_HOTKEYS, transactionId);
   }

   public FusionPktDataEmoticonHotkeys(FusionPacket packet) {
      super(packet);
   }

   public final String[] getHotkeyList() {
      return this.getStringArrayField((short)1, ' ');
   }

   public final void setHotkeyList(String[] hotkeyList) {
      this.setField((short)1, hotkeyList, ' ');
   }

   public final String[] getAlternateHotkeyList() {
      return this.getStringArrayField((short)2, ' ');
   }

   public final void setAlternateHotkeyList(String[] alternateHotkeyList) {
      this.setField((short)2, alternateHotkeyList, ' ');
   }
}
