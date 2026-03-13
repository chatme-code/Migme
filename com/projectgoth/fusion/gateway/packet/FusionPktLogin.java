package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataLogin;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

public class FusionPktLogin extends FusionPktDataLogin {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLogin.class));
   private static final String LOGIN_DISABLED = "You cannot login at this time, please try again";

   public FusionPktLogin() {
   }

   public FusionPktLogin(short transactionId) {
      super(transactionId);
   }

   public FusionPktLogin(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktLogin(FusionPacket packet) {
      super(packet);
   }

   public int getInitialPresenceValue() {
      PresenceType initialPresence = this.getInitialPresence();
      return initialPresence == null ? PresenceType.AVAILABLE.value() : initialPresence.value();
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError errPkt;
      try {
         String offlineMessage = connection.getGateway().getOfflineMessage();
         if (offlineMessage != null && offlineMessage.length() > 0) {
            throw new Exception(offlineMessage);
         } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_CONCURRENT_SESSIONS_BREACHED_LOCKDOWN_ENABLED) && null != MemCachedClientWrapper.get(MemCachedKeySpaces.RateLimitKeySpace.MAX_CONCURRENT_SESSIONS_BREACHED, this.getUsername())) {
            errPkt = new FusionPktError();
            errPkt.setErrorDescription("You cannot login at this time, please try again");
            return new FusionPacket[]{errPkt};
         } else if (connection.getSessionPrx() != null) {
            throw new Exception("Session already exists");
         } else {
            ClientType clientType = this.getClientType();
            if (clientType == null) {
               throw new Exception("Unspecified client type");
            } else {
               Short clientVersion = this.getClientVersion();
               if (clientVersion == null) {
                  throw new Exception("Unspecified client version");
               } else if ((clientType == ClientType.MIDP1 || clientType == ClientType.MIDP2) && !connection.getGateway().verifyMidletVersion(clientVersion)) {
                  return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.INVALID_VERSION, 15)};
               } else {
                  Short protocol = this.getProtocolVersion();
                  if (protocol != null && connection.getGateway().verifyProtocolVersion(protocol.intValue())) {
                     connection = connection.onLogin(this);
                     FusionPktLoginChallenge loginChallenge = new FusionPktLoginChallenge(this.transactionId);
                     loginChallenge.setChallenge(connection.getLoginChallenge());
                     String sessionIDToReturn = null;
                     if (SSOLogin.isEncryptedSessionIDSupported(clientType.value(), clientVersion)) {
                        sessionIDToReturn = connection.getEncryptedSessionID();
                     } else {
                        sessionIDToReturn = connection.getSessionID();
                     }

                     loginChallenge.setSessionId(sessionIDToReturn);
                     return new FusionPacket[]{loginChallenge};
                  } else {
                     return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNSUPPORTED_PROTOCOL, "Protocol version " + protocol + " is no longer supported.")};
                  }
               }
            }
         }
      } catch (Exception var8) {
         connection.onSessionTerminated();
         errPkt = new FusionPktError(this.transactionId);
         errPkt.setErrorDescription("Unable to login at this time. Please try again later");
         log.error("Login error for [" + this.getUsername() + "] [" + this.getUserAgent() + "] :" + var8.getMessage(), var8);
         return new FusionPacket[]{errPkt};
      }
   }
}
