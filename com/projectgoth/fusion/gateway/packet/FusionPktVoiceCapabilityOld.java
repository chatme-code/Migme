package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

/** @deprecated */
@Deprecated
public class FusionPktVoiceCapabilityOld extends FusionPacket {
   public FusionPktVoiceCapabilityOld() {
      super((short)418);
   }

   public FusionPktVoiceCapabilityOld(short transactionId) {
      super((short)418, transactionId);
   }

   public FusionPktVoiceCapabilityOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getContactId() {
      return this.getIntField((short)1);
   }

   public void setContactId(int ContactId) {
      this.setField((short)1, ContactId);
   }

   public Byte getVoiceCapability() {
      return this.getByteField((short)2);
   }

   public void setVoiceCapability(byte voiceCapability) {
      this.setField((short)2, voiceCapability);
   }
}
