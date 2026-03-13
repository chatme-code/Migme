package com.projectgoth.fusion.sessioncache;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.slice.SessionIce;
import com.projectgoth.fusion.slice.SessionMetricsIce;
import java.util.Date;

public class SessionArchiveDetail {
   private int userID;
   private String username;
   private String language;
   private int sourceCountryID;
   private boolean authenticated;
   private ClientType deviceType;
   private Enums.ConnectionEnum connectionType;
   private int port;
   private int remotePort;
   private String remoteAddress;
   private String mobileDevice;
   private short clientVersion;
   private Date startDateTime;
   private Date endDateTime;
   private int migLevel;
   private int migLevelScore;
   private SessionMetricsIce sessionMetrics;

   public SessionArchiveDetail(SessionIce session, SessionMetricsIce sessionMetrics) {
      this.userID = session.userID;
      this.username = session.username;
      this.language = session.language;
      this.sourceCountryID = session.sourceCountryID;
      this.authenticated = session.authenticated;
      this.deviceType = ClientType.fromValue(session.deviceType);
      this.connectionType = Enums.ConnectionEnum.fromValue(session.connectionType);
      this.port = session.port;
      this.remotePort = session.remotePort;
      this.remoteAddress = session.ipAddress;
      this.mobileDevice = session.mobileDevice;
      this.clientVersion = session.clientVersion;
      this.startDateTime = new Date(session.startDateTime);
      this.endDateTime = new Date(session.endDateTime);
      this.sessionMetrics = sessionMetrics;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getLanguage() {
      return this.language;
   }

   public void setLanguage(String language) {
      this.language = language;
   }

   public int getSourceCountryID() {
      return this.sourceCountryID;
   }

   public void setSourceCountryID(int sourceCountryID) {
      this.sourceCountryID = sourceCountryID;
   }

   public boolean isAuthenticated() {
      return this.authenticated;
   }

   public void setAuthenticated(boolean authenticated) {
      this.authenticated = authenticated;
   }

   public ClientType getDeviceType() {
      return this.deviceType;
   }

   public void setDeviceType(ClientType deviceType) {
      this.deviceType = deviceType;
   }

   public Enums.ConnectionEnum getConnectionType() {
      return this.connectionType;
   }

   public void setConnectionType(Enums.ConnectionEnum connectionType) {
      this.connectionType = connectionType;
   }

   public String getRemoteAddress() {
      return this.remoteAddress;
   }

   public void setRemoteAddress(String ipAddress) {
      this.remoteAddress = ipAddress;
   }

   public short getClientVersion() {
      return this.clientVersion;
   }

   public void setClientVersion(short clientVersion) {
      this.clientVersion = clientVersion;
   }

   public Date getStartDateTime() {
      return this.startDateTime;
   }

   public void setStartDateTime(Date startDateTime) {
      this.startDateTime = startDateTime;
   }

   public Date getEndDateTime() {
      return this.endDateTime;
   }

   public void setEndDateTime(Date endDateTime) {
      this.endDateTime = endDateTime;
   }

   public SessionMetricsIce getSessionMetrics() {
      return this.sessionMetrics;
   }

   public void setSessionMetrics(SessionMetricsIce sessionMetrics) {
      this.sessionMetrics = sessionMetrics;
   }

   public int getPort() {
      return this.port;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public int getRemotePort() {
      return this.remotePort;
   }

   public String getMobileDevice() {
      return this.mobileDevice;
   }

   public void setMobileDevice(String mobileDevice) {
      this.mobileDevice = mobileDevice;
   }

   public int getUserID() {
      return this.userID;
   }

   public int getMigLevel() {
      return this.migLevel;
   }

   public int getMigLevelScore() {
      return this.migLevelScore;
   }

   public void setMigLevel(int migLevel) {
      this.migLevel = migLevel;
   }

   public void setMigLevelScore(int migLevelScore) {
      this.migLevelScore = migLevelScore;
   }

   public String toString() {
      return StringUtil.join((Object[])(new String[]{String.format("userID=%d", this.userID), String.format("username='%s'", this.username), String.format("sourceCountryID=%d", this.sourceCountryID), String.format("authenticated=%s", this.authenticated ? "true" : "false"), String.format("deviceType=%s(%d)", this.deviceType == null ? "null" : this.deviceType.name(), this.deviceType == null ? 0 : this.deviceType.value()), String.format("connectionType=%s(%d)", this.connectionType == null ? "null" : this.connectionType.name(), this.connectionType == null ? 0 : this.connectionType.value()), String.format("port=%d", this.port), String.format("remotePort=%d", this.remotePort), String.format("remoteAddress='%s'", this.remoteAddress), String.format("mobileDevice='%s'", this.mobileDevice), String.format("clientVersion=%d", this.clientVersion), String.format("startDateTime='%s'", this.startDateTime), String.format("endDateTime='%s'", this.endDateTime), String.format("migLevel=%d", this.migLevel), String.format("migLevelScore=%d", this.migLevelScore), String.format("uniqueUsersPrivateChattedWith=%d", this.sessionMetrics.uniqueUsersPrivateChattedWith), String.format("privateMessagesSent=%d", this.sessionMetrics.privateMessagesSent), String.format("groupMessagesSent=%d", this.sessionMetrics.groupMessagesSent), String.format("groupChatsEntered=%d", this.sessionMetrics.groupChatsEntered), String.format("chatroomMessagesSent=%d", this.sessionMetrics.chatroomMessagesSent), String.format("chatroomsEntered=%d", this.sessionMetrics.chatroomsEntered), String.format("uniqueChatroomsEntered=%d", this.sessionMetrics.uniqueChatroomsEntered), String.format("inviteByPhoneNumber=%d", this.sessionMetrics.inviteByPhoneNumber), String.format("inviteByUsername=%d", this.sessionMetrics.inviteByUsername), String.format("themeUpdated=%d", this.sessionMetrics.themeUpdated), String.format("statusMessagesSet=%d", this.sessionMetrics.statusMessagesSet), String.format("profileEdited=%d", this.sessionMetrics.profileEdited), String.format("photosUploaded=%d", this.sessionMetrics.photosUploaded)}), ", ");
   }
}
