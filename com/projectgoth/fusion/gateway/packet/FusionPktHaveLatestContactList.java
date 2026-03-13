package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataAlert;
import com.projectgoth.fusion.fdl.packets.FusionPktDataHaveLatestContactList;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.ContactList;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktHaveLatestContactList extends FusionPktDataHaveLatestContactList {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktHaveLatestContactList.class));

   public FusionPktHaveLatestContactList() {
   }

   public FusionPktHaveLatestContactList(short transactionId) {
      super(transactionId);
   }

   public FusionPktHaveLatestContactList(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktHaveLatestContactList(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         Long statusTimestamp = this.getTimestamp();
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
                     fusionPkts.add(new FusionPktContact(this.transactionId, contactData, connection));
                  } else {
                     if (contactData.isOnline()) {
                        fusionPkts.add(new FusionPktPresence(this.transactionId, contactData, connection));
                     }

                     long contactStatusTimestamp = 0L;
                     if (contactData.statusTimeStamp != null) {
                        contactStatusTimestamp = contactData.statusTimeStamp.getTime();
                     }

                     if (contactStatusTimestamp > statusTimestamp) {
                        FusionPktStatusMessage statusMessage = new FusionPktStatusMessage();
                        statusMessage.setContactId(contactData.id);
                        statusMessage.setTimestamp(System.currentTimeMillis());
                        if (contactData.statusMessage != null) {
                           statusMessage.setStatusMessage(contactData.statusMessage);
                        }

                        fusionPkts.add(statusMessage);
                        FusionPktDisplayPicture displayPicture = new FusionPktDisplayPicture(this.transactionId);
                        displayPicture.setContactId(contactData.id);
                        displayPicture.setTimestamp(System.currentTimeMillis());
                        if (contactData.displayPicture != null) {
                           displayPicture.setDisplayPictureGuid(contactData.displayPicture);
                        }

                        fusionPkts.add(displayPicture);
                     }
                  }
               }

               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               FusionPktServerQuestion serverQuestion = connection.getServerQuestion();
               if (serverQuestion != null) {
                  fusionPkts.add(serverQuestion);
               }

               if (serverQuestion == null && contactList.contacts.length < 2) {
                  String infoText = misEJB.getInfoText(40);
                  if (infoText != null) {
                     FusionPktAlert pktAlert = new FusionPktAlert(this.transactionId);
                     pktAlert.setAlertType(FusionPktDataAlert.AlertType.INFORMATION);
                     pktAlert.setContentType(AlertContentType.TEXT);
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
