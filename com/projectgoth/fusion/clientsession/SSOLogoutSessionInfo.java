package com.projectgoth.fusion.clientsession;

import com.projectgoth.fusion.gateway.ConnectionI;

public class SSOLogoutSessionInfo {
   public String sessionID;
   public ConnectionI connection;

   public SSOLogoutSessionInfo(String sessionID, ConnectionI connection) {
      this.sessionID = sessionID;
      this.connection = connection;
   }
}
