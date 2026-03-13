package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.Voice;
import com.projectgoth.fusion.interfaces.VoiceHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktAnonymousCallResponse extends FusionRequest {
   public FusionPktAnonymousCallResponse() {
      super((short)807);
   }

   public FusionPktAnonymousCallResponse(short transactionId) {
      super((short)807, transactionId);
   }

   public FusionPktAnonymousCallResponse(FusionPacket packet) {
      super(packet);
   }

   public String getRequestingUsername() {
      return this.getStringField((short)1);
   }

   public void setRequestingUsername(String requestingUsername) {
      this.setField((short)1, requestingUsername);
   }

   public Byte getResponse() {
      return this.getByteField((short)2);
   }

   public void setResponse(byte response) {
      this.setField((short)2, response);
   }

   public Byte getBlockRequestingUser() {
      return this.getByteField((short)3);
   }

   public void setBlockRequestingUser(byte blockRequestingUser) {
      this.setField((short)3, blockRequestingUser);
   }

   public Byte getDisableAnonymousCalling() {
      return this.getByteField((short)4);
   }

   public void setDisableAnonymousCalling(byte disableAnonymousCalling) {
      this.setField((short)4, disableAnonymousCalling);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String requestingUsername = this.getRequestingUsername();
         if (requestingUsername == null) {
            throw new Exception("Requesting username not specified");
         } else {
            String source = connection.clearPendingAnonymousCallRequest(requestingUsername);
            if (source == null) {
               throw new Exception("Invalid requesting username");
            } else {
               Byte block = this.getBlockRequestingUser();
               if (block != null && block.intValue() == 1) {
                  Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
                  contactEJB.blockContact(connection.getUserID(), connection.getUsername(), requestingUsername);
               }

               Byte disableAnonymousCalling = this.getDisableAnonymousCalling();
               if (disableAnonymousCalling != null && disableAnonymousCalling.intValue() == 1) {
                  User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                  userEJB.updateAnonymousCallSetting(connection.getUsername(), UserSettingData.AnonymousCallEnum.DISABLED);
               }

               Byte response = this.getResponse();
               if (response != null && response.intValue() == 1) {
                  String destination = connection.getUserPrx().getUserData().mobilePhone;
                  CallData callData = new CallData();
                  callData.username = requestingUsername;
                  callData.source = source;
                  callData.sourceType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
                  callData.destination = destination;
                  callData.destinationType = CallData.SourceDestinationTypeEnum.PSTN_PHONE;
                  callData.type = CallData.TypeEnum.MIDLET_ANONYMOUS_CALLBACK;
                  Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
                  voiceEJB.initiatePhoneCall(callData);
               }

               try {
                  MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                  String responseMessage;
                  if (response != null && response.intValue() == 1) {
                     responseMessage = misEJB.getInfoText(45);
                     if (responseMessage == null) {
                        responseMessage = "%d has accepted your anonymous call request.";
                     }
                  } else {
                     responseMessage = misEJB.getInfoText(46);
                     if (responseMessage == null) {
                        responseMessage = "%d has rejected your anonymous call request.";
                     }
                  }

                  UserPrx requestingUserPrx = connection.findRegistry().findUserObject(requestingUsername);
                  if (requestingUserPrx != null) {
                     requestingUserPrx.putAlertMessage(responseMessage.replaceAll("%d", connection.getUsername()), (String)null, (short)0);
                  }
               } catch (Exception var10) {
               }

               return (new FusionPktOk(this.transactionId)).toArray();
            }
         }
      } catch (CreateException var11) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to respond to anonymous call request - Failed to create VoiceEJB")).toArray();
      } catch (RemoteException var12) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to respond to anonymous call request - " + RMIExceptionHelper.getRootMessage(var12))).toArray();
      } catch (Exception var13) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to respond to anonymous call request - " + var13.getMessage())).toArray();
      }
   }
}
