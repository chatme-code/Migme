package com.projectgoth.fusion.clientsession;

import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.restapi.data.SSOEnums;

public class SSOLoginSessionInfo {
   public SSOEnums.View view;
   public Byte clientType;
   public Short clientVersion;
   public String sessionID;
   public Integer initialPresence;
   public String remoteAddress;
   public String mobileDevice;
   public String userAgent;
   public String language;
   public ConnectionI connection;

   public SSOLoginSessionInfo(SSOEnums.View view, Byte clientType, Short clientVersion, String sessionID, Integer initialPresence, String remoteAddress, String mobileDevice, String userAgent, String language, ConnectionI connection) {
      this.view = view;
      this.clientType = clientType;
      this.clientVersion = clientVersion;
      this.sessionID = sessionID;
      this.initialPresence = initialPresence;
      this.remoteAddress = remoteAddress;
      this.mobileDevice = mobileDevice;
      this.userAgent = userAgent;
      this.language = language;
      this.connection = connection;
   }

   public byte getClientType() {
      return this.clientType == null ? 0 : this.clientType;
   }

   public short getClientVersion() {
      return this.clientVersion == null ? 0 : this.clientVersion;
   }
}
