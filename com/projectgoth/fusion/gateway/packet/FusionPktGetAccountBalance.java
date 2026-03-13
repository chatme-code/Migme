package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetAccountBalance;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktGetAccountBalance extends FusionPktDataGetAccountBalance {
   public FusionPktGetAccountBalance(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetAccountBalance(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         AccountBalanceData balance = accountEJB.getAccountBalance(connection.getUsername());
         FusionPktAccountBalance balancePkt = new FusionPktAccountBalance(this.transactionId);
         if (connection.isAjax()) {
            balancePkt.setAccountBalance(balance.format());
         } else {
            try {
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               String infoText = misEJB.getInfoText(9);
               UserData userData = new UserData(connection.getUserPrx().getUserData());
               infoText = infoText.replaceAll("%b", "%s").replaceAll("%m", userData.mobilePhone).replaceAll("%u", userData.username);
               infoText = String.format(infoText, balance.format());
               balancePkt.setAccountBalance(infoText);
            } catch (Exception var8) {
               balancePkt.setAccountBalance(balance.format());
            }
         }

         return new FusionPacket[]{balancePkt};
      } catch (CreateException var9) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create AccountEJB");
         return new FusionPacket[]{pktError};
      } catch (RemoteException var10) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Get account balance failed- " + RMIExceptionHelper.getRootMessage(var10));
         return new FusionPacket[]{pktError};
      }
   }
}
