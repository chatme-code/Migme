package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataDisplayPicture extends FusionPacket {
   public FusionPktDataDisplayPicture() {
      super(PacketType.DISPLAY_PICTURE);
   }

   public FusionPktDataDisplayPicture(short transactionId) {
      super(PacketType.DISPLAY_PICTURE, transactionId);
   }

   public FusionPktDataDisplayPicture(FusionPacket packet) {
      super(packet);
   }

   public final Integer getContactId() {
      return this.getIntField((short)1);
   }

   public final void setContactId(int contactId) {
      this.setField((short)1, contactId);
   }

   public final String getDisplayPictureGuid() {
      return this.getStringField((short)2);
   }

   public final void setDisplayPictureGuid(String displayPictureGuid) {
      this.setField((short)2, displayPictureGuid);
   }

   public final Long getTimestamp() {
      return this.getLongField((short)3);
   }

   public final void setTimestamp(long timestamp) {
      this.setField((short)3, timestamp);
   }
}
