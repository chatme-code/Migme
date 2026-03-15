package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataImIcons extends FusionPacket {
   public FusionPktDataImIcons() {
      super(PacketType.IM_ICONS);
   }

   public FusionPktDataImIcons(short transactionId) {
      super(PacketType.IM_ICONS, transactionId);
   }

   public FusionPktDataImIcons(FusionPacket packet) {
      super(packet);
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)1));
   }

   public final void setImType(ImType imType) {
      this.setField((short)1, imType.value());
   }

   public final byte[] getOnlineIconImageData() {
      return this.getByteArrayField((short)2);
   }

   public final void setOnlineIconImageData(byte[] onlineIconImageData) {
      this.setField((short)2, onlineIconImageData);
   }

   public final byte[] getRoamingIconImageData() {
      return this.getByteArrayField((short)3);
   }

   public final void setRoamingIconImageData(byte[] roamingIconImageData) {
      this.setField((short)3, roamingIconImageData);
   }

   public final byte[] getBusyIconImageData() {
      return this.getByteArrayField((short)4);
   }

   public final void setBusyIconImageData(byte[] busyIconImageData) {
      this.setField((short)4, busyIconImageData);
   }

   public final byte[] getAwayIconImageData() {
      return this.getByteArrayField((short)5);
   }

   public final void setAwayIconImageData(byte[] awayIconImageData) {
      this.setField((short)5, awayIconImageData);
   }

   public final byte[] getOfflineIconImageData() {
      return this.getByteArrayField((short)6);
   }

   public final void setOfflineIconImageData(byte[] offlineIconImageData) {
      this.setField((short)6, offlineIconImageData);
   }
}
