package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.ContactList;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktHaveLatestContactListOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktHaveLatestContactListOld.class));

   public FusionPktHaveLatestContactListOld() {
      super((short)419);
   }

   public FusionPktHaveLatestContactListOld(short transactionId) {
      super((short)419, transactionId);
   }

   public FusionPktHaveLatestContactListOld(FusionPacket packet) {
      super(packet);
   }

   public Long getStatusTimestamp() {
      return this.getLongField((short)1);
   }

   public void setStatusTimestamp(String statusTimestamp) {
      this.setField((short)1, statusTimestamp);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         Long statusTimestamp = this.getStatusTimestamp();
         if (statusTimestamp == null) {
            throw new Exception("Status timestamp not specified");
         } else {
            statusTimestamp = statusTimestamp - 300000L;
            UserPrx userPrx = connection.getUserPrx();
            if (userPrx == null) {
               FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "User not logged in");
               return new FusionPacket[]{pktError};
            } else {
               ContactList contactList = userPrx.getContactList();
               List<FusionPacket> fusionPkts = new LinkedList();
               fusionPkts.add(new FusionPktOk(this.transactionId));
               ContactDataIce[] arr$ = contactList.contacts;
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  ContactDataIce contact = arr$[i$];
                  ContactData contactData = new ContactData(contact);
                  if (contactData.isOtherIMOnly()) {
                     fusionPkts.add(new FusionPktContactOld(this.transactionId, contactData, connection));
                  } else {
                     if (contactData.isOnline()) {
                        fusionPkts.add(new FusionPktPresenceOld(this.transactionId, contactData, connection));
                     }

                     long contactStatusTimestamp = 0L;
                     if (contactData.statusTimeStamp != null) {
                        contactStatusTimestamp = contactData.statusTimeStamp.getTime();
                     }

                     if (contactStatusTimestamp > statusTimestamp) {
                        FusionPktStatusMessageOld statusMessage = new FusionPktStatusMessageOld();
                        statusMessage.setContactID(contactData.id);
                        statusMessage.setStatusTimeStamp(System.currentTimeMillis());
                        if (contactData.statusMessage != null) {
                           statusMessage.setStatusMessage(contactData.statusMessage);
                        }

                        fusionPkts.add(statusMessage);
                        FusionPktDisplayPictureOld displayPicture = new FusionPktDisplayPictureOld(this.transactionId);
                        displayPicture.setContactID(contactData.id);
                        displayPicture.setStatusTimeStamp(System.currentTimeMillis());
                        if (contactData.displayPicture != null) {
                           displayPicture.setDisplayPicture(contactData.displayPicture);
                        }

                        fusionPkts.add(displayPicture);
                     }
                  }
               }

               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               FusionPktServerQuestionOld serverQuestion = connection.getServerQuestionOld();
               if (serverQuestion != null) {
                  fusionPkts.add(serverQuestion);
               }

               if (serverQuestion == null && contactList.contacts.length < 2) {
                  String infoText = misEJB.getInfoText(40);
                  if (infoText != null) {
                     FusionPktAlertOld pktAlert = new FusionPktAlertOld(this.transactionId);
                     pktAlert.setAlertType((short)1);
                     pktAlert.setContentType((byte)1);
                     pktAlert.setContent(infoText);
                     fusionPkts.add(pktAlert);
                  }
               }

               Iterator i$ = connection.getMidletTabsAfterLogin().iterator();

               while(i$.hasNext()) {
                  FusionPktMidletTab pktMidletTab = (FusionPktMidletTab)i$.next();
                  pktMidletTab.setTransactionId(this.getTransactionId());
                  fusionPkts.add(pktMidletTab);
               }

               connection.clearMidletTabAfterLogin();
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
                  MessageSwitchboardPrx msp = connection.getGatewayContext().getRegistryPrx().getMessageSwitchboard();
                  msp.onLogon(connection.getUserID(), connection.getSessionPrx(), this.transactionId, connection.getUsername());
               } else {
                  MessageSwitchboardI msi = new MessageSwitchboardI();
                  msi.onLogon(connection.getUserID(), connection.getSessionPrx(), this.transactionId, connection.getUsername());
               }

               return (FusionPacket[])fusionPkts.toArray(new FusionPacket[fusionPkts.size()]);
            }
         }
      } catch (CreateException var15) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create MIS EJB")).toArray();
      } catch (RemoteException var16) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var16.getClass().getName() + ":" + RMIExceptionHelper.getRootMessage(var16))).toArray();
      } catch (LocalException var17) {
         return (new FusionPktInternalServerError(this.transactionId, var17, "Contact list check failed")).toArray();
      } catch (Exception var18) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var18.getMessage())).toArray();
      }
   }
}
