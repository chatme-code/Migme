package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataActivateAccount;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktActivateAccount extends FusionPktDataActivateAccount {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktActivateAccount.class));

   public FusionPktActivateAccount(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktActivateAccount(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         userEJB.activateAccount(connection.getUsername(), this.getVerificationCode(), false, new AccountEntrySourceData(connection));

         try {
            Web webEJB = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
            if (webEJB.isIndosatIP(connection.getRemoteAddress())) {
               webEJB.joinGroup(connection.getUsername(), 20, 0, connection.getRemoteAddress(), connection.getSessionID(), connection.getMobileDevice(), connection.getUsername(), false, true, true, false, false, false);
            }
         } catch (Exception var4) {
            log.warn("Unable to join " + connection.getUsername() + " to Indosat group", var4);
         }

         FusionPktOk pktOk = new FusionPktOk(this.transactionId, 8);
         return pktOk.toArray();
      } catch (CreateException var5) {
         return (new FusionPktInternalServerError(this.transactionId, var5, "Authentication unsuccessful")).toArray();
      } catch (RemoteException var6) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Authentication unsuccessful. " + RMIExceptionHelper.getRootMessage(var6))).toArray();
      }
   }
}
