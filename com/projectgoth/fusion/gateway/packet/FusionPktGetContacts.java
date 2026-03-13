package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataAlert;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetContacts;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.ContactGroupDataIce;
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

public class FusionPktGetContacts extends FusionPktDataGetContacts {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetContacts.class));

   public FusionPktGetContacts() {
   }

   public FusionPktGetContacts(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetContacts(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         UserPrx userPrx = connection.getUserPrx();
         if (userPrx == null) {
            FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "User not logged in");
            return new FusionPacket[]{pktError};
         } else {
            ContactList contactList = userPrx.getContactList();
            boolean defaultGroupPktRequired = !connection.isMidletVersionAndAbove(400);
            boolean contactGroupModified = false;
            int len$;
            int len$;
            if (this.getContactId() != null) {
               int contactId = this.getContactId();
               ContactDataIce[] arr$ = contactList.contacts;
               len$ = arr$.length;

               for(len$ = 0; len$ < len$; ++len$) {
                  ContactDataIce contact = arr$[len$];
                  if (contact.id == contactId) {
                     ContactData contactData = new ContactData(contact);
                     if (contactData.contactGroupId == null && defaultGroupPktRequired) {
                        contactData.contactGroupId = -1;
                     }

                     return new FusionPacket[]{new FusionPktContact(this.transactionId, contactData, connection)};
                  }
               }

               return null;
            } else {
               List<FusionPacket> fusionPkts = new LinkedList();
               ContactGroupDataIce[] arr$ = contactList.contactGroups;
               len$ = arr$.length;

               for(len$ = 0; len$ < len$; ++len$) {
                  ContactGroupDataIce group = arr$[len$];
                  fusionPkts.add(new FusionPktGroup(this.transactionId, new ContactGroupData(group)));
               }

               if (connection.isAjax() || connection.isMidletVersionAndAbove(440)) {
                  fusionPkts.add(new FusionPktGroup(this.transactionId, -3, "Facebook"));
                  fusionPkts.add(new FusionPktGroup(this.transactionId, -4, "Google Talk"));
                  fusionPkts.add(new FusionPktGroup(this.transactionId, -5, "MSN"));
                  fusionPkts.add(new FusionPktGroup(this.transactionId, -6, "Yahoo!"));
               }

               PresenceType presence = this.getPresence();
               ContactDataIce[] arr$ = contactList.contacts;
               len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  ContactDataIce contact = arr$[i$];
                  ContactData contactData = new ContactData(contact);
                  if (contactData.contactGroupId == null && defaultGroupPktRequired) {
                     contactData.contactGroupId = -1;
                     contactGroupModified = true;
                  }

                  if (presence != null) {
                     if (presence.isOnline() == contactData.isOnline()) {
                        fusionPkts.add(new FusionPktContact(this.transactionId, contactData, connection));
                     }
                  } else {
                     fusionPkts.add(new FusionPktContact(this.transactionId, contactData, connection));
                  }
               }

               if (defaultGroupPktRequired && (contactList.contactGroups.length == 0 || contactGroupModified)) {
                  fusionPkts.add(0, new FusionPktGroup(this.transactionId, -1, "migme"));
                  connection.setDefaultGroupPktSent(true);
               }

               FusionPktContactListVersion pktVersion = new FusionPktContactListVersion(this.transactionId);
               pktVersion.setVersion(contactList.version);
               pktVersion.setTimestamp(System.currentTimeMillis());
               fusionPkts.add(pktVersion);
               connection.getLastContactListVersionSent().set(contactList.version);
               fusionPkts.add(new FusionPktGetContactsComplete(this.transactionId));
               FusionPktServerQuestion serverQuestion = connection.getServerQuestion();
               if (serverQuestion != null) {
                  fusionPkts.add(serverQuestion);
               }

               if (connection.isMidletVersionAndAbove(400)) {
                  MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
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
               }

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
      } catch (CreateException var13) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create MIS EJB")).toArray();
      } catch (RemoteException var14) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var14.getClass().getName() + ":" + RMIExceptionHelper.getRootMessage(var14))).toArray();
      } catch (LocalException var15) {
         return (new FusionPktInternalServerError(this.transactionId, var15, "Failed to get contacts")).toArray();
      } catch (Exception var16) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var16.getMessage())).toArray();
      }
   }
}
