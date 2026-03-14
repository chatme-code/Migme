package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktLoginOkOld extends FusionPacket {
   public FusionPktLoginOkOld() {
      super((short)203);
   }

   public FusionPktLoginOkOld(short transactionId) {
      super((short)203, transactionId);
   }

   public FusionPktLoginOkOld(FusionPacket packet) {
      super(packet);
   }

   public String getMobilePhone() {
      return this.getStringField((short)1);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)1, mobilePhone);
   }

   public Byte getMobileVerified() {
      return this.getByteField((short)2);
   }

   public void setMobileVerified(byte mobileVerified) {
      this.setField((short)2, mobileVerified);
   }

   public String getAlert() {
      return this.getStringField((short)3);
   }

   public void setAlert(String alert) {
      this.setField((short)3, alert);
   }

   public Byte getAlertContentType() {
      return this.getByteField((short)4);
   }

   public void setAlertContentType(byte alertContentType) {
      this.setField((short)4, alertContentType);
   }

   public String getAsteriskServers() {
      return this.getStringField((short)5);
   }

   public void setAsteriskServers(String asteriskServers) {
      this.setField((short)5, asteriskServers);
   }

   public Byte getMSNDetail() {
      return this.getByteField((short)6);
   }

   public void setMSNDetail(byte msnDetail) {
      this.setField((short)6, msnDetail);
   }

   public Byte getAIMDetail() {
      return this.getByteField((short)7);
   }

   public void setAIMDetail(byte aimDetail) {
      this.setField((short)7, aimDetail);
   }

   public Byte getYahooDetail() {
      return this.getByteField((short)8);
   }

   public void setYahooDetail(byte msnDetail) {
      this.setField((short)8, msnDetail);
   }

   public Integer getAsteriskId() {
      return this.getIntField((short)9);
   }

   public void setAsteriskId(int asteriskId) {
      this.setField((short)9, asteriskId);
   }

   public String getAsteriskServer() {
      return this.getStringField((short)10);
   }

   public void setAsteriskServer(String asteriskServer) {
      this.setField((short)10, asteriskServer);
   }

   public String getCurrency() {
      return this.getStringField((short)11);
   }

   public void setCurrency(String currency) {
      this.setField((short)11, currency);
   }

   public String getExchangeRate() {
      return this.getStringField((short)12);
   }

   public void setExchangeRate(String exchangeRate) {
      this.setField((short)12, exchangeRate);
   }

   public String getMailURL() {
      return this.getStringField((short)13);
   }

   public void setMailURL(String mailURL) {
      this.setField((short)13, mailURL);
   }

   public Integer getMailCount() {
      return this.getIntField((short)14);
   }

   public void setMailCount(int mailCount) {
      this.setField((short)14, mailCount);
   }

   public Byte getVOIPCodec() {
      return this.getByteField((short)15);
   }

   public void setVOIPCodec(byte voipCodec) {
      this.setField((short)15, voipCodec);
   }

   public Integer getEmoticonHeight() {
      return this.getIntField((short)16);
   }

   public void setEmoticonHeight(int emoticonHeight) {
      this.setField((short)16, emoticonHeight);
   }

   public Byte getGTalkDetail() {
      return this.getByteField((short)17);
   }

   public void setGTalkDetail(byte gtalkDetail) {
      this.setField((short)17, gtalkDetail);
   }

   public Byte getLocalDIDSupport() {
      return this.getByteField((short)18);
   }

   public void setLocalDIDSupport(byte localDIDSupport) {
      this.setField((short)18, localDIDSupport);
   }

   public Integer getContactListVersion() {
      return this.getIntField((short)20);
   }

   public void setContactListVersion(int contactListVersion) {
      this.setField((short)20, contactListVersion);
   }

   public String getImageServerURL() {
      return this.getStringField((short)23);
   }

   public void setImageServerURL(String imageServerURL) {
      this.setField((short)23, imageServerURL);
   }

   public Integer getUserEventsToKeep() {
      return this.getIntField((short)24);
   }

   public void setUserEventsToKeep(int userEventsToKeep) {
      this.setField((short)24, userEventsToKeep);
   }

   public Byte getAnonymousCalling() {
      return this.getByteField((short)25);
   }

   public void setAnonymousCalling(byte anonymousCalling) {
      this.setField((short)25, anonymousCalling);
   }

   public String getPageletURL() {
      return this.getStringField((short)26);
   }

   public void setPageletURL(String pageletURL) {
      this.setField((short)26, pageletURL);
   }

   public Byte getUserType() {
      return this.getByteField((short)27);
   }

   public void setUserType(byte userType) {
      this.setField((short)27, userType);
   }

   public String getBadgeHotKey() {
      return this.getStringField((short)28);
   }

   public void setBadgeHotKey(String badgeHotKey) {
      this.setField((short)28, badgeHotKey);
   }

   public Byte getSendConnectionReport() {
      return this.getByteField((short)29);
   }

   public void setSendConnectionReport(byte sendConnectionReport) {
      this.setField((short)29, sendConnectionReport);
   }

   public Byte getFacebookDetail() {
      return this.getByteField((short)30);
   }

   public void setFacebookDetail(byte facebookDetail) {
      this.setField((short)30, facebookDetail);
   }

   public Short getReputationLevel() {
      return this.getShortField((short)31);
   }

   public void setReputationLevel(short reputationLevel) {
      this.setField((short)31, reputationLevel);
   }

   public String getReputationImagePath() {
      return this.getStringField((short)32);
   }

   public void setReputationImagePath(String reputationImagePath) {
      this.setField((short)32, reputationImagePath);
   }

   public Byte getTCPTunnelling() {
      return this.getByteField((short)33);
   }

   public void setTCPTunnelling(byte sendConnectionReport) {
      this.setField((short)33, sendConnectionReport);
   }

   public Integer getFusionPresence() {
      return this.getIntField((short)34);
   }

   public void setFusionPresence(Integer fusionPresence) {
      this.setField((short)34, fusionPresence);
   }

   public Integer getUserID() {
      return this.getIntField((short)38);
   }

   public void setUserID(Integer userID) {
      this.setField((short)38, userID);
   }

   public String getUsername() {
      return this.getStringField((short)39);
   }

   public void setUsername(String username) {
      this.setField((short)39, username);
   }

   public Byte getSupportsChatSync() {
      return this.getByteField((short)40);
   }

   public void setSupportsChatSync(byte boolFlag) {
      this.setField((short)40, boolFlag);
   }

   public Integer getVirtualGiftSize() {
      return this.getIntField((short)41);
   }

   public void setVirtualGiftSize(int virtualGiftSize) {
      this.setField((short)41, virtualGiftSize);
   }

   public Byte getSupportsStickers() {
      return this.getByteField((short)42);
   }

   public void setSupportsStickers(byte boolFlag) {
      this.setField((short)42, boolFlag);
   }

   public Long getServerTime() {
      return this.getLongField((short)43);
   }

   public void setServerTime(long millis) {
      this.setField((short)43, millis);
   }
}
