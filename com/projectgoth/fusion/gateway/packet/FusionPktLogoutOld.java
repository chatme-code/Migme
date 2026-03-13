package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.clientsession.SSOLogoutClientContext;
import com.projectgoth.fusion.clientsession.SSOLogoutSessionInfo;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import org.apache.log4j.Logger;

public class FusionPktLogoutOld extends FusionRequest implements SSOLogoutClientContext {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLogoutOld.class));

   public FusionPktLogoutOld() {
      super((short)300);
   }

   public FusionPktLogoutOld(short transactionId) {
      super((short)300, transactionId);
   }

   public FusionPktLogoutOld(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      SSOLogoutSessionInfo sessionInfo = new SSOLogoutSessionInfo(connection.getSessionID(), connection);
      SSOLogin.doLogout(sessionInfo, this);
      return null;
   }

   public void postLogout(SSOLogoutSessionInfo sessionInfo) {
      if (sessionInfo.connection != null) {
         sessionInfo.connection.onSessionTerminated();
      }

   }
}
