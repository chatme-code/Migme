package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.clientsession.SSOLogoutClientContext;
import com.projectgoth.fusion.clientsession.SSOLogoutSessionInfo;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.packets.FusionPktDataLogout;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

public class FusionPktLogout extends FusionPktDataLogout implements SSOLogoutClientContext {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLogout.class));

   public FusionPktLogout() {
   }

   public FusionPktLogout(short transactionId) {
      super(transactionId);
   }

   public FusionPktLogout(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktLogout(FusionPacket packet) {
      super(packet);
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
