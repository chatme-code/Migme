package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.Voice;
import com.projectgoth.fusion.interfaces.VoiceHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktCompareCallRate extends FusionRequest {
   public FusionPktCompareCallRate() {
      super((short)805);
   }

   public FusionPktCompareCallRate(short transactionId) {
      super((short)805, transactionId);
   }

   public FusionPktCompareCallRate(FusionPacket packet) {
      super(packet);
   }

   public String getSource() {
      return this.getStringField((short)1);
   }

   public void setSource(String source) {
      this.setField((short)1, source);
   }

   public String getDestination() {
      return this.getStringField((short)2);
   }

   public void setDestination(String destination) {
      this.setField((short)2, destination);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         UserData userData = userEJB.loadUser(connection.getUsername(), false, false);
         Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
         String fullDIDNumber = voiceEJB.getFullDIDNumber(userData.countryID);
         if (fullDIDNumber == null) {
            FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Call-through is not supported in your country");
            return new FusionPacket[]{pktError};
         } else {
            CallData callback = new CallData();
            callback.username = connection.getUsername();
            callback.source = this.getSource();
            callback.sourceType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
            callback.destination = this.getDestination();
            callback.destinationType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
            callback.type = CallData.TypeEnum.MIDLET_CALLBACK;
            CallData callThrough = new CallData();
            callThrough.username = connection.getUsername();
            callThrough.source = this.getSource();
            callThrough.sourceType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
            callThrough.destination = this.getDestination();
            callThrough.destinationType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
            callThrough.type = CallData.TypeEnum.MIDLET_CALL_THROUGH;
            callThrough.didNumber = fullDIDNumber;
            callback = voiceEJB.evaluatePhoneCall(callback);
            callThrough = voiceEJB.evaluatePhoneCall(callThrough);
            Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
            CurrencyData userCurrency = accountEJB.getUsersLocalCurrency(connection.getUsername());
            String infoText;
            if (callback.sourceIDDCode == 27) {
               infoText = "This call will be free if between South Africa and US, India or Pakistan - you pay for the call today and migme will fully refund the cost on August 1. Today, a callback will debit %r1 per minute and a call-through will debit %r2 per minute";
            } else {
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               infoText = misEJB.getInfoText(33);
            }

            infoText = infoText.replaceAll("%s", callback.source).replaceAll("%d", callback.destination).replaceAll("%r1", userCurrency.formatWithCode(userCurrency.convertFromBaseCurrency(callback.rate))).replaceAll("%f1", userCurrency.formatWithCode(userCurrency.convertFromBaseCurrency(callback.signallingFee))).replaceAll("%t1", String.valueOf(callback.maxCallDuration / 60)).replaceAll("%r2", userCurrency.formatWithCode(userCurrency.convertFromBaseCurrency(callThrough.rate))).replaceAll("%f2", userCurrency.formatWithCode(userCurrency.convertFromBaseCurrency(callThrough.signallingFee))).replaceAll("%t2", String.valueOf(callThrough.maxCallDuration / 60));
            FusionPktOk pkt = new FusionPktOk(this.transactionId);
            pkt.setServerResponse(infoText);
            return pkt.toArray();
         }
      } catch (CreateException var12) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to compare call rate - " + var12.getMessage())).toArray();
      } catch (RemoteException var13) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to compare call rate - " + RMIExceptionHelper.getRootMessage(var13))).toArray();
      }
   }
}
