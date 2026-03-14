package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.ImDetailType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.enums.UserType;
import com.projectgoth.fusion.fdl.enums.VoipCodecType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataLoginOk extends FusionPacket {
   public FusionPktDataLoginOk() {
      super(PacketType.LOGIN_OK);
   }

   public FusionPktDataLoginOk(short transactionId) {
      super(PacketType.LOGIN_OK, transactionId);
   }

   public FusionPktDataLoginOk(FusionPacket packet) {
      super(packet);
   }

   public final String getMobilePhone() {
      return this.getStringField((short)1);
   }

   public final void setMobilePhone(String mobilePhone) {
      this.setField((short)1, mobilePhone);
   }

   public final Boolean getMobileVerified() {
      return this.getBooleanField((short)2);
   }

   public final void setMobileVerified(boolean mobileVerified) {
      this.setField((short)2, mobileVerified);
   }

   public final String getAlert() {
      return this.getStringField((short)3);
   }

   public final void setAlert(String alert) {
      this.setField((short)3, alert);
   }

   public final AlertContentType getAlertContentType() {
      return AlertContentType.fromValue(this.getByteField((short)4));
   }

   public final void setAlertContentType(AlertContentType alertContentType) {
      this.setField((short)4, alertContentType.value());
   }

   public final String getAsteriskServers() {
      return this.getStringField((short)5);
   }

   public final void setAsteriskServers(String asteriskServers) {
      this.setField((short)5, asteriskServers);
   }

   public final ImDetailType getMsnDetail() {
      return ImDetailType.fromValue(this.getByteField((short)6));
   }

   public final void setMsnDetail(ImDetailType msnDetail) {
      this.setField((short)6, msnDetail.value());
   }

   public final ImDetailType getAimDetail() {
      return ImDetailType.fromValue(this.getByteField((short)7));
   }

   public final void setAimDetail(ImDetailType aimDetail) {
      this.setField((short)7, aimDetail.value());
   }

   public final ImDetailType getYahooDetail() {
      return ImDetailType.fromValue(this.getByteField((short)8));
   }

   public final void setYahooDetail(ImDetailType yahooDetail) {
      this.setField((short)8, yahooDetail.value());
   }

   public final Integer getAsteriskId() {
      return this.getIntField((short)9);
   }

   public final void setAsteriskId(int asteriskId) {
      this.setField((short)9, asteriskId);
   }

   public final String getAsteriskServer() {
      return this.getStringField((short)10);
   }

   public final void setAsteriskServer(String asteriskServer) {
      this.setField((short)10, asteriskServer);
   }

   public final String getCurrency() {
      return this.getStringField((short)11);
   }

   public final void setCurrency(String currency) {
      this.setField((short)11, currency);
   }

   public final String getExchangeRate() {
      return this.getStringField((short)12);
   }

   public final void setExchangeRate(String exchangeRate) {
      this.setField((short)12, exchangeRate);
   }

   public final String getMailUrl() {
      return this.getStringField((short)13);
   }

   public final void setMailUrl(String mailUrl) {
      this.setField((short)13, mailUrl);
   }

   public final Integer getMailCount() {
      return this.getIntField((short)14);
   }

   public final void setMailCount(int mailCount) {
      this.setField((short)14, mailCount);
   }

   public final VoipCodecType getVoipCodec() {
      return VoipCodecType.fromValue(this.getByteField((short)15));
   }

   public final void setVoipCodec(VoipCodecType voipCodec) {
      this.setField((short)15, voipCodec.value());
   }

   public final Integer getEmoticonHeight() {
      return this.getIntField((short)16);
   }

   public final void setEmoticonHeight(int emoticonHeight) {
      this.setField((short)16, emoticonHeight);
   }

   public final ImDetailType getGtalkDetail() {
      return ImDetailType.fromValue(this.getByteField((short)17));
   }

   public final void setGtalkDetail(ImDetailType gtalkDetail) {
      this.setField((short)17, gtalkDetail.value());
   }

   public final Boolean getLocalDidSupport() {
      return this.getBooleanField((short)18);
   }

   public final void setLocalDidSupport(boolean localDidSupport) {
      this.setField((short)18, localDidSupport);
   }

   public final Integer getContactListVersion() {
      return this.getIntField((short)20);
   }

   public final void setContactListVersion(int contactListVersion) {
      this.setField((short)20, contactListVersion);
   }

   public final String getImageServerUrl() {
      return this.getStringField((short)23);
   }

   public final void setImageServerUrl(String imageServerUrl) {
      this.setField((short)23, imageServerUrl);
   }

   public final Integer getUserEventsToKeep() {
      return this.getIntField((short)24);
   }

   public final void setUserEventsToKeep(int userEventsToKeep) {
      this.setField((short)24, userEventsToKeep);
   }

   public final Boolean getAnonymousCalling() {
      return this.getBooleanField((short)25);
   }

   public final void setAnonymousCalling(boolean anonymousCalling) {
      this.setField((short)25, anonymousCalling);
   }

   public final String getPageletUrl() {
      return this.getStringField((short)26);
   }

   public final void setPageletUrl(String pageletUrl) {
      this.setField((short)26, pageletUrl);
   }

   public final UserType getUserType() {
      return UserType.fromValue(this.getByteField((short)27));
   }

   public final void setUserType(UserType userType) {
      this.setField((short)27, userType.value());
   }

   public final String getBadgeHotkey() {
      return this.getStringField((short)28);
   }

   public final void setBadgeHotkey(String badgeHotkey) {
      this.setField((short)28, badgeHotkey);
   }

   public final Boolean getSendConnectionReport() {
      return this.getBooleanField((short)29);
   }

   public final void setSendConnectionReport(boolean sendConnectionReport) {
      this.setField((short)29, sendConnectionReport);
   }

   public final ImDetailType getFacebookDetail() {
      return ImDetailType.fromValue(this.getByteField((short)30));
   }

   public final void setFacebookDetail(ImDetailType facebookDetail) {
      this.setField((short)30, facebookDetail.value());
   }

   public final Short getReputationLevel() {
      return this.getShortField((short)31);
   }

   public final void setReputationLevel(short reputationLevel) {
      this.setField((short)31, reputationLevel);
   }

   public final String getReputationImagePath() {
      return this.getStringField((short)32);
   }

   public final void setReputationImagePath(String reputationImagePath) {
      this.setField((short)32, reputationImagePath);
   }

   public final Boolean getTcpTunneling() {
      return this.getBooleanField((short)33);
   }

   public final void setTcpTunneling(boolean tcpTunneling) {
      this.setField((short)33, tcpTunneling);
   }

   public final PresenceType getFusionPresence() {
      return PresenceType.fromValue(this.getByteField((short)34));
   }

   public final void setFusionPresence(PresenceType fusionPresence) {
      this.setField((short)34, fusionPresence.value());
   }

   public final Integer getUserId() {
      return this.getIntField((short)38);
   }

   public final void setUserId(int userId) {
      this.setField((short)38, userId);
   }

   public final String getUsername() {
      return this.getStringField((short)39);
   }

   public final void setUsername(String username) {
      this.setField((short)39, username);
   }

   public final Boolean getSupportsChatSync() {
      return this.getBooleanField((short)40);
   }

   public final void setSupportsChatSync(boolean supportsChatSync) {
      this.setField((short)40, supportsChatSync);
   }

   public final Integer getVirtualGiftSize() {
      return this.getIntField((short)41);
   }

   public final void setVirtualGiftSize(int virtualGiftSize) {
      this.setField((short)41, virtualGiftSize);
   }

   public final Boolean getSupportsStickers() {
      return this.getBooleanField((short)42);
   }

   public final void setSupportsStickers(boolean supportsStickers) {
      this.setField((short)42, supportsStickers);
   }

   public final Long getServerTime() {
      return this.getLongField((short)43);
   }

   public final void setServerTime(long serverTime) {
      this.setField((short)43, serverTime);
   }
}
