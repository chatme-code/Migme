package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataVoiceCapability extends FusionPacket {
   public FusionPktDataVoiceCapability() {
      super(PacketType.VOICE_CAPABILITY);
   }

   public FusionPktDataVoiceCapability(short transactionId) {
      super(PacketType.VOICE_CAPABILITY, transactionId);
   }

   public FusionPktDataVoiceCapability(FusionPacket packet) {
      super(packet);
   }

   public final Integer getContactId() {
      return this.getIntField((short)1);
   }

   public final void setContactId(int contactId) {
      this.setField((short)1, contactId);
   }

   public final Boolean getIsVoiceCapable() {
      return this.getBooleanField((short)2);
   }

   public final void setIsVoiceCapable(boolean isVoiceCapable) {
      this.setField((short)2, isVoiceCapable);
   }
}
