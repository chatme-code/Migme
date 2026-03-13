package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.ContactGroupDataIce;
import com.projectgoth.fusion.slice.ContactList;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.PresenceAndCapabilityIce;
import com.projectgoth.fusion.slice.UserErrorResponse;
import com.projectgoth.fusion.slice.UserPrx;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class ChatContacts {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatContacts.class));
   private final List<ContactGroupData> groupList;
   private final Set<ContactData> contactList;
   private int contactListVersion;
   private final Set<String> contactRequestSentList = new HashSet();
   private ChatUserData userData;
   private String username;
   private Integer userID;
   private boolean showOfflineMSNContacts = false;
   private boolean showOfflineYahooContacts = false;
   private boolean showOfflineAIMContacts = false;
   private boolean showOfflineGTalkContacts = false;
   private boolean showOfflineFacebookContacts = false;
   private Integer idIncrement = 0;
   private static UserErrorResponse USER_ERROR_RESPONSE_SILENT_FAIL;
   private static UserErrorResponse USER_ERROR_RESPONSE_OK;
   private static UserErrorResponse USER_ERROR_RESPONSE_FAIL_NOT_ONLINE;
   private static UserErrorResponse USER_ERROR_RESPONSE_FAIL_NOT_ACCEPTING_NOTIFICATIONS;
   private List<String> currentChatrooms = Collections.synchronizedList(new ArrayList());

   public ChatContacts(Contact contactEJB, ChatUserData userData) throws Exception {
      this.userData = userData;
      this.userID = userData.getUserID();
      this.username = userData.getUsername();
      List<ContactGroupData> groupList = null;
      Set<ContactData> contactList = null;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
         UserObject user = new UserObject(this.username);
         groupList = user.getGroupList();
         contactList = user.getContactList();
         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: get group list and contact list for user:%s, group list:%s, contact list:%s", user, groupList, contactList));
         }
      } else {
         if (contactEJB == null) {
            contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
         }

         if (contactEJB != null) {
            contactList = contactEJB.getContactList(this.username);
            groupList = contactEJB.getGroupList(this.username);
            this.contactListVersion = contactEJB.getContactListVersion(this.userID, (Connection)null);
         }

         if (contactList == null) {
            contactList = new HashSet();
         }

         if (groupList == null) {
            groupList = new LinkedList();
         }
      }

      this.groupList = (List)groupList;
      this.contactList = (Set)contactList;
      USER_ERROR_RESPONSE_OK = new UserErrorResponse((String)null, true, false);
      USER_ERROR_RESPONSE_SILENT_FAIL = new UserErrorResponse((String)null, true, true);
      USER_ERROR_RESPONSE_FAIL_NOT_ONLINE = new UserErrorResponse(this.username + " is not online", false, true);
      USER_ERROR_RESPONSE_FAIL_NOT_ACCEPTING_NOTIFICATIONS = new UserErrorResponse(this.username + " is not accepting messages", false, true);
   }

   public ContactData createContact(String username, String displayname, ImType imType) {
      ContactData contact = new ContactData();
      synchronized(this.idIncrement) {
         contact.id = this.idIncrement = this.idIncrement - 1;
      }

      contact.username = username;
      contact.displayName = displayname;
      if (imType == ImType.MSN) {
         contact.msnUsername = username;
         contact.defaultIM = ImType.MSN;
         contact.msnPresence = PresenceType.OFFLINE;
         contact.contactGroupId = -5;
      } else if (imType == ImType.YAHOO) {
         contact.yahooUsername = username;
         contact.defaultIM = ImType.YAHOO;
         contact.yahooPresence = PresenceType.OFFLINE;
         contact.contactGroupId = -6;
      } else if (imType == ImType.AIM) {
         contact.aimUsername = username;
         contact.defaultIM = ImType.AIM;
         contact.aimPresence = PresenceType.OFFLINE;
         contact.contactGroupId = -2;
      } else if (imType == ImType.GTALK) {
         contact.gtalkUsername = username;
         contact.defaultIM = ImType.GTALK;
         contact.gtalkPresence = PresenceType.OFFLINE;
         contact.contactGroupId = -4;
      } else if (imType == ImType.FACEBOOK) {
         contact.facebookUsername = username;
         contact.defaultIM = ImType.FACEBOOK;
         contact.facebookPresence = PresenceType.OFFLINE;
         contact.contactGroupId = -3;
      }

      contact.displayOnPhone = true;
      contact.status = ContactData.StatusEnum.ACTIVE;
      synchronized(this.contactList) {
         this.contactList.add(contact);
         return contact;
      }
   }

   public void addContact(ContactData contact, int contactListVersion) {
      synchronized(this.contactList) {
         this.contactList.add(contact);
         this.contactListVersion = contactListVersion;
      }
   }

   public void removeContact(ContactData contact) {
      synchronized(this.contactList) {
         this.contactList.remove(contact);
      }
   }

   public void removeContact(ContactData contact, int contactListVersion) {
      synchronized(this.contactList) {
         this.contactList.remove(contact);
         this.contactListVersion = contactListVersion;
      }
   }

   public ContactData removeContact(int contactId) {
      synchronized(this.contactList) {
         Iterator iterator = this.contactList.iterator();

         ContactData contactData;
         do {
            if (!iterator.hasNext()) {
               return null;
            }

            contactData = (ContactData)iterator.next();
         } while(contactData.id != contactId);

         iterator.remove();
         return contactData;
      }
   }

   public ContactData removeContact(String username) {
      synchronized(this.contactList) {
         Iterator iterator = this.contactList.iterator();

         ContactData contactData;
         do {
            if (!iterator.hasNext()) {
               return null;
            }

            contactData = (ContactData)iterator.next();
         } while(!StringUtils.hasLength(contactData.fusionUsername) || !contactData.fusionUsername.equals(username));

         iterator.remove();
         return contactData;
      }
   }

   public ArrayList<Integer> removeContacts(ImType type) {
      ArrayList<Integer> contactsRemoved = new ArrayList();
      synchronized(this.contactList) {
         Iterator i = this.contactList.iterator();

         while(true) {
            ContactData contact;
            do {
               if (!i.hasNext()) {
                  return contactsRemoved;
               }

               contact = (ContactData)i.next();
            } while((type != ImType.MSN || !contact.isMSNOnly()) && (type != ImType.YAHOO || !contact.isYahooOnly()) && (type != ImType.AIM || !contact.isAIMOnly()) && (type != ImType.GTALK || !contact.isGTalkOnly()) && (type != ImType.FACEBOOK || !contact.isFacebookOnly()));

            i.remove();
            contactsRemoved.add(contact.id);
         }
      }
   }

   public int getContactListVersion() {
      return this.contactListVersion;
   }

   public void setContactListVersion(int version) {
      synchronized(this.contactList) {
         this.contactListVersion = version;
      }
   }

   public ContactData getContact(String contactName) {
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         ContactData contact;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            contact = (ContactData)i$.next();
         } while(!contactName.equalsIgnoreCase(contact.fusionUsername));

         return contact;
      }
   }

   public ContactData getContact(int id) {
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         ContactData contact;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            contact = (ContactData)i$.next();
         } while(contact.id != id);

         return contact;
      }
   }

   public ContactData getContact(String username, ImType imType) {
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         ContactData existingContact;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            existingContact = (ContactData)i$.next();
         } while((imType != ImType.MSN || !username.equals(existingContact.msnUsername)) && (imType != ImType.YAHOO || !username.equals(existingContact.yahooUsername)) && (imType != ImType.AIM || !username.equals(existingContact.aimUsername)) && (imType != ImType.GTALK || !username.equals(existingContact.gtalkUsername)) && (imType != ImType.FACEBOOK || !username.equals(existingContact.facebookUsername)));

         return existingContact;
      }
   }

   public Integer findSourceContactID(MessageType messageType, String source) {
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            switch(messageType) {
            case FUSION:
               if (contact.fusionUsername != null && contact.fusionUsername.equals(source)) {
                  return contact.id;
               }
               break;
            case SMS:
               if (contact.mobilePhone != null && contact.mobilePhone.equals(source)) {
                  return contact.id;
               }
               break;
            case EMAIL:
               if (contact.emailAddress != null && contact.emailAddress.equals(source)) {
                  return contact.id;
               }
               break;
            case MSN:
               if (contact.msnUsername != null && contact.msnUsername.equals(source)) {
                  return contact.id;
               }
               break;
            case AIM:
               if (contact.aimUsername != null && contact.aimUsername.equals(source)) {
                  return contact.id;
               }
               break;
            case YAHOO:
               if (contact.yahooUsername != null && contact.yahooUsername.equals(source)) {
                  return contact.id;
               }
               break;
            case GTALK:
               if (contact.gtalkUsername != null && contact.gtalkUsername.equals(source)) {
                  return contact.id;
               }
               break;
            case FACEBOOK:
               if (contact.facebookUsername != null && contact.facebookUsername.equals(source)) {
                  return contact.id;
               }
            }
         }

         return null;
      }
   }

   public ContactDataIce[] getOnlineContacts() {
      ArrayList<ContactDataIce> contactDataIceArray = new ArrayList();
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if ((!contact.isMSNOnly() || this.showOfflineMSNContacts || contact.msnPresence != null && contact.msnPresence != PresenceType.OFFLINE) && (!contact.isYahooOnly() || this.showOfflineYahooContacts || contact.yahooPresence != null && contact.yahooPresence != PresenceType.OFFLINE) && (!contact.isAIMOnly() || this.showOfflineAIMContacts || contact.aimPresence != null && contact.aimPresence != PresenceType.OFFLINE) && (!contact.isGTalkOnly() || this.showOfflineGTalkContacts || contact.gtalkPresence != null && contact.gtalkPresence != PresenceType.OFFLINE) && (!contact.isFacebookOnly() || this.showOfflineFacebookContacts || contact.facebookPresence != null && contact.facebookPresence != PresenceType.OFFLINE)) {
               contactDataIceArray.add(contact.toIceObject());
            }
         }

         return (ContactDataIce[])contactDataIceArray.toArray(new ContactDataIce[contactDataIceArray.size()]);
      }
   }

   public ContactDataIce[] getContacts() {
      ArrayList<ContactDataIce> contactDataIceArray = new ArrayList();
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if ((!contact.isMSNOnly() || this.showOfflineMSNContacts || contact.msnPresence != null && contact.msnPresence != PresenceType.OFFLINE) && (!contact.isYahooOnly() || this.showOfflineYahooContacts || contact.yahooPresence != null && contact.yahooPresence != PresenceType.OFFLINE) && (!contact.isAIMOnly() || this.showOfflineAIMContacts || contact.aimPresence != null && contact.aimPresence != PresenceType.OFFLINE) && (!contact.isGTalkOnly() || this.showOfflineGTalkContacts || contact.gtalkPresence != null && contact.gtalkPresence != PresenceType.OFFLINE) && (!contact.isFacebookOnly() || this.showOfflineFacebookContacts || contact.facebookPresence != null && contact.facebookPresence != PresenceType.OFFLINE)) {
               contactDataIceArray.add(contact.toIceObject());
            }
         }

         return (ContactDataIce[])contactDataIceArray.toArray(new ContactDataIce[contactDataIceArray.size()]);
      }
   }

   public ArrayList<ContactData> getContacts(String source, ImType imTypeEnum) {
      ArrayList<ContactData> contacts = new ArrayList();
      Iterator i$;
      ContactData contact;
      switch(imTypeEnum) {
      case FUSION:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(i$.hasNext()) {
               contact = (ContactData)i$.next();
               if (source.equals(contact.fusionUsername)) {
                  contacts.add(contact);
               }
            }

            return contacts;
         }
      case MSN:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(i$.hasNext()) {
               contact = (ContactData)i$.next();
               if (source.equals(contact.msnUsername)) {
                  contacts.add(contact);
               }
            }

            return contacts;
         }
      case AIM:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(i$.hasNext()) {
               contact = (ContactData)i$.next();
               if (source.equals(contact.aimUsername)) {
                  contacts.add(contact);
               }
            }

            return contacts;
         }
      case YAHOO:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(i$.hasNext()) {
               contact = (ContactData)i$.next();
               if (source.equals(contact.yahooUsername)) {
                  contacts.add(contact);
               }
            }

            return contacts;
         }
      case GTALK:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(i$.hasNext()) {
               contact = (ContactData)i$.next();
               if (source.equals(contact.gtalkUsername)) {
                  contacts.add(contact);
               }
            }

            return contacts;
         }
      case FACEBOOK:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(i$.hasNext()) {
               contact = (ContactData)i$.next();
               if (source.equals(contact.facebookUsername)) {
                  contacts.add(contact);
               }
            }

            return contacts;
         }
      default:
         log.warn("getContacts() received invalid IM type '" + imTypeEnum.name() + "'");
         return null;
      }
   }

   public ContactDataIce[] getOtherIMContacts() {
      ArrayList<ContactDataIce> contactDataIceArray = new ArrayList();
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if (contact.isOtherIMOnly()) {
               contactDataIceArray.add(contact.toIceObject());
            }
         }

         return (ContactDataIce[])contactDataIceArray.toArray(new ContactDataIce[contactDataIceArray.size()]);
      }
   }

   public int getOnlineContactsCount() {
      int onlineContacts = 0;
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if (contact.isOnline()) {
               ++onlineContacts;
            }
         }

         return onlineContacts;
      }
   }

   public void contactChangedPresence(ImType imTypeEnum, String source, PresenceType presence, ArrayList<Integer> contactIDs, HashSet<ContactData> IMContactsNoLongerOffline) {
      Iterator i$;
      ContactData contact;
      switch(imTypeEnum) {
      case FUSION:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            do {
               if (!i$.hasNext()) {
                  return;
               }

               contact = (ContactData)i$.next();
            } while(!source.equals(contact.fusionUsername));

            contactIDs.add(contact.id);
            contact.fusionPresence = presence;
            return;
         }
      case MSN:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(true) {
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  contact = (ContactData)i$.next();
               } while(!source.equals(contact.msnUsername));

               if (!this.showOfflineMSNContacts && contact.isMSNOnly() && (contact.msnPresence == null || contact.msnPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
                  IMContactsNoLongerOffline.add(contact);
               }

               contactIDs.add(contact.id);
               contact.msnPresence = presence;
            }
         }
      case AIM:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(true) {
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  contact = (ContactData)i$.next();
               } while(!source.equals(contact.aimUsername));

               if (!this.showOfflineAIMContacts && contact.isAIMOnly() && (contact.aimPresence == null || contact.aimPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
                  IMContactsNoLongerOffline.add(contact);
               }

               contactIDs.add(contact.id);
               contact.aimPresence = presence;
            }
         }
      case YAHOO:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(true) {
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  contact = (ContactData)i$.next();
               } while(!source.equals(contact.yahooUsername));

               if (!this.showOfflineYahooContacts && contact.isYahooOnly() && (contact.yahooPresence == null || contact.yahooPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
                  IMContactsNoLongerOffline.add(contact);
               }

               contactIDs.add(contact.id);
               contact.yahooPresence = presence;
            }
         }
      case GTALK:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(true) {
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  contact = (ContactData)i$.next();
               } while(!source.equals(contact.gtalkUsername));

               if (!this.showOfflineGTalkContacts && contact.isGTalkOnly() && (contact.gtalkPresence == null || contact.gtalkPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
                  IMContactsNoLongerOffline.add(contact);
               }

               contactIDs.add(contact.id);
               contact.gtalkPresence = presence;
            }
         }
      case FACEBOOK:
         synchronized(this.contactList) {
            i$ = this.contactList.iterator();

            while(true) {
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  contact = (ContactData)i$.next();
               } while(!source.equals(contact.facebookUsername));

               if (!this.showOfflineFacebookContacts && contact.isFacebookOnly() && (contact.facebookPresence == null || contact.facebookPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
                  IMContactsNoLongerOffline.add(contact);
               }

               contactIDs.add(contact.id);
               contact.facebookPresence = presence;
            }
         }
      default:
         log.warn("ChatUser.contactChangedPresence() received invalid IM type '" + imTypeEnum + "'");
      }
   }

   public void setContactsPresence(ImType imType, PresenceType presence) {
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            this.setContactDataPresence(contact, imType, presence);
         }

      }
   }

   public List<String> setContactsPresenceOffline() {
      List<String> contactUsernames = new LinkedList();
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if (contact.fusionUsername != null) {
               contact.fusionPresence = PresenceType.OFFLINE;
               contactUsernames.add(contact.fusionUsername);
            }

            if (contact.msnUsername != null && contact.msnPresence == null) {
               contact.msnPresence = PresenceType.OFFLINE;
            }

            if (contact.yahooUsername != null && contact.yahooPresence == null) {
               contact.yahooPresence = PresenceType.OFFLINE;
            }

            if (contact.aimUsername != null && contact.aimPresence == null) {
               contact.aimPresence = PresenceType.OFFLINE;
            }

            if (contact.gtalkUsername != null && contact.gtalkPresence == null) {
               contact.gtalkPresence = PresenceType.OFFLINE;
            }

            if (contact.facebookUsername != null && contact.facebookPresence == null) {
               contact.facebookPresence = PresenceType.OFFLINE;
            }
         }

         return contactUsernames;
      }
   }

   public void setContactsDataPresence(ImType imType, PresenceType presence) {
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            this.setContactDataPresence(contact, imType, presence);
         }

      }
   }

   public void setContactDataPresence(ContactData contact, ImType imType, PresenceType presence) {
      switch(imType) {
      case FUSION:
         contact.fusionPresence = presence;
         break;
      case MSN:
         contact.msnPresence = presence;
         break;
      case AIM:
         contact.aimPresence = presence;
         break;
      case YAHOO:
         contact.yahooPresence = presence;
         break;
      case GTALK:
         contact.gtalkPresence = presence;
         break;
      case FACEBOOK:
         contact.facebookPresence = presence;
      }

   }

   public ContactGroupDataIce[] getGroups() {
      synchronized(this.groupList) {
         ContactGroupDataIce[] contactGroupDataIceArray = new ContactGroupDataIce[this.groupList.size()];

         for(int i = 0; i < this.groupList.size(); ++i) {
            contactGroupDataIceArray[i] = ((ContactGroupData)this.groupList.get(i)).toIceObject();
         }

         return contactGroupDataIceArray;
      }
   }

   public void assignPresence(Map<String, UserPrx> contactUserProxies) {
      if (contactUserProxies != null) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();

            try {
               UserPrx contactUserPrx = (UserPrx)contactUserProxies.get(contact.fusionUsername);
               if (contactUserPrx != null) {
                  contact.fusionPresence = PresenceType.fromValue(contactUserPrx.getOverallFusionPresence(this.username));
                  if (log.isDebugEnabled()) {
                     log.debug("User " + this.username + ": Contact=" + contact.fusionUsername + " has presence=" + contact.fusionPresence);
                  }
               }
            } catch (Exception var5) {
               if (log.isDebugEnabled()) {
                  log.debug("ChatContacts.assignPresence: unable to determine presence for " + contact.fusionUsername + " due to " + var5);
               }
            }
         }

      }
   }

   public boolean getIsContactWithPresence(ImType imTypeEnum, ContactData contact, PresenceType presence) {
      switch(imTypeEnum) {
      case FUSION:
         return true;
      case MSN:
         if (this.showOfflineMSNContacts && contact.isMSNOnly() && (contact.msnPresence == null || contact.msnPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
            return true;
         }
         break;
      case AIM:
         if (!this.showOfflineAIMContacts && contact.isAIMOnly() && (contact.aimPresence == null || contact.aimPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
            return true;
         }
         break;
      case YAHOO:
         if (!this.showOfflineYahooContacts && contact.isYahooOnly() && (contact.yahooPresence == null || contact.yahooPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
            return true;
         }
         break;
      case GTALK:
         if (!this.showOfflineGTalkContacts && contact.isGTalkOnly() && (contact.gtalkPresence == null || contact.gtalkPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
            return true;
         }
         break;
      case FACEBOOK:
         if (!this.showOfflineFacebookContacts && contact.isFacebookOnly() && (contact.facebookPresence == null || contact.facebookPresence == PresenceType.OFFLINE) && presence != PresenceType.OFFLINE) {
            return true;
         }
         break;
      default:
         log.warn("invalid IM type '" + imTypeEnum + "'");
         return false;
      }

      return false;
   }

   public boolean getIsContactShown(ContactData contact, ImType imType) {
      return imType == ImType.MSN && this.showOfflineMSNContacts && contact.isMSNOnly() || imType == ImType.YAHOO && this.showOfflineYahooContacts && contact.isYahooOnly() || imType == ImType.AIM && this.showOfflineAIMContacts && contact.isAIMOnly() || imType == ImType.GTALK && this.showOfflineGTalkContacts && contact.isGTalkOnly() || imType == ImType.FACEBOOK && this.showOfflineFacebookContacts && contact.isFacebookOnly();
   }

   public boolean isOnContactList(String contactUsername) {
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         ContactData contact;
         do {
            if (!i$.hasNext()) {
               return false;
            }

            contact = (ContactData)i$.next();
         } while(!contactUsername.equalsIgnoreCase(contact.fusionUsername));

         return true;
      }
   }

   public boolean isOnBlockList(String contactUsername) {
      return this.userData.isOnBlockList(contactUsername);
   }

   public Integer blockUser(String blockUsername, int contactListVersion) {
      Integer contactID = this.findSourceContactID(MessageType.FUSION, blockUsername);
      this.removeContact(blockUsername);
      this.userData.blockUser(blockUsername);
      synchronized(this.contactList) {
         if (contactID != null) {
            this.contactListVersion = contactListVersion;
         }

         return contactID;
      }
   }

   public boolean unblockUser(String username) {
      return this.userData.unblockUser(username);
   }

   public UserErrorResponse userCanContactMe(String username, MessageDataIce message, boolean silencedNotifications) {
      if (this.userData.isOnBlockList(username)) {
         return USER_ERROR_RESPONSE_FAIL_NOT_ONLINE;
      } else {
         if (silencedNotifications && !this.isOnContactList(username)) {
            if (message == null) {
               return USER_ERROR_RESPONSE_FAIL_NOT_ACCEPTING_NOTIFICATIONS;
            }

            if (!MessageData.isMessageToAChatRoom(message)) {
               return USER_ERROR_RESPONSE_FAIL_NOT_ACCEPTING_NOTIFICATIONS;
            }
         }

         return this.userData.getMessageSetting() == UserSettingData.MessageEnum.FRIENDS_ONLY && message != null && !this.userData.isOnBroadcastList(message.source) && MessageData.isMessageToAnIndividual(message) ? USER_ERROR_RESPONSE_FAIL_NOT_ACCEPTING_NOTIFICATIONS : USER_ERROR_RESPONSE_OK;
      }
   }

   public ContactDataIce assignPresence(ContactDataIce contactIce) {
      ContactData contact = this.getContact(contactIce.id);
      if (contact == null) {
         log.warn("contact id " + contactIce.id + " not found in contact list for user [" + this.username + "]");
         return contactIce;
      } else {
         if (contact.fusionPresence != null) {
            contactIce.fusionPresence = contact.fusionPresence.value();
         }

         if (contact.msnPresence != null) {
            contactIce.msnPresence = contact.msnPresence.value();
         }

         if (contact.aimPresence != null) {
            contactIce.aimPresence = contact.aimPresence.value();
         }

         if (contact.yahooPresence != null) {
            contactIce.yahooPresence = contact.yahooPresence.value();
         }

         if (contact.gtalkPresence != null) {
            contactIce.gtalkPresence = contact.gtalkPresence.value();
         }

         if (contact.facebookPresence != null) {
            contactIce.facebookPresence = contact.facebookPresence.value();
         }

         return contactIce;
      }
   }

   public boolean assignPresence(PresenceAndCapabilityIce presence, int contactId) {
      boolean found = false;
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if (contact.id == contactId) {
               if (contact.fusionPresence != null) {
                  presence.fusionPresence = contact.fusionPresence.value();
               }

               if (contact.msnPresence != null) {
                  presence.msnPresence = contact.msnPresence.value();
               }

               if (contact.aimPresence != null) {
                  presence.aimPresence = contact.aimPresence.value();
               }

               if (contact.yahooPresence != null) {
                  presence.yahooPresence = contact.yahooPresence.value();
               }

               if (contact.gtalkPresence != null) {
                  presence.gtalkPresence = contact.gtalkPresence.value();
               }

               if (contact.facebookPresence != null) {
                  presence.facebookPresence = contact.facebookPresence.value();
               }

               found = true;
               break;
            }
         }

         return found;
      }
   }

   public Integer changedDisplayPicture(String source, String displayPicture, long timeStamp) {
      Integer contactId = null;
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if (source.equals(contact.fusionUsername)) {
               contactId = contact.id;
               contact.displayPicture = displayPicture;
               contact.statusTimeStamp = new Date(timeStamp);
               break;
            }
         }

         return contactId;
      }
   }

   public Integer changedStatusMessage(String source, String statusMessage, long timeStamp) {
      Integer contactId = null;
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData contact = (ContactData)i$.next();
            if (source.equals(contact.fusionUsername)) {
               contactId = contact.id;
               contact.statusMessage = statusMessage;
               contact.statusTimeStamp = new Date(timeStamp);
               break;
            }
         }

         return contactId;
      }
   }

   public ContactList getContactList() {
      ContactList contactList = new ContactList();
      contactList.contacts = this.getContacts();
      contactList.contactGroups = this.getGroups();
      synchronized(this.contactList) {
         contactList.version = this.contactListVersion;
         return contactList;
      }
   }

   public Set<String> getOutstandingContactRequests(Set<String> pendingContacts) {
      synchronized(this.contactRequestSentList) {
         pendingContacts.removeAll(this.contactRequestSentList);
         return pendingContacts;
      }
   }

   public void updateOutstandingContactRequests(Set<String> users) {
      synchronized(this.contactRequestSentList) {
         Iterator i$ = users.iterator();

         while(i$.hasNext()) {
            String username = (String)i$.next();
            this.contactRequestSentList.add(username);
         }

      }
   }

   public void changedDetail(ContactData newContact, int contactListVersion) {
      synchronized(this.contactList) {
         Iterator i$ = this.contactList.iterator();

         while(i$.hasNext()) {
            ContactData existingContact = (ContactData)i$.next();
            if (existingContact.id.equals(newContact.id)) {
               newContact.msnPresence = existingContact.msnPresence;
               newContact.yahooPresence = existingContact.yahooPresence;
               newContact.aimPresence = existingContact.aimPresence;
               newContact.gtalkPresence = existingContact.gtalkPresence;
               newContact.facebookPresence = existingContact.facebookPresence;
               this.contactList.remove(existingContact);
               break;
            }
         }

         this.contactList.add(newContact);
         this.contactListVersion = contactListVersion;
      }
   }

   public void removeGroup(int contactGroupID) {
      synchronized(this.groupList) {
         Iterator i$ = this.groupList.iterator();

         while(i$.hasNext()) {
            ContactGroupData contactGroup = (ContactGroupData)i$.next();
            if (contactGroup.id == contactGroupID) {
               this.groupList.remove(contactGroup);
               break;
            }
         }

      }
   }

   public void updateGroup(ContactGroupData newContactGroup) {
      synchronized(this.groupList) {
         Iterator i$ = this.groupList.iterator();

         while(i$.hasNext()) {
            ContactGroupData existingGroup = (ContactGroupData)i$.next();
            if (existingGroup.id.equals(newContactGroup.id)) {
               this.groupList.remove(existingGroup);
               break;
            }
         }

         this.groupList.add(newContactGroup);
      }
   }

   public PresenceType findOverallFusionPresenceForContact(ChatObjectManagerUser objectManager, ContactData contact, String username) {
      try {
         UserPrx contactUserPrx = objectManager.findUserPrxFromRegistry(contact.fusionUsername);
         return PresenceType.fromValue(contactUserPrx.getOverallFusionPresence(username));
      } catch (ObjectNotFoundException var5) {
         return PresenceType.OFFLINE;
      } catch (Exception var6) {
         return contact.fusionPresence == null ? PresenceType.OFFLINE : contact.fusionPresence;
      }
   }

   public void addToCurrentChatroomList(String chatroom) {
      this.currentChatrooms.add(chatroom);
   }

   public void removeFromCurrentChatroomList(String chatroom) {
      this.currentChatrooms.remove(chatroom);
   }

   public String[] getCurrentChatrooms() {
      return (String[])this.currentChatrooms.toArray(new String[this.currentChatrooms.size()]);
   }

   public void copyPresenceForExistingIMContacts(ContactData oldContactData, ContactData newContactData) {
      if (newContactData.aimUsername != null && newContactData.aimUsername.equals(oldContactData.aimUsername)) {
         newContactData.aimPresence = oldContactData.aimPresence;
      } else {
         newContactData.aimPresence = PresenceType.OFFLINE;
      }

      if (newContactData.gtalkUsername != null && newContactData.gtalkUsername.equals(oldContactData.gtalkUsername)) {
         newContactData.gtalkPresence = oldContactData.gtalkPresence;
      } else {
         newContactData.gtalkPresence = PresenceType.OFFLINE;
      }

      if (newContactData.yahooUsername != null && newContactData.yahooUsername.equals(oldContactData.yahooUsername)) {
         newContactData.yahooPresence = oldContactData.yahooPresence;
      } else {
         newContactData.yahooPresence = PresenceType.OFFLINE;
      }

      if (newContactData.msnUsername != null && newContactData.msnUsername.equals(oldContactData.msnUsername)) {
         newContactData.msnPresence = oldContactData.msnPresence;
      } else {
         newContactData.msnPresence = PresenceType.OFFLINE;
      }

      if (newContactData.facebookUsername != null && newContactData.facebookUsername.equals(oldContactData.facebookUsername)) {
         newContactData.facebookPresence = oldContactData.facebookPresence;
      } else {
         newContactData.facebookPresence = PresenceType.OFFLINE;
      }

   }
}
