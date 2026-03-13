package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.SMSRouteData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class DispatchThread implements Runnable {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DispatchThread.class));
   private SMSEngine engine;
   private List<SMSMessage> messages;
   private Message messageEJB;

   public DispatchThread(SMSEngine engine, MessageData messageData) {
      this.engine = engine;
      this.messages = SMSMessage.parse(messageData);
   }

   public DispatchThread(SMSEngine engine, SystemSMSData systemSMSData) {
      this.engine = engine;
      this.messages = SMSMessage.parse(systemSMSData);
   }

   public void run() {
      try {
         this.messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         Iterator i$ = this.messages.iterator();

         while(true) {
            while(i$.hasNext()) {
               SMSMessage message = (SMSMessage)i$.next();
               Integer iddCode = message.getIDDCode();
               if (iddCode == null) {
                  log.warn("Invalid " + message.getShortDescription() + " - No IDD code");
               } else {
                  SMSRouteData.TypeEnum type = message.getType();
                  if (type == null) {
                     log.warn("Invalid " + message.getShortDescription() + " - SMS type not specified");
                  } else {
                     List<SMSRouteData> routes = RoutingTable.getRoutes(type, iddCode, message.getDestination());
                     if (routes != null && routes.size() != 0) {
                        if (!MessageHistory.add(message.getKey())) {
                           log.warn("Discarding duplicated " + message.getShortDescription());
                        } else {
                           Iterator<SMSRouteData> iterator = routes.iterator();
                           DispatchStatus result = new DispatchStatus(DispatchStatus.StatusEnum.TRY_OTHER_GATEWAYS, message);

                           while(iterator.hasNext()) {
                              SMSRouteData route = (SMSRouteData)iterator.next();
                              SMSGateway gateway = RoutingTable.getGateway(route.gatewayID);
                              if (gateway == null) {
                                 log.warn("Unable to locate SMS gateway " + route.gatewayID);
                              } else {
                                 try {
                                    result = gateway.dispatchMessage(message);
                                    if (result.status != DispatchStatus.StatusEnum.TRY_OTHER_GATEWAYS) {
                                       this.updateDispatchingStatus(message, gateway, result);
                                       break;
                                    }
                                 } catch (Exception var11) {
                                    log.error("exception while dispatching message", var11);
                                 }
                              }
                           }

                           if (result.status == DispatchStatus.StatusEnum.TRY_OTHER_GATEWAYS) {
                              MessageHistory.remove(message.getKey());
                           }
                        }
                     } else {
                        log.warn("No route(s) available for phone number " + message.getDestination());
                        this.updateDispatchingStatus(message, (SMSGateway)null, new DispatchStatus(DispatchStatus.StatusEnum.FAILED, message));
                     }
                  }
               }
            }

            return;
         }
      } catch (CreateException var12) {
         log.warn("Unable to create Message EJB - " + var12.getMessage(), var12);
      }
   }

   private void updateDispatchingStatus(SMSMessage message, SMSGateway gateway, DispatchStatus result) {
      try {
         SMSRouteData.TypeEnum type = message.getType();
         Integer gatewayID = gateway == null ? null : gateway.getID();
         if (result.status == DispatchStatus.StatusEnum.SUCCEEDED) {
            this.engine.onMessageSent();
            if (type == SMSRouteData.TypeEnum.USER_SMS) {
               this.messageEJB.changePendingMessageToSent(MessageType.SMS, message.getParentID(), message.getID(), gatewayID, message.getIDDCode(), message.getDestination(), result.transactionID);
            } else {
               this.messageEJB.changePendingSystemSMSToSent(message.getType().value(), message.getID(), gatewayID, result.source, message.getIDDCode(), message.getDestination(), result.transactionID, result.billed);
            }
         } else if (result.status == DispatchStatus.StatusEnum.FAILED) {
            if (type == SMSRouteData.TypeEnum.USER_SMS) {
               this.messageEJB.smsFailed(message.getID(), gatewayID, message.getUsername(), new AccountEntrySourceData(SMSEngine.class));
            } else {
               this.messageEJB.systemSMSFailed(message.getID(), gatewayID, result.source);
            }
         }
      } catch (Exception var6) {
         log.error("Failed to update dispatching status of " + message.getShortDescription() + " to " + result + " - " + var6.getMessage());
      }

   }
}
