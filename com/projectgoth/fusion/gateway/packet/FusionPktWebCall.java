package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.FloodControl;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.Voice;
import com.projectgoth.fusion.interfaces.VoiceHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;

public class FusionPktWebCall extends FusionRequest {
   private static final Logger auditLog = Logger.getLogger("VoiceAudit");

   public FusionPktWebCall() {
      super((short)801);
   }

   public FusionPktWebCall(short transactionId) {
      super((short)801, transactionId);
   }

   public FusionPktWebCall(FusionPacket packet) {
      super(packet);
   }

   public Byte getDestinationType() {
      return this.getByteField((short)1);
   }

   public void setDestinationType(byte destinationType) {
      this.setField((short)1, destinationType);
   }

   public String getDestination() {
      return this.getStringField((short)2);
   }

   public void setDestination(String destination) {
      this.setField((short)2, destination);
   }

   public Integer getGateway() {
      return this.getIntField((short)3);
   }

   public void setGateway(int gateway) {
      this.setField((short)3, gateway);
   }

   public String getGatewayName() {
      return this.getStringField((short)4);
   }

   public void setGatewayName(String gatewayName) {
      this.setField((short)4, gatewayName);
   }

   public Byte getEvaluate() {
      return this.getByteField((short)5);
   }

   public void setEvaluate(byte evaluate) {
      this.setField((short)5, evaluate);
   }

   public Byte getProtocol() {
      return this.getByteField((short)6);
   }

   public void setProtocol(byte protocol) {
      this.setField((short)6, protocol);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         Byte destinationType = this.getDestinationType();
         if (destinationType == null) {
            throw new Exception("Destination type not specified");
         } else {
            String destination = this.getDestination();
            if (destination == null) {
               throw new Exception("Destination not specified");
            } else {
               Integer gateway = this.getGateway();
               if (gateway == null) {
                  throw new Exception("Gateway not specified");
               } else {
                  String gatewayName = this.getGatewayName();
                  if (gatewayName == null) {
                     throw new Exception("Gateway name not specified");
                  } else {
                     CallData.ProtocolEnum protocol = CallData.ProtocolEnum.IAX2;
                     Byte byteVal = this.getProtocol();
                     if (byteVal != null) {
                        protocol = CallData.ProtocolEnum.fromValue(byteVal.intValue());
                     }

                     if (protocol == null) {
                        throw new Exception("Invalid protocol");
                     } else {
                        try {
                           FloodControl.detectFlooding(connection.getUsername(), connection.getUserPrx(), new FloodControl.Action[]{FloodControl.Action.PHONE_CALL.setMaxHits(SystemProperty.getLong("PhoneCallUserPerSecondRateLimit", 3L))});
                        } catch (Exception var15) {
                           auditLog.info(connection.getUsername() + ", user disconnected and suspended for 1 hour, exceeded 3/second rate limit. Destination[" + destination + "] from source[" + connection.getUsername() + "]");
                           throw var15;
                        }

                        if (MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.PHONECALL.toString(), MemCachedKeyUtils.getFullKeyFromStrings("PS", "DEST", destination), SystemProperty.getLong("PhoneCallDestinationPerSecondRateLimit", 1L), 1000L) && MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.PHONECALL.toString(), MemCachedKeyUtils.getFullKeyFromStrings("PH", "DEST", destination), SystemProperty.getLong("PhoneCallDestinationPerHourRateLimit", 60L), 3600000L)) {
                           Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
                           CallData callData = new CallData();
                           callData.username = callData.source = connection.getUsername();
                           callData.sourceType = CallData.SourceDestinationTypeEnum.MIG33_USER;
                           callData.sourceProtocol = protocol;
                           callData.destination = destination;
                           callData.gateway = gateway;
                           callData.type = CallData.TypeEnum.TOOLBAR_CALL;
                           callData.destinationType = destinationType == 1 ? CallData.SourceDestinationTypeEnum.MIG33_USER : CallData.SourceDestinationTypeEnum.PSTN_PHONE;
                           UserPrx userPrx = null;
                           if (callData.destinationType == CallData.SourceDestinationTypeEnum.MIG33_USER) {
                              RegistryPrx registryPrx = connection.findRegistry();
                              if (registryPrx == null) {
                                 throw new Exception("Unable to locate registry");
                              }

                              userPrx = registryPrx.findUserObject(destination);
                              if (userPrx == null) {
                                 throw new Exception("Unable to locate user " + destination);
                              }
                           }

                           FusionPktOk okPkt = new FusionPktOk(this.transactionId);
                           Byte evaluate = this.getEvaluate();
                           if (evaluate != null && evaluate == 1) {
                              callData = voiceEJB.evaluatePhoneCall(callData);
                              Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
                              CurrencyData userCurrency = accountEJB.getUsersLocalCurrency(connection.getUsername());
                              if (callData.destinationType == CallData.SourceDestinationTypeEnum.PSTN_PHONE) {
                                 callData.destination = "+" + callData.destination;
                              }

                              okPkt.setServerResponse("A call to " + callData.destination + " will cost you " + userCurrency.format(userCurrency.convertFromBaseCurrency(callData.rate)) + " per minute.\r\n\r\nDo you want to proceed?");
                           } else if (callData.destinationType == CallData.SourceDestinationTypeEnum.MIG33_USER) {
                              callData = voiceEJB.evaluatePhoneCall(callData);
                              userPrx.putWebCallNotification(callData.username, callData.destination, callData.gateway, gatewayName, callData.sourceProtocol.value());
                           } else {
                              voiceEJB.initiatePhoneCall(callData);
                           }

                           return new FusionPacket[]{okPkt};
                        } else if (SystemProperty.getBool("SuspendPhoneCallDestinationRateLimitOffender", false)) {
                           auditLog.info(connection.getUsername() + ", user disconnected and suspended for 1 hour. Destination Rate Limit exceeded. Destination [" + destination + "]");
                           connection.getUserPrx().disconnectFlooder("Flooding. Broke Phone Call Destination Rate Limit. Destination [" + destination + "]");
                           throw new Exception("You have been disconnected.");
                        } else {
                           auditLog.info(connection.getUsername() + ", call dropped, exceeded rate limit to destination[" + destination + "] from source[" + connection.getUsername() + "]");
                           return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "System busy. Please try again later.")).toArray();
                        }
                     }
                  }
               }
            }
         }
      } catch (RemoteException var16) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to initiate a call - " + RMIExceptionHelper.getRootMessage(var16));
         return new FusionPacket[]{pktError};
      } catch (ObjectNotFoundException var17) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to initiate a call - " + this.getDestination() + " is currently offline");
         return new FusionPacket[]{pktError};
      } catch (FusionException var18) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to initiate a call - " + var18.message);
         return new FusionPacket[]{pktError};
      } catch (LocalException var19) {
         return (new FusionPktInternalServerError(this.transactionId, var19, "Failed to initiate a call")).toArray();
      } catch (Exception var20) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to initiate a call - " + var20.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
