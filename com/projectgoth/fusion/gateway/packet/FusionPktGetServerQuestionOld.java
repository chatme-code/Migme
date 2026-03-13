package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.Date;
import org.apache.log4j.Logger;

public class FusionPktGetServerQuestionOld extends FusionRequest {
   private static Logger log = Logger.getLogger(FusionPktGetServerQuestionOld.class);

   public FusionPktGetServerQuestionOld() {
      super((short)18);
   }

   public FusionPktGetServerQuestionOld(short transactionId) {
      super((short)18, transactionId);
   }

   public FusionPktGetServerQuestionOld(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         UserPrx userPrx = connection.getUserPrx();
         if (userPrx == null) {
            throw new Exception("You are no longer logged in");
         } else {
            UserDataIce userData = userPrx.getUserData();
            AlertMessageData alertMessage = userEJB.getLatestAlertMessage(connection.getClientVersion(), AlertMessageData.TypeEnum.LOGIN, userData.countryID, userData.lastLoginDate == Long.MIN_VALUE ? null : new Date(userData.lastLoginDate), (AlertContentType)null, connection.getDeviceType().value());
            return alertMessage != null ? new FusionPacket[]{new FusionPktServerQuestionOld(alertMessage, this.transactionId)} : new FusionPacket[]{new FusionPktServerQuestionOld(this.transactionId)};
         }
      } catch (Exception var6) {
         log.error(String.format("Failed to retrieve server question for transactionid:%s", this.transactionId), var6);
         FusionPktError error = new FusionPktError(this.transactionId);
         error.setErrorDescription("Failed to retrieve server question");
         return new FusionPacket[]{error};
      }
   }
}
