package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataImLogin;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice.UserPrxHelper;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

public class FusionPktImLogin extends FusionPktDataImLogin {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktImLogin.class));

   public FusionPktImLogin(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktImLogin(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      ImType imType = null;
      UserPrx userPrx = null;

      FusionPktError pktError;
      try {
         imType = this.getImType();
         if (imType == null) {
            throw new Exception("Unsupported IM type");
         } else {
            PresenceType presence = this.getInitialPresence();
            if (presence == null) {
               presence = PresenceType.AVAILABLE;
            }

            Boolean boolVal = this.getShowOfflineContacts();
            boolean showOfflineContacts = boolVal != null && boolVal;
            userPrx = connection.getUserPrx();
            if (userPrx == null) {
               throw new Exception("You are no longer logged in");
            } else {
               UserPrxHelper.checkedCast(userPrx.ice_timeout(connection.getGateway().getImLoginTimeout())).otherIMLogin(imType.value(), presence.value(), showOfflineContacts);
               return new FusionPacket[]{new FusionPktOk(this.transactionId)};
            }
         }
      } catch (LocalException var7) {
         log.error("An error occurred during IM login [IMTYPE:" + imType + "] [SID:" + connection.getSessionID() + "] userPrx=" + userPrx + "e=" + var7, var7);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to sign into " + imType);
         return new FusionPacket[]{pktError};
      } catch (FusionException var8) {
         log.error("An error occurred during IM login [IMTYPE:" + imType + "]", var8);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to sign into " + imType + " - " + var8.message);
         return new FusionPacket[]{pktError};
      } catch (Exception var9) {
         log.error("An error occurred during IM login [IMTYPE:" + imType + "]", var9);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to sign into other IM - " + var9.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
