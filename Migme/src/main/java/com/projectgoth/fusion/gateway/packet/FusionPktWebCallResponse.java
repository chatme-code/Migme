package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Voice;
import com.projectgoth.fusion.interfaces.VoiceHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;

public class FusionPktWebCallResponse extends FusionRequest {
   public FusionPktWebCallResponse() {
      super((short)803);
   }

   public FusionPktWebCallResponse(short transactionId) {
      super((short)803, transactionId);
   }

   public FusionPktWebCallResponse(FusionPacket packet) {
      super(packet);
   }

   public String getSource() {
      return this.getStringField((short)1);
   }

   public void setSource(String source) {
      this.setField((short)1, source);
   }

   public Integer getGateway() {
      return this.getIntField((short)2);
   }

   public void setGateway(int gateway) {
      this.setField((short)2, gateway);
   }

   public String getGatewayName() {
      return this.getStringField((short)3);
   }

   public void setGatewayName(String gatewayName) {
      this.setField((short)3, gatewayName);
   }

   public Byte getSourceProtocol() {
      return this.getByteField((short)4);
   }

   public void setSourceProtocol(byte sourceProtocol) {
      this.setField((short)4, sourceProtocol);
   }

   public Byte getDestinationProtocol() {
      return this.getByteField((short)5);
   }

   public void setDestinationProtocol(byte destinationProtocol) {
      this.setField((short)5, destinationProtocol);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      String errorMessage;
      try {
         String source = this.getSource();
         if (source != null && connection.clearPendingWebCallRequest(source)) {
            Integer gateway = this.getGateway();
            if (gateway == null) {
               throw new Exception("Gateway not specified");
            }

            CallData.ProtocolEnum sourceProtocol = CallData.ProtocolEnum.IAX2;
            Byte byteVal = this.getSourceProtocol();
            if (byteVal != null) {
               sourceProtocol = CallData.ProtocolEnum.fromValue(byteVal.intValue());
               if (sourceProtocol == null) {
                  throw new Exception("Invalid source protocol");
               }
            }

            CallData.ProtocolEnum destinationProtocol = CallData.ProtocolEnum.IAX2;
            byteVal = this.getDestinationProtocol();
            if (byteVal != null) {
               destinationProtocol = CallData.ProtocolEnum.fromValue(byteVal.intValue());
               if (destinationProtocol == null) {
                  throw new Exception("Invalid destination protocol");
               }
            }

            CallData callData = new CallData();
            callData.username = source;
            callData.source = callData.username;
            callData.sourceType = CallData.SourceDestinationTypeEnum.MIG33_USER;
            callData.sourceProtocol = sourceProtocol;
            callData.destination = connection.getUsername();
            callData.destinationType = CallData.SourceDestinationTypeEnum.MIG33_USER;
            callData.destinationProtocol = destinationProtocol;
            callData.gateway = gateway;
            callData.type = CallData.TypeEnum.TOOLBAR_CALL;
            Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
            voiceEJB.initiatePhoneCall(callData);
            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         }

         return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Invalid request")};
      } catch (RemoteException var11) {
         errorMessage = "Failed to initiate a call to " + connection.getUsername() + " - " + RMIExceptionHelper.getRootMessage(var11);
      } catch (Exception var12) {
         errorMessage = "Failed to initiate a call to " + connection.getUsername() + " - " + var12.getMessage();
      }

      try {
         RegistryPrx registryPrx = connection.findRegistry();
         if (registryPrx != null) {
            UserPrx userPrx = registryPrx.findUserObject(this.getSource());
            if (userPrx != null) {
               userPrx.putAlertMessage(errorMessage, (String)null, (short)0);
            }
         }
      } catch (Exception var10) {
      }

      return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, errorMessage)};
   }
}
