package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktGetContactRequests extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetContactRequests.class));

   public FusionPktGetContactRequests() {
      super((short)425);
   }

   public FusionPktGetContactRequests(short transactionId) {
      super((short)425, transactionId);
   }

   public FusionPktGetContactRequests(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return true;
   }

   public FusionPacket[] processRequest(ConnectionI connection) {
      ArrayList packetsToReturn = new ArrayList();

      try {
         log.info("FusionPktGetContactRequests [" + connection.getUsername() + "]");
         boolean pushNewFollowersAsPendingContacts = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.PUSH_NEW_FOLLOWERS_AS_PENDING_CONTACTS_ENABLED);
         int maxPendingContacts = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.MAX_PENDING_CONTACTS_TO_RETRIEVE);
         String url = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.USERPROFILE_URL);
         Set<String> userList = new HashSet();
         Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
         Set<String> pendingContacts = contactEJB.getPendingContacts(connection.getUsername());
         if (pendingContacts != null && pendingContacts.size() > 0) {
            userList.addAll(pendingContacts);
         }

         if (pushNewFollowersAsPendingContacts && userList.size() < maxPendingContacts) {
            Set<String> newFollowerUsernames = contactEJB.getRecentFollowers(connection.getUserID());
            if (newFollowerUsernames != null && newFollowerUsernames.size() > 0) {
               userList.addAll(newFollowerUsernames);
            }
         }

         Iterator i$ = userList.iterator();

         while(i$.hasNext()) {
            String username = (String)i$.next();
            if (packetsToReturn.size() == maxPendingContacts) {
               break;
            }

            FusionPktContactRequest contactRequestPacket = new FusionPktContactRequest(username, url + username);
            contactRequestPacket.setTransactionId(this.transactionId);
            packetsToReturn.add(contactRequestPacket);
         }

         log.info("FusionPktGetContactRequests [" + connection.getUsername() + "] returning [" + packetsToReturn.size() + "]");
         return (FusionPacket[])packetsToReturn.toArray(new FusionPacket[packetsToReturn.size()]);
      } catch (CreateException var12) {
         log.error("Unable to create bean", var12);
         return (new FusionPktInternalServerError(this.transactionId, var12, "Unable to get contact requests")).toArray();
      } catch (RemoteException var13) {
         log.error("Remote exception: " + var13.getClass().getName(), var13);
         return (new FusionPktInternalServerError(this.transactionId, var13, "Unable to get contact requests")).toArray();
      } catch (NoSuchFieldException var14) {
         log.error(var14.getMessage());
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get contact requests.")).toArray();
      } catch (Exception var15) {
         log.error(var15.getMessage());
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get contact requests.")).toArray();
      }
   }
}
