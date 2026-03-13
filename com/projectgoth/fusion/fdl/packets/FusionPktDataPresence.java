package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataPresence extends FusionPacket {
   public FusionPktDataPresence() {
      super(PacketType.PRESENCE);
   }

   public FusionPktDataPresence(short transactionId) {
      super(PacketType.PRESENCE, transactionId);
   }

   public FusionPktDataPresence(FusionPacket packet) {
      super(packet);
   }

   public final Integer getContactId() {
      return this.getIntField((short)1);
   }

   public final void setContactId(int contactId) {
      this.setField((short)1, contactId);
   }

   public final PresenceType getPresence() {
      return PresenceType.fromValue(this.getByteField((short)2));
   }

   public final void setPresence(PresenceType presence) {
      this.setField((short)2, presence.value());
   }

   public final PresenceType getMsnPresence() {
      return PresenceType.fromValue(this.getByteField((short)3));
   }

   public final void setMsnPresence(PresenceType msnPresence) {
      this.setField((short)3, msnPresence.value());
   }

   public final PresenceType getAimPresence() {
      return PresenceType.fromValue(this.getByteField((short)4));
   }

   public final void setAimPresence(PresenceType aimPresence) {
      this.setField((short)4, aimPresence.value());
   }

   public final PresenceType getYahooPresence() {
      return PresenceType.fromValue(this.getByteField((short)5));
   }

   public final void setYahooPresence(PresenceType yahooPresence) {
      this.setField((short)5, yahooPresence.value());
   }

   public final PresenceType getIcqPresence() {
      return PresenceType.fromValue(this.getByteField((short)6));
   }

   public final void setIcqPresence(PresenceType icqPresence) {
      this.setField((short)6, icqPresence.value());
   }

   public final PresenceType getGtalkPresence() {
      return PresenceType.fromValue(this.getByteField((short)7));
   }

   public final void setGtalkPresence(PresenceType gtalkPresence) {
      this.setField((short)7, gtalkPresence.value());
   }

   public final PresenceType getFacebookPresence() {
      return PresenceType.fromValue(this.getByteField((short)8));
   }

   public final void setFacebookPresence(PresenceType facebookPresence) {
      this.setField((short)8, facebookPresence.value());
   }

   public final ImType[] getImTypeList() {
      return ImType.fromByteArrayValues(this.getByteArrayField((short)9));
   }

   public final void setImTypeList(ImType[] imTypeList) {
      this.setByteEnumArrayField((short)9, imTypeList);
   }

   public final PresenceType[] getImPresenceList() {
      return PresenceType.fromByteArrayValues(this.getByteArrayField((short)10));
   }

   public final void setImPresenceList(PresenceType[] imPresenceList) {
      this.setByteEnumArrayField((short)10, imPresenceList);
   }
}
