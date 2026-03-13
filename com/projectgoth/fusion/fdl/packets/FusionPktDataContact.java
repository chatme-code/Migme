package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PhoneNumberType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataContact extends FusionPacket {
   public FusionPktDataContact() {
      super(PacketType.CONTACT);
   }

   public FusionPktDataContact(short transactionId) {
      super(PacketType.CONTACT, transactionId);
   }

   public FusionPktDataContact(FusionPacket packet) {
      super(packet);
   }

   public final Integer getContactId() {
      return this.getIntField((short)1);
   }

   public final void setContactId(int contactId) {
      this.setField((short)1, contactId);
   }

   public final Integer getContactGroupId() {
      return this.getIntField((short)2);
   }

   public final void setContactGroupId(int contactGroupId) {
      this.setField((short)2, contactGroupId);
   }

   public final String getDisplayName() {
      return this.getStringField((short)3);
   }

   public final void setDisplayName(String displayName) {
      this.setField((short)3, displayName);
   }

   public final String getFirstName() {
      return this.getStringField((short)4);
   }

   public final void setFirstName(String firstName) {
      this.setField((short)4, firstName);
   }

   public final String getLastName() {
      return this.getStringField((short)5);
   }

   public final void setLastName(String lastName) {
      this.setField((short)5, lastName);
   }

   public final String getEmailAddress() {
      return this.getStringField((short)6);
   }

   public final void setEmailAddress(String emailAddress) {
      this.setField((short)6, emailAddress);
   }

   public final PhoneNumberType getDefaultPhone() {
      return PhoneNumberType.fromValue(this.getByteField((short)7));
   }

   public final void setDefaultPhone(PhoneNumberType defaultPhone) {
      this.setField((short)7, defaultPhone.value());
   }

   public final String getMobilePhoneNumber() {
      return this.getStringField((short)8);
   }

   public final void setMobilePhoneNumber(String mobilePhoneNumber) {
      this.setField((short)8, mobilePhoneNumber);
   }

   public final String getHomePhoneNumber() {
      return this.getStringField((short)9);
   }

   public final void setHomePhoneNumber(String homePhoneNumber) {
      this.setField((short)9, homePhoneNumber);
   }

   public final String getOfficePhoneNumber() {
      return this.getStringField((short)10);
   }

   public final void setOfficePhoneNumber(String officePhoneNumber) {
      this.setField((short)10, officePhoneNumber);
   }

   public final ImType getDefaultIm() {
      return ImType.fromValue(this.getByteField((short)11));
   }

   public final void setDefaultIm(ImType defaultIm) {
      this.setField((short)11, defaultIm.value());
   }

   public final String getFusionUsername() {
      return this.getStringField((short)12);
   }

   public final void setFusionUsername(String fusionUsername) {
      this.setField((short)12, fusionUsername);
   }

   public final PresenceType getFusionPresence() {
      return PresenceType.fromValue(this.getByteField((short)13));
   }

   public final void setFusionPresence(PresenceType fusionPresence) {
      this.setField((short)13, fusionPresence.value());
   }

   public final String getMsnUsername() {
      return this.getStringField((short)14);
   }

   public final void setMsnUsername(String msnUsername) {
      this.setField((short)14, msnUsername);
   }

   public final PresenceType getMsnPresence() {
      return PresenceType.fromValue(this.getByteField((short)15));
   }

   public final void setMsnPresence(PresenceType msnPresence) {
      this.setField((short)15, msnPresence.value());
   }

   public final String getAimUsername() {
      return this.getStringField((short)16);
   }

   public final void setAimUsername(String aimUsername) {
      this.setField((short)16, aimUsername);
   }

   public final PresenceType getAimPresence() {
      return PresenceType.fromValue(this.getByteField((short)17));
   }

   public final void setAimPresence(PresenceType aimPresence) {
      this.setField((short)17, aimPresence.value());
   }

   public final String getYahooUsername() {
      return this.getStringField((short)18);
   }

   public final void setYahooUsername(String yahooUsername) {
      this.setField((short)18, yahooUsername);
   }

   public final PresenceType getYahooPresence() {
      return PresenceType.fromValue(this.getByteField((short)19));
   }

   public final void setYahooPresence(PresenceType yahooPresence) {
      this.setField((short)19, yahooPresence.value());
   }

   public final String getIcqUsername() {
      return this.getStringField((short)20);
   }

   public final void setIcqUsername(String icqUsername) {
      this.setField((short)20, icqUsername);
   }

   public final PresenceType getIcqPresence() {
      return PresenceType.fromValue(this.getByteField((short)21));
   }

   public final void setIcqPresence(PresenceType icqPresence) {
      this.setField((short)21, icqPresence.value());
   }

   public final String getGtalkUsername() {
      return this.getStringField((short)22);
   }

   public final void setGtalkUsername(String gtalkUsername) {
      this.setField((short)22, gtalkUsername);
   }

   public final PresenceType getGtalkPresence() {
      return PresenceType.fromValue(this.getByteField((short)23));
   }

   public final void setGtalkPresence(PresenceType gtalkPresence) {
      this.setField((short)23, gtalkPresence.value());
   }

   public final Boolean getShareMobilePhone() {
      return this.getBooleanField((short)24);
   }

   public final void setShareMobilePhone(boolean shareMobilePhone) {
      this.setField((short)24, shareMobilePhone);
   }

   public final Boolean getIsVoiceCapable() {
      return this.getBooleanField((short)25);
   }

   public final void setIsVoiceCapable(boolean isVoiceCapable) {
      this.setField((short)25, isVoiceCapable);
   }

   public final String getStatusMessage() {
      return this.getStringField((short)27);
   }

   public final void setStatusMessage(String statusMessage) {
      this.setField((short)27, statusMessage);
   }

   public final String getDisplayPictureGuid() {
      return this.getStringField((short)28);
   }

   public final void setDisplayPictureGuid(String displayPictureGuid) {
      this.setField((short)28, displayPictureGuid);
   }

   public final String getFacebookUsername() {
      return this.getStringField((short)29);
   }

   public final void setFacebookUsername(String facebookUsername) {
      this.setField((short)29, facebookUsername);
   }

   public final PresenceType getFacebookPresence() {
      return PresenceType.fromValue(this.getByteField((short)30));
   }

   public final void setFacebookPresence(PresenceType facebookPresence) {
      this.setField((short)30, facebookPresence.value());
   }

   public final ImType[] getImTypeList() {
      return ImType.fromByteArrayValues(this.getByteArrayField((short)31));
   }

   public final void setImTypeList(ImType[] imTypeList) {
      this.setByteEnumArrayField((short)31, imTypeList);
   }

   public final PresenceType[] getImPresenceList() {
      return PresenceType.fromByteArrayValues(this.getByteArrayField((short)32));
   }

   public final void setImPresenceList(PresenceType[] imPresenceList) {
      this.setByteEnumArrayField((short)32, imPresenceList);
   }

   public final String[] getImUsernameList() {
      return this.getStringArrayField((short)33);
   }

   public final void setImUsernameList(String[] imUsernameList) {
      this.setField((short)33, imUsernameList);
   }
}
