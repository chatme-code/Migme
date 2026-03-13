package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import java.util.Date;
import javax.ejb.CreateException;

public class FusionPktGetPreloginMarketingMsg extends FusionRequest {
   public FusionPktGetPreloginMarketingMsg() {
      super((short)931);
   }

   public FusionPktGetPreloginMarketingMsg(short transactionId) {
      super((short)931, transactionId);
   }

   public FusionPktGetPreloginMarketingMsg(FusionPacket packet) {
      super(packet);
   }

   public Byte getClientType() {
      return this.getByteField((short)1);
   }

   public void setClientType(byte clientType) {
      this.setField((short)1, clientType);
   }

   public Short getClientVersion() {
      return this.getShortField((short)2);
   }

   public void setClientVersion(short clientVersion) {
      this.setField((short)2, clientVersion);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         String offlineMessage = connection.getGateway().getOfflineMessage();
         if (offlineMessage != null && offlineMessage.length() > 0) {
            return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, offlineMessage)};
         } else {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            AlertMessageData messageData = userEJB.getLatestAlertMessage(this.getClientVersion(), AlertMessageData.TypeEnum.PRELOGIN, 0, (Date)null, (AlertContentType)null, this.getClientType());
            if (messageData == null) {
               return new FusionPacket[]{new FusionPktOk(this.transactionId)};
            } else {
               FusionPktPreloginMarketingMsg marketingPkt = new FusionPktPreloginMarketingMsg(this.transactionId);
               marketingPkt.setMarketingMsg(messageData.content);
               if (messageData.contentType != AlertContentType.TEXT) {
                  marketingPkt.setURL(messageData.url);
               }

               return new FusionPacket[]{marketingPkt};
            }
         }
      } catch (CreateException var6) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create UserEJB");
         return new FusionPacket[]{pktError};
      } catch (RemoteException var7) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get marketing message - " + RMIExceptionHelper.getRootMessage(var7));
         return new FusionPacket[]{pktError};
      }
   }
}
