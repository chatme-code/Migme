package com.projectgoth.fusion.objectcache;

import Ice.LocalException;
import com.google.gson.GsonBuilder;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.chat.external.ChatConnectionInterface;
import com.projectgoth.fusion.chat.external.ChatConnectionListenerInterface;
import com.projectgoth.fusion.chat.external.ChatExternal;
import com.projectgoth.fusion.chatsync.ChatDefinition;
import com.projectgoth.fusion.chatsync.ChatList;
import com.projectgoth.fusion.chatsync.CurrentChatList;
import com.projectgoth.fusion.chatsync.MessageStatusEvent;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.data.FileData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.emote.EmoteCommandStateStorage;
import com.projectgoth.fusion.exception.UserNotOnlineException;
import com.projectgoth.fusion.fdl.enums.ImDetailType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.packet.FusionPktImAvailable;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.ChatroomMessageSentTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.PrivateMessageSentTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ThirdPartyInstantMessageTrigger;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.ContactGroupDataIce;
import com.projectgoth.fusion.slice.ContactList;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.CurrencyDataIce;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.PresenceAndCapabilityIce;
import com.projectgoth.fusion.slice.SessionCachePrx;
import com.projectgoth.fusion.slice.SessionIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserErrorResponse;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class ChatUser implements ChatSourceUser, ChatConnectionListenerInterface {
   private static boolean logSessions;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatUser.class));
   private static final Logger floodLog = Logger.getLogger("FloodLogAudit");
   private Semaphore loginSemaphore;
   private ChatContacts contactList;
   private final ChatUserSessions sessionList;
   private ChatUserEmoteKeys emoticonList;
   private PresenceType overallFusionPresence;
   private boolean silencedNotifications;
   private boolean purging;
   private String username;
   private ChatUserData chatUserData;
   private ChatUserReputation chatUserReputation;
   private ChatUserRegistry chatRegistry;
   private ChatExternal chatExternal;
   private final ChatUser.ChatUserGroupChats chatGroupChats;
   private Integer userID;
   private ChatObjectManagerUser objectManager;
   private ChatUser.ChatUserMetrics metrics;
   private EmoteCommandStateStorage emoteCommandStates;
   private ChatUserRateLimit rateLimit;
   private AtomicReference<ChatList> currentChatListGroupChatSubset;

   public ChatUser(ChatObjectManagerUser objectManager, String username) {
      this.overallFusionPresence = PresenceType.OFFLINE;
      this.currentChatListGroupChatSubset = new AtomicReference((Object)null);
      this.objectManager = objectManager;
      this.username = username;
      this.metrics = new ChatUser.ChatUserMetrics(objectManager.getRequestCounter());
      this.sessionList = new ChatUserSessions(objectManager, this);
      this.rateLimit = new ChatUserRateLimit(objectManager);
      this.chatRegistry = new ChatUserRegistry(objectManager.getRegistryPrx());
      this.loginSemaphore = new Semaphore(SystemProperty.getInt((String)"MaxConcurrentLoginsPerUser", 100));
      this.chatGroupChats = new ChatUser.ChatUserGroupChats(objectManager);
      this.emoteCommandStates = new EmoteCommandStateStorage(objectManager.getIcePrxFinder());
   }

   public void loadFromState(ChatUserState state) {
      if (state != null) {
         ;
      }
   }

   public UserPrx makeProxy() {
      return this.objectManager.makeUserPrx(this.username);
   }

   public void loadFromDB() throws Exception {
      UserData userData;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
         UserObject user = new UserObject(this.username);
         userData = user.getUserData();
         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: get userdata from DAO:%s", userData));
         }
      } else {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         userData = userEJB.loadUser(this.username, true, false);
      }

      if (userData == null) {
         throw new Exception("Invalid migme username " + this.username);
      } else {
         this.chatUserReputation = new ChatUserReputation(userData.userID);
         ChatUserData chatUserData = new ChatUserData(userData);
         this.username = chatUserData.getUsername();
         this.userID = chatUserData.getUserID();
         this.chatUserData = chatUserData;
         Contact contactEJB = null;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
            this.contactList = new ChatContacts((Contact)null, chatUserData);
         } else {
            contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            this.contactList = new ChatContacts(contactEJB, chatUserData);
         }

         try {
            List<String> contactUsernames = this.contactList.setContactsPresenceOffline();
            String[] users = (String[])contactUsernames.toArray(new String[contactUsernames.size()]);
            Map<String, UserPrx> userProxyMap = this.chatRegistry.findUserObjectsMap(users);
            this.contactList.assignPresence(userProxyMap);
         } catch (Exception var7) {
            log.error("Unable to assign presence to user objects", var7);
            return;
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.ENABLE_LOOKUP_UNS_VIA_ICE)) {
            chatUserData.updatePendingContacts(this.getUserNotificationServiceProxy());
         } else {
            if (contactEJB == null) {
               contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            }

            chatUserData.updatePendingContacts(contactEJB);
         }

         this.emoticonList = new ChatUserEmoteKeys(this.username);
         this.chatExternal = new ChatExternal(this.objectManager, this, this.userID, this.username);
      }
   }

   public String getUsername() {
      return this.username;
   }

   public static void setLogSessions(boolean log) {
      logSessions = log;
   }

   public void dispose() {
      synchronized(this) {
         this.purging = true;
         this.sessionList.dispose();
         this.updateOverallFusionPresence();
         this.chatExternal.dispose();
      }
   }

   public void disconnect(String reason) {
      this.sessionList.disconnect(reason, this.username);
   }

   public void disconnectAndSuspend(String reason, String reference) {
      log.info("Suspending [" + this.username + "]. Reason [" + reason + "]. Reference [" + reference + "].");
      MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.TEMP_LOGIN_SUSPENSION, this.username, "1");
      this.disconnect(reason);
   }

   public void disconnectFlooder(String reason) {
      try {
         this.disconnectFlooderInner(reason);
      } catch (Exception var3) {
         log.error("Unable to disconnect flooder, username=" + this.username + " reason=" + reason + " cause=" + var3, var3);
      }

   }

   private void disconnectFlooderInner(String reason) throws Exception {
      log.info("Detected flooder [" + this.username + "]. " + reason);
      if (!this.chatUserData.isChatRoomAdmin() && this.chatUserReputation.canGetBanned()) {
         log.info("FLOODING: Disconnecting and suspending user from login [" + this.username + "]. " + reason);
         floodLog.info("FLOODING: Disconnecting and suspending user from login [" + this.username + "]. " + reason);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.LOGIN_BAN, this.username, "1");
      } else {
         log.info("FLOODING: Disconnecting user from login [" + this.username + "]. " + reason);
      }

      try {
         EmailUserNotification note = new EmailUserNotification();
         note.emailAddress = SystemProperty.get("ReportAbuseEmail");
         note.subject = "[DISCONNECTED FOR FLOODING] " + this.username;
         note.message = reason;
         if (note.emailAddress.length() > 0) {
            this.getUserNotificationServiceProxy().notifyUserViaEmail(note);
         }
      } catch (Exception var4) {
         log.warn("Unable to notify customer support about flooder [" + this.username + "]", var4);
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIN_MIG_LEVEL_BEFORE_LOGIN_BAN_LOG_ENABLED)) {
         try {
            floodLog.info("FLOODING: User stat [" + this.username + "] - " + (new GsonBuilder()).serializeNulls().excludeFieldsWithoutExposeAnnotation().create().toJson(this));
         } catch (Exception var3) {
            log.warn("Unable to log user stats for user " + this.username);
         }
      }

      this.disconnect("Violation of terms and conditions");
   }

   public void endSession(ChatSession session) {
      String sessionID = session.getSessionID();
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se545ChatUserEndSessionSyncChangesEnabled.getValue()) {
         this.sessionList.removeSession(sessionID, false);
      } else {
         synchronized(this) {
            this.sessionList.removeSession(sessionID, false);
         }
      }

   }

   private void removeSession(ChatSession session, boolean userIsBeingPurged) {
      String sessionID = session.getSessionID();
      if (log.isDebugEnabled()) {
         log.debug("User.removeSession - sessionId:" + sessionID);
      }

      this.objectManager.removeSession(sessionID, userIsBeingPurged);
      if (!userIsBeingPurged) {
         this.updateOverallFusionPresence();
      }

      this.logSession(session);
      if (this.overallFusionPresence == PresenceType.OFFLINE) {
         this.chatExternal.logout();
      }

      try {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.CHATROOM_MESSAGE_TRIGGER_ENABLED)) {
            int chatRoomMessagesSent = session.getSessionMetrics().chatroomMessagesSent;
            if (chatRoomMessagesSent > 0) {
               ChatroomMessageSentTrigger trigger = new ChatroomMessageSentTrigger(this.chatUserData.getUserData());
               trigger.amountDelta = 0.0D;
               trigger.quantityDelta = chatRoomMessagesSent;
               RewardCentre.getInstance().sendTrigger(trigger);
            }
         }
      } catch (Exception var7) {
         log.warn("Unable to send ChatroomMessageSentTrigger for user [" + this.username + "] :" + var7.getMessage(), var7);
      }

      synchronized(this) {
         if (!userIsBeingPurged && this.sessionList.isEmpty()) {
            this.triggerPurgeUser();
         }
      }

      this.triggerSessionRemoved();
   }

   public void purgeExpiredSessions() {
      if (this.sessionList.getSessionCount() == 0) {
         this.triggerPurgeUser();
      } else {
         this.sessionList.purgeExpiredSessions();
      }
   }

   public boolean isPurging() {
      return this.purging;
   }

   public boolean isOnContactList(String contactUsername) {
      return this.contactList.isOnContactList(contactUsername);
   }

   public boolean isOnBlockList(String contactUsername) {
      return this.chatUserData.isOnBlockList(contactUsername);
   }

   public void setNotificationSilence(boolean silence) {
      log.info("Silenced notifications [" + silence + "] for user [" + this.username + "]");
      this.silencedNotifications = silence;
   }

   public void setSessionLanguage(String sessionID, String language) {
      ChatSession session = this.sessionList.getSession(sessionID);
      if (session != null) {
         session.setLanguage(language);
      }
   }

   private boolean handleUserErrorResponse(UserErrorResponse response) throws FusionException {
      if (response.silentlyIgnore) {
         return response.error;
      } else if (response.error) {
         throw new FusionException(response.reason);
      } else {
         return response.error;
      }
   }

   private List<ChatConnectionInterface> getActiveChatConnections() {
      return this.chatExternal.getActiveChatConnections();
   }

   public int[] getConnectedOtherIMs() {
      return this.chatExternal.getConnectedOtherIMs();
   }

   public ContactList getContactList() {
      return this.contactList.getContactList();
   }

   public ContactDataIce[] getContacts() {
      return this.contactList.getOnlineContacts();
   }

   public int getContactListVersion() {
      return this.contactList.getContactListVersion();
   }

   public void putMessage(MessageDataIce message) throws FusionException {
      if (this.purging) {
         throw new FusionException(this.username + " is not online");
      } else {
         if (message.type == MessageType.FUSION.value() && message.messageDestinations != null && message.messageDestinations.length > 0 && message.messageDestinations[0].type == MessageDestinationData.TypeEnum.INDIVIDUAL.value()) {
            if (this.handleUserErrorResponse(this.contactList.userCanContactMe(message.source, (MessageDataIce)null, this.silencedNotifications))) {
               return;
            }

            if (this.chatUserData.getMessageSetting() == UserSettingData.MessageEnum.FRIENDS_ONLY && !this.chatUserData.isOnBroadcastList(message.source) && !this.sessionList.hasPrivateChattedWith(message.source)) {
               throw new FusionException("You must be a friend of this user to chat with them");
            }
         }

         if (this.validatePutMessage(message)) {
            this.sessionList.putMessage(message);
            if (MessageData.isMessageToAGroupChat(message)) {
               this.ensureGroupChatIsInChatList(message);
            }

            this.metrics.request();
         }
      }
   }

   private void ensureGroupChatIsInChatList(MessageDataIce message) {
      try {
         String groupChatGUID = message.messageDestinations[0].destination;
         ChatDefinition cdGroupChatKey = new ChatDefinition(groupChatGUID, (byte)MessageDestinationData.TypeEnum.GROUP.value());
         ChatList cclSubset = (ChatList)this.currentChatListGroupChatSubset.get();
         if (cclSubset == null || cclSubset != null && !cclSubset.containsChat(cdGroupChatKey)) {
            try {
               MessageSwitchboardDispatcher.getInstance().onJoinGroupChat(this.objectManager.getRegistryPrx(), this.getUsername(), this.getUserID(), groupChatGUID, false, (UserPrx)null);
               ChatList clone = cclSubset == null ? new CurrentChatList(0) : cclSubset.clone();
               ((ChatList)clone).setVersion(0);
               ((ChatList)clone).addChat(cdGroupChatKey);
               this.currentChatListGroupChatSubset.compareAndSet(cclSubset, clone);
            } catch (Exception var6) {
               log.warn("Unable to add participant=" + this.getUsername() + " to stored group chat=" + groupChatGUID + " in chatsync");
            }
         }
      } catch (Exception var7) {
         log.error("ensureGroupChatIsInChatList: e=" + var7, var7);
      }

   }

   public void putMessage(String sessionID, MessageDataIce message) throws FusionException {
      if (this.validatePutMessage(message)) {
         try {
            ChatSession session = this.sessionList.getSession(sessionID);
            session.putMessageLocal(message);
         } catch (LocalException var4) {
            throw new FusionException("The user is no longer connected");
         }

         this.metrics.request();
      }
   }

   private void putOtherIMMessage(String conferenceID, String username, String message, MessageType messageType, boolean notifyRewardSystem) {
      MessageData messageData = new MessageData();
      messageData.username = username;
      messageData.dateCreated = new Date();
      messageData.type = messageType;
      messageData.contentType = MessageData.ContentTypeEnum.TEXT;
      messageData.messageText = message;
      messageData.sendReceive = MessageData.SendReceiveEnum.RECEIVE;
      messageData.source = username;
      messageData.emoticonKeys = this.emoticonList.getEmoticonKeysInMessage(message);
      messageData.messageDestinations = new LinkedList();
      MessageDestinationData messageDestData = new MessageDestinationData();
      if (conferenceID == null) {
         messageDestData.type = MessageDestinationData.TypeEnum.INDIVIDUAL;
         messageDestData.destination = username;
      } else {
         messageDestData.type = MessageDestinationData.TypeEnum.GROUP;
         messageDestData.destination = conferenceID;
      }

      messageDestData.cost = 0.0D;
      messageDestData.status = MessageDestinationData.StatusEnum.PENDING;
      messageData.messageDestinations.add(messageDestData);
      this.logMessage(messageData.type, this.chatUserData.getCountryID(), username, this.getUsername(), message);
      if (notifyRewardSystem) {
         this.chatExternal.triggerReward(this.chatUserData.getUserData(), messageData.type, ThirdPartyInstantMessageTrigger.EventTypeEnum.MESSAGE_RECEIVED);
      }

      try {
         this.putMessage(messageData.toIceObject());
      } catch (FusionException var9) {
         log.error("Unable forward received IM message");
      }

   }

   public void otherIMSetPresence() {
      String statusMessage = this.chatUserData.getStatusMessage();
      Iterator i$ = this.getActiveChatConnections().iterator();

      while(i$.hasNext()) {
         ChatConnectionInterface connection = (ChatConnectionInterface)i$.next();

         try {
            connection.setStatus(this.overallFusionPresence, statusMessage);
         } catch (Exception var5) {
            log.warn("Unable to change " + connection.getImType() + " presence for the user '" + this.username + "'. Exception: " + var5.getMessage());
         }
      }

   }

   public void contactRequestReject(String inviterUsername) {
      if (log.isDebugEnabled()) {
         log.debug("[" + inviterUsername + "] rejecting contact request from user [" + this.getUsername() + "], B side");
      }

      int outstandingPendingContacts = this.chatUserData.contactRequestReject(inviterUsername);
      this.sessionList.contactRequestRejected(inviterUsername, outstandingPendingContacts);
   }

   public ContactDataIce contactRequestWasAccepted(ContactDataIce contact, String statusMessage, String displayPicture, int overallFusionPresence, int contactListVersion) {
      if (this.purging) {
         return contact;
      } else {
         ContactData newContact = new ContactData(contact);
         this.chatUserData.addToBroadcastList(contact.username);

         try {
            if (log.isDebugEnabled()) {
               log.debug("our [" + this.getUsername() + "] contact request was accepted, setting presence from " + "[" + newContact.username + "] to [" + PresenceType.fromValue(overallFusionPresence).toString() + "]");
            }

            this.contactChangedPresence(ImType.FUSION.value(), newContact.username, overallFusionPresence);
            long now = System.currentTimeMillis();
            this.contactChangedStatusMessage(contact.username, statusMessage, now);
            this.contactChangedDisplayPicture(contact.username, displayPicture, now);
         } catch (FusionException var9) {
         }

         if (contactListVersion > 0) {
            this.contactList.setContactListVersion(contactListVersion);
         }

         contact.fusionPresence = this.overallFusionPresence.value();
         if (log.isDebugEnabled()) {
            log.debug("contactRequestWasAccepted, returning contact [" + contact.fusionUsername + "] with presence [" + contact.fusionPresence + "]");
         }

         return contact;
      }
   }

   public void contactRequestWasRejected(String contactRequestUsername, int contactListVersion) {
      if (log.isDebugEnabled()) {
         log.debug("[" + contactRequestUsername + "] rejecting contact request from user [" + this.getUsername() + "], A side");
      }

      Integer contactID = this.contactList.findSourceContactID(MessageType.FUSION, contactRequestUsername);
      this.contactList.removeContact(contactRequestUsername);
      this.chatUserData.removeFromBroadcastList(contactRequestUsername);
      if (contactID != null) {
         this.contactList.setContactListVersion(contactListVersion);
         this.notifySessionsOfRemovedContact(contactID, contactListVersion);
      } else if (log.isDebugEnabled()) {
         log.debug("could not find contact ID for contact [" + contactRequestUsername + "]");
      }

   }

   public void contactChangedDisplayPicture(String source, String displayPicture, long timeStamp) throws FusionException {
      Integer contactId = this.contactList.changedDisplayPicture(source, displayPicture, timeStamp);
      if (contactId != null) {
         this.sessionList.contactChangedDisplayPicture(contactId, displayPicture, timeStamp);
         this.metrics.request();
      }
   }

   public void contactChangedStatusMessage(String source, String statusMessage, long timeStamp) throws FusionException {
      Integer contactId = this.contactList.changedStatusMessage(source, statusMessage, timeStamp);
      if (contactId != null) {
         this.sessionList.contactChangedStatusMessage(contactId, statusMessage, timeStamp);
         this.metrics.request();
      }
   }

   public void blockUser(String blockUsername, int contactListVersion) {
      if (log.isDebugEnabled()) {
         log.debug("user [" + this.getUsername() + "] is blocking other user [" + blockUsername + "]");
      }

      try {
         this.contactChangedPresence(ImType.FUSION.value(), blockUsername, PresenceType.OFFLINE.value());
      } catch (Exception var4) {
      }

      Integer contactID = this.contactList.blockUser(blockUsername, contactListVersion);
      if (contactID != null) {
         this.notifySessionsOfRemovedContact(contactID, contactListVersion);
      } else if (log.isDebugEnabled()) {
         log.debug("blockUser() could not find contact ID for contact [" + blockUsername + "]");
      }

   }

   public void unblockUser(String username) {
      if (!this.purging) {
         if (log.isDebugEnabled()) {
            log.debug("unblocking user [" + username + "]");
         }

         boolean unblocked = this.contactList.unblockUser(username);
         if (unblocked && log.isDebugEnabled()) {
            log.debug("removed [" + username + "] from [" + this.getUsername() + "]'s blocklist");
         }

      }
   }

   public void notifySessionsOfNewContact(ContactData contact, int contactListVersion, boolean guaranteedIsNew) {
      this.sessionList.notifySessionsOfNewContact(contact, contactListVersion, guaranteedIsNew);
   }

   public void notifySessionsOfRemovedContact(int contactID, int contactListVersion) {
      if (log.isDebugEnabled()) {
         log.debug("removing our [" + this.getUsername() + "] contact [" + contactID + "] from the contacts");
      }

      this.sessionList.notifySessionsOfRemovedContact(contactID, contactListVersion);
   }

   public void onSignInSuccess(ChatConnectionInterface source) {
      this.sessionList.onSignInSuccess(source);
      String statusMessage = this.chatUserData.getStatusMessage();

      try {
         source.setStatus(this.overallFusionPresence, statusMessage);
      } catch (Exception var4) {
         log.warn("Unable to set " + source.getImType() + " presence after signed in", var4);
      }

   }

   public void onSignInFailed(ChatConnectionInterface source, String reason) {
      if (source == null) {
         log.warn("User:" + this.username + " - 3rd party chat login failed (no source) reason:" + reason);
      } else {
         this.contactList.setContactsPresence(source.getImType(), PresenceType.OFFLINE);
         this.sessionList.onSignInFailed(source, reason);
      }
   }

   public void onDisconnected(ChatConnectionInterface source, String reason) {
      this.contactList.setContactsDataPresence(source.getImType(), PresenceType.OFFLINE);
      this.sessionList.otherIMLoggedOut(source.getImType(), reason);
   }

   public void onMessageReceived(ChatConnectionInterface source, String conferenceID, String username, String message) {
      this.putOtherIMMessage(conferenceID, username, message, this.chatExternal.getMessageTypeEnum(source.getImType()), true);
   }

   public void onMessageFailed(ChatConnectionInterface source, String conferenceID, String username, String message, String reason) {
      this.putOtherIMMessage(conferenceID, username, "The following message could not be sent: " + message + ". [" + reason + "]", this.chatExternal.getMessageTypeEnum(source.getImType()), false);
   }

   public void onContactStatusChanged(ChatConnectionInterface source, String username, PresenceType presence) {
      try {
         this.contactChangedPresence(source.getImType().value(), username, presence.value());
      } catch (Exception var5) {
         log.warn("Unable to change the presence of the " + source.getImType() + " contact '" + username + "' for the Fusion user '" + username + "'");
      }

   }

   public void contactChangedPresence(int imType, String source, int presenceId) throws FusionException {
      ArrayList<Integer> contactIDs = new ArrayList();
      HashSet<ContactData> IMContactsNoLongerOffline = new HashSet();
      ImType imTypeEnum = ImType.fromValue(imType);
      PresenceType presence = PresenceType.fromValue(presenceId);
      if (log.isDebugEnabled()) {
         log.debug("updating contact [" + source + "] presence [" + presence + "] for imtype [" + imTypeEnum.toString() + "]");
      }

      if (imTypeEnum == null) {
         log.warn("UserI.contactChangedPresence() received invalid IM type '" + imType + "'");
      }

      this.contactList.contactChangedPresence(imTypeEnum, source, presence, contactIDs, IMContactsNoLongerOffline);
      if (contactIDs.size() == 0) {
         if (log.isDebugEnabled()) {
            log.debug("failed to find contact with username [" + source + "], to set the presence to [" + presence + "]");
         }

      } else {
         int contactListVersion = this.contactList.getContactListVersion();
         Iterator i$ = IMContactsNoLongerOffline.iterator();

         while(i$.hasNext()) {
            ContactData item = (ContactData)i$.next();
            this.sessionList.contactAdded(item, contactListVersion, false);
         }

         i$ = contactIDs.iterator();

         while(i$.hasNext()) {
            Integer contactID = (Integer)i$.next();
            this.sessionList.contactChangedPresence(contactID, imType, presence);
         }

         this.metrics.request();
      }
   }

   public void onContactDetail(ChatConnectionInterface source, String username, String displayname) {
      ImType imType = source.getImType();
      ContactData contact = null;
      contact = this.contactList.getContact(username, imType);
      if (contact == null) {
         contact = this.contactList.createContact(username, displayname, imType);
      }

      boolean isShown = this.contactList.getIsContactShown(contact, imType);
      if (isShown) {
         this.sessionList.contactAdded(contact, this.contactList.getContactListVersion(), false);
      }

   }

   public void onContactRequest(ChatConnectionInterface source, String username, String displayname) {
   }

   public void onConferenceCreated(ChatConnectionInterface source, String conferenceID, String creator) {
      ImType imType = source.getImType();
      if (!creator.equals(this.chatExternal.getUsername(imType))) {
         this.sessionList.otherIMConferenceCreated(source.getImType(), conferenceID, creator);
         this.putOtherIMMessage(conferenceID, conferenceID, creator + " started a " + this.chatExternal.getIMDisplayName(source.getImType()) + " conference", this.chatExternal.getMessageTypeEnum(source.getImType()), false);
         this.sendIMGroupChatParticipants(conferenceID, source.getImType().value());
      }
   }

   private void sendIMGroupChatParticipants(String conferenceID, byte imType) {
      log.info("sendIMGroupChatParticipants: groupChatId=" + conferenceID + ", imType=" + imType);

      try {
         if ((Boolean)SystemPropertyEntities.Temp.Cache.fi83Week3UseAutogeneratedPacketsEnabled.getValue()) {
            String[] participants = this.getOtherIMConferenceParticipants(imType, conferenceID);
            String[] EMPTY = new String[0];
            this.sessionList.sendGroupChatParticipantArrays(conferenceID, imType, participants, EMPTY);
         } else {
            String participants = StringUtil.join((Object[])this.getOtherIMConferenceParticipants(imType, conferenceID), ";");
            this.sessionList.sendGroupChatParticipants(conferenceID, imType, participants, "");
         }
      } catch (Exception var5) {
         log.info("sendIMGroupChatParticipants error: " + var5.getMessage());
      }

   }

   public String[] getOtherIMConferenceParticipants(int imType, String otherIMConferenceID) {
      ImType imTypeEnum = ImType.fromValue(imType);

      try {
         ChatConnectionInterface connection = this.chatExternal.getChatConnection(imTypeEnum);
         String[] participants = (String[])connection.getConferenceParticipants(otherIMConferenceID).toArray(new String[0]);
         return participants;
      } catch (Exception var6) {
         log.warn("otherIMGetConferenceParticipants() error: IM type " + MessageType.fromValue(imType) + " is not supported");
         return new String[0];
      }
   }

   public ContactDataIce[] getOtherIMContacts() {
      return this.contactList.getOtherIMContacts();
   }

   public Credential[] getOtherIMCredentials() {
      return this.chatExternal.getOtherIMCredentials();
   }

   public void notifyUserLeftGroupChat(String groupChatId, String username) {
      this.sessionList.notifyUserLeftGroupChat(groupChatId, username);
   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted) {
      this.sessionList.notifyUserJoinedGroupChat(groupChatId, username, isMuted);
   }

   public void onUserJoinedConference(ChatConnectionInterface source, String conferenceID, String username) {
      this.putOtherIMMessage(conferenceID, conferenceID, username + " added to the conference", this.chatExternal.getMessageTypeEnum(source.getImType()), false);
      log.info("notifyUserJoinedIMGroupChat: groupChatId=" + conferenceID + ", username=" + username);
      this.sessionList.notifyUserJoinedGroupChat(conferenceID, username, false);
   }

   public void onUserLeftConference(ChatConnectionInterface source, String conferenceID, String username) {
      this.putOtherIMMessage(conferenceID, conferenceID, username + " left the conference", this.chatExternal.getMessageTypeEnum(source.getImType()), false);
      log.info("notifyUserLeftIMGroupChat: groupChatId=" + conferenceID + ", username=" + username);
      this.sessionList.notifyUserLeftGroupChat(conferenceID, username);
   }

   public void onConferenceInvitationFailed(ChatConnectionInterface source, String conferenceID, String username, String reason) {
      this.putOtherIMMessage(conferenceID, conferenceID, "Unable to invite " + username + " to conference: " + reason, this.chatExternal.getMessageTypeEnum(source.getImType()), false);
   }

   public void onSessionRemoved(ChatSession session) {
      this.removeSession(session, false);
   }

   public void otherIMLogin(int imType, int presence, boolean showOfflineContacts) throws FusionException {
      ImType imTypeEnum = ImType.fromValue(imType);
      String imTypeName = imTypeEnum.toString();
      this.chatUserReputation.verifyIMAllowed(imTypeEnum);
      if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.OTHER_IM_LOGIN, this.chatUserData.getUserData()) && SystemProperty.getBool("OtherIMLoginDisabledForUnauthenticatedUsers", false)) {
         throw new FusionException("You must authenticate your account before you can use Instant Messaging.");
      } else {
         this.chatExternal.login(imTypeEnum);
      }
   }

   public void otherIMLogout(int imType) {
      this.chatExternal.logout(ImType.fromValue(imType));
   }

   public void otherIMSendMessage(int imType, String otherIMUsername, String message) throws FusionException {
      ImType imTypeEnum = ImType.fromValue(imType);

      ChatConnectionInterface connection;
      try {
         connection = this.chatExternal.getChatConnection(imTypeEnum);
      } catch (Exception var8) {
         log.warn("otherIMSendMessage() user:" + this.username + " error: IM type " + imTypeEnum + " is not supported");
         throw new FusionException(var8.getMessage());
      }

      if (!connection.isSignedIn()) {
         throw new FusionException("You are not signed in to " + imTypeEnum);
      } else {
         try {
            connection.sendMessage(otherIMUsername, message);
         } catch (Exception var7) {
            throw new FusionException(imTypeEnum + " user:" + this.username + " error: " + var7.getMessage());
         }
      }
   }

   public String otherIMInviteToConference(int imType, String otherIMConferenceID, String otherIMUsername) throws FusionException {
      ImType imTypeEnum = ImType.fromValue(imType);

      ChatConnectionInterface connection;
      try {
         connection = this.chatExternal.getChatConnection(imTypeEnum);
      } catch (Exception var8) {
         log.warn("otherIMInviteToConference() user:" + this.username + " error: IM type " + imTypeEnum + " is not supported");
         throw new FusionException(var8.getMessage());
      }

      if (connection != null && connection.isSignedIn()) {
         try {
            return connection.inviteToConference(otherIMConferenceID, otherIMUsername);
         } catch (Exception var7) {
            throw new FusionException(imTypeEnum + " user:" + this.username + " error: " + var7.getMessage());
         }
      } else {
         throw new FusionException("You are not signed in to " + imTypeEnum);
      }
   }

   public void otherIMLeaveConference(int imType, String otherIMConferenceID) {
      ImType imTypeEnum = ImType.fromValue(imType);
      ChatConnectionInterface connection = null;

      try {
         connection = this.chatExternal.getChatConnection(imTypeEnum);
      } catch (Exception var7) {
         log.warn("otherIMLeaveConference() user:" + this.username + " error: IM type " + imTypeEnum + " is not supported");
      }

      if (connection != null && connection.isSignedIn()) {
         try {
            connection.leaveConference(otherIMConferenceID);
         } catch (Exception var6) {
            log.warn("otherIMLeaveConference() user:" + this.username + " error: IM type " + imTypeEnum + " is not supported");
         }

      }
   }

   public void otherIMAddContact(int imType, String otherIMUsername) throws FusionException {
      ImType imTypeEnum = ImType.fromValue(imType);
      ChatConnectionInterface connection = null;

      try {
         connection = this.chatExternal.getChatConnection(imTypeEnum);
      } catch (Exception var7) {
         log.warn("otherIMAddContact() user:" + this.username + " error: IM type " + imTypeEnum + " is not supported");
         throw new FusionException("IM type " + imTypeEnum + " is not supported");
      }

      if (connection != null && connection.isSignedIn()) {
         try {
            connection.addContact(otherIMUsername);
         } catch (Exception var6) {
            throw new FusionException(imTypeEnum + " Error: " + var6.getMessage());
         }
      } else {
         throw new FusionException("You are not signed in to " + imTypeEnum);
      }
   }

   public void otherIMRemoveContact(int contactId) throws FusionException {
      ContactData contactData = this.contactList.getContact(contactId);
      if (contactData != null) {
         ImType imEnumType = contactData.defaultIM;
         ChatConnectionInterface connection = null;

         try {
            connection = this.chatExternal.getChatConnection(contactData.defaultIM);
         } catch (Exception var8) {
            log.warn("otherIMRemoveContact() user:" + this.username + " error: IM type " + contactData.defaultIM + " is not supported");
            throw new FusionException("IM type " + contactData.defaultIM + " is not supported");
         }

         String imUserName = this.chatExternal.getIMUsername(contactData, imEnumType);

         try {
            connection.removeContact(imUserName);
         } catch (Exception var7) {
            throw new FusionException(imEnumType + " Error: " + var7.getMessage());
         }

         this.contactList.removeContact(contactData);
         this.sessionList.notifySessionsOfRemovedContact(contactId, this.contactList.getContactListVersion());
      }
   }

   public void updateOverallFusionPresence() {
      PresenceType oldPresence = this.overallFusionPresence;
      PresenceType newPresence = PresenceType.OFFLINE;
      newPresence = this.sessionList.getPresence(newPresence);
      if (this.overallFusionPresence != newPresence) {
         this.overallFusionPresence = newPresence;
         String[] watchers = this.chatUserData.findAllowedWatchers();
         this.chatRegistry.contactChangedPresence(watchers, ImType.FUSION.value(), this.getUsername(), this.overallFusionPresence.value());
         this.otherIMSetPresence();
         if (oldPresence == PresenceType.OFFLINE) {
            try {
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               userEJB.sendLookouts(this.getUsername(), new AccountEntrySourceData(ObjectCache.class));
            } catch (Exception var5) {
            }
         }

      }
   }

   public UserErrorResponse userCanContactMe(String username, MessageDataIce message) {
      return this.contactList.userCanContactMe(username, message, this.silencedNotifications);
   }

   public void userDetailChanged(UserDataIce user, AuthenticationServicePrx authenticationServicePrx) {
      this.chatUserData.update(new UserData(user));

      try {
         this.chatExternal.setIMCredentials(authenticationServicePrx.getCredentialsForTypes(this.chatUserData.getUserID(), PasswordType.OTHER_IM_TYPES));
      } catch (FusionException var4) {
         log.warn("Unable to reload otherIMCredentials for " + user.username, var4);
      }

   }

   public void userDisplayPictureChanged(String displayPicture, long timeStamp) {
      String[] broadcastListArray = this.chatUserData.getBroadcastList();
      String statusMessage = this.chatUserData.getStatusMessage();
      this.chatUserData.updateDisplayPicture(displayPicture, timeStamp);
      this.chatRegistry.contactChangedDisplayPicture(broadcastListArray, this.getUsername(), displayPicture, timeStamp);
      this.sessionList.avatarChanged(displayPicture, statusMessage);
   }

   public void userStatusMessageChanged(String statusMessage, long timeStamp) {
      String[] broadcastListArray = this.chatUserData.getBroadcastList();
      String displayPicture = this.chatUserData.getDisplayPicture();
      this.chatUserData.updateStatusMessage(statusMessage, timeStamp);
      this.chatRegistry.contactChangedStatusMessage(broadcastListArray, this.getUsername(), statusMessage, timeStamp);
      this.sessionList.avatarChanged(displayPicture, statusMessage);
      this.otherIMSetPresence();
   }

   private void triggerDisconnectAndSuspend(String reason, String reference) {
      log.info("Suspending [" + this.username + "]. Reason [" + reason + "]. Reference [" + reference + "].");
      MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.TEMP_LOGIN_SUSPENSION, this.username, "1");
      this.disconnect(reason);
   }

   private void triggerPurgeUser() {
      this.objectManager.removeUser(this.username);
   }

   private void triggerSessionRemoved() {
      this.objectManager.onSessionRemoved(this);
   }

   private void validateMessageToActiveUser(String destinationUsername, boolean sendAsOfflineMessage, User userEJB) throws RemoteException, FusionException {
      if (sendAsOfflineMessage && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.CHECK_FOR_DEACTIVATED_RECIPIENT_ENABLED)) {
         boolean recipientActive = userEJB.getAccountStatus(destinationUsername);
         if (!recipientActive) {
            throw new FusionException(destinationUsername + "'s account is not active.");
         }
      }

   }

   private boolean checkIsSendAsOfflineMessage(String destinationUsername, MessageData messageData, Integer recipientId, User userEJB) throws FusionException {
      boolean sendAsOfflineMessage;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.OFFLINE_MESSAGE_GUARDSET_ENABLED)) {
         sendAsOfflineMessage = this.validateOfflineMsgGuardset(messageData, destinationUsername, userEJB, recipientId);
      } else {
         int minMigLevel = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.MIN_MIG_LEVEL_FOR_SENDING);
         if (minMigLevel != 0) {
            log.debug("OLM sending control by mig level enabled");
            int senderMigLevel = this.chatUserReputation.getReputationDataLevel();
            sendAsOfflineMessage = senderMigLevel >= minMigLevel;
         } else {
            log.debug("Can send offline messages: offline msging guardset is disabled  and no control by mig level");
            sendAsOfflineMessage = true;
         }
      }

      return sendAsOfflineMessage;
   }

   private void checkOutstandingContactRequests() {
      Set<String> userList = this.contactList.getOutstandingContactRequests(this.chatUserData.getPendingContacts());
      if (userList.size() > 0) {
         Set<String> sent = this.sessionList.contactRequest(userList);
         this.contactList.updateOutstandingContactRequests(sent);
      }

   }

   private void storeOfflineFusionMessageForIndividual(ChatSession session, int userID, MessageData messageData, String destinationUsername, int recipientId) throws FusionException {
      try {
         OfflineMessageHelper.StorageResult result = OfflineMessageHelper.getInstance().scheduleOfflineMessageStorageAndWait(messageData, userID, recipientId);
         if (result.failed()) {
            throw new FusionException(result.getError());
         } else {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.OfflineMessageSettings.MSG_STORED_CONF_ENABLED)) {
               session.sendOfflineMessageStoredConfirmation(messageData, destinationUsername);
            }

         }
      } catch (FusionException var7) {
         throw var7;
      } catch (Exception var8) {
         log.error("Exception in storeOfflineFusionMessageForIndividual(): " + var8);
         throw new FusionException(var8.getMessage());
      }
   }

   private String storeImage(String sender, byte[] image) throws Exception {
      return this.objectManager.getFileStore().storeImage(sender, image);
   }

   private void logMessage(MessageToLog.TypeEnum typeEnum, Integer countryID, String source, String destinationUsername, int i, String messageText) {
      if (ChatUser.ChatData.logMessagesToFile) {
         try {
            this.objectManager.logMessage(typeEnum, countryID, source, destinationUsername, i, messageText);
         } catch (LocalException var8) {
            log.warn("Unable to send a Fusion message to the MessageLogger application. Exception: " + var8.toString());
         }

      }
   }

   private void logMessage(MessageType messageDataType, Integer countryID, String sourceUsername, String destinationUsername, String message) {
      if (ChatUser.ChatData.logMessagesToFile) {
         try {
            switch(messageDataType) {
            case MSN:
               this.logMessage(MessageToLog.TypeEnum.MSN_RECEIVED, countryID, sourceUsername, destinationUsername, 1, message);
               break;
            case YAHOO:
               this.logMessage(MessageToLog.TypeEnum.YAHOO_RECEIVED, countryID, sourceUsername, destinationUsername, 1, message);
               break;
            case AIM:
               this.logMessage(MessageToLog.TypeEnum.AIM_RECEIVED, countryID, sourceUsername, destinationUsername, 1, message);
               break;
            case GTALK:
               this.logMessage(MessageToLog.TypeEnum.GTALK_RECEIVED, countryID, sourceUsername, destinationUsername, 1, message);
               break;
            case FACEBOOK:
               this.logMessage(MessageToLog.TypeEnum.FACEBOOK_RECEIVED, countryID, sourceUsername, destinationUsername, 1, message);
               break;
            default:
               log.warn("Unable to log unknown received IM message type " + messageDataType);
            }
         } catch (LocalException var7) {
            log.warn("Unable to send an other IM message to the MessageLogger application. Exception: " + var7.toString());
         }

      }
   }

   private void logSession(ChatSession session) {
      if (logSessions) {
         try {
            SessionIce sessionIce = session.getSessionLog();
            sessionIce.sourceCountryID = this.chatUserData.getCountryID();
            sessionIce.userID = this.chatUserData.getUserID();
            sessionIce.username = this.chatUserData.getUsername();
            sessionIce.authenticated = this.chatUserData.isMobileVerified();
            this.getSessionCachePrx().logSession(sessionIce, session.getSessionMetrics());
         } catch (Exception var3) {
            log.warn("Unable to log a session for the user [" + this.username + "]", var3);
         }

      }
   }

   private void logIceLocalException(String msg, MessageData md, LocalException e) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SESSIONI_LOG_ICELOCALEXCEPTIONS_ENABLED)) {
         try {
            if (md != null) {
               msg = msg + " messageData=" + md.toString();
            }

            msg = msg + " getCause=" + e.getCause();
            log.error(msg, e);
         } catch (Exception var5) {
         }
      }

   }

   private boolean validateOfflineMsgGuardset(MessageData messageData, String destinationUsername, User userEJB, int recipientId) throws FusionException {
      try {
         boolean senderInGuardset = userEJB.isUserInMigboAccessList(this.userID, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.OFFLINE_MESSAGING.value());
         boolean recipientInGuardset = userEJB.isUserInMigboAccessList(recipientId, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.OFFLINE_MESSAGING.value());
         if (senderInGuardset && recipientInGuardset) {
            log.info("Can send offline messages: sender " + this.username + " and recipient " + destinationUsername + " both in offline messaging guardset");
            return true;
         } else {
            log.info("Cannot send offline messages: " + this.username + " and/or " + destinationUsername + " not in offline messaging guardset");
            throw new UserNotOnlineException(destinationUsername);
         }
      } catch (FusionException var7) {
         throw var7;
      } catch (Exception var8) {
         log.error("Exception in checkOfflineMsgGuardset(): " + var8);
         throw new FusionException(var8.getMessage());
      }
   }

   public void sendMessageBackToUserAsEmote(String sessionID, MessageDataIce message) throws FusionException {
      message.contentType = MessageData.ContentTypeEnum.EMOTE.value();
      if (message.messageDestinations[0].type == MessageDestinationData.TypeEnum.INDIVIDUAL.value()) {
         String tmp = message.source;
         message.source = message.messageDestinations[0].destination;
         message.messageDestinations[0].destination = tmp;
      }

      this.putMessage(sessionID, message);
   }

   public int getOverallFusionPresence(String requestingUsername) {
      if (!StringUtils.hasLength(requestingUsername)) {
         return this.overallFusionPresence.value();
      } else if (this.chatUserData.isOnBroadcastList(requestingUsername)) {
         return this.overallFusionPresence.value();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("requesting username [" + requestingUsername + "] is not on our broadcastlist, returning offline as presence");
         }

         return PresenceType.OFFLINE.value();
      }
   }

   public void pushNotification(Message message) throws FusionException {
      if (message == null) {
         throw new FusionException("Message cannot be null");
      } else {
         this.sessionList.pushNotification(message);
      }
   }

   public void otherIMRemoved(int imType) {
      this.otherIMLogout(imType);
      ImType type = ImType.fromValue(imType);
      int contactListVersion = this.contactList.getContactListVersion();
      ArrayList<Integer> contactsRemoved = this.contactList.removeContacts(type);
      if (contactsRemoved != null) {
         Iterator i$ = contactsRemoved.iterator();

         while(i$.hasNext()) {
            Integer contactId = (Integer)i$.next();
            this.notifySessionsOfRemovedContact(contactId, contactListVersion);
         }
      }

      Enums.IMEnum[] arr$ = Enums.IMEnum.values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Enums.IMEnum otherIM = arr$[i$];
         if (otherIM.getImType().value() == imType) {
            FusionPktImAvailable imAvailablePkt = new FusionPktImAvailable(otherIM, ImDetailType.UNREGISTERED);

            try {
               this.putSerializedPacket(imAvailablePkt.toSerializedBytes());
            } catch (FusionException var11) {
               log.error(String.format("Unable to notify user sessions about otherIMRemoved event, imtype: %d, %s", imType, otherIM.getName()));
            }
            break;
         }
      }

   }

   public boolean hasPrivateChattedWith(String username) {
      return this.sessionList.hasPrivateChattedWith(username);
   }

   public String[] getBlockList() {
      return this.chatUserData.getBlockList();
   }

   public void addToCurrentChatroomList(String chatroom) throws FusionException {
      this.contactList.addToCurrentChatroomList(chatroom);
      MessageSwitchboardDispatcher.getInstance().onJoinChatRoom(this.objectManager.getApplicationContext(), this.chatUserData.getUsername(), this.chatUserData.getUserID(), chatroom);
   }

   public void removeFromCurrentChatroomList(String chatroom) {
      this.contactList.removeFromCurrentChatroomList(chatroom);
   }

   public String[] getCurrentChatrooms() {
      return this.contactList.getCurrentChatrooms();
   }

   public int getReputationDataLevel() {
      return this.chatUserReputation.getReputationDataLevel();
   }

   public void putFileReceived(MessageDataIce messageIce) throws FusionException {
      if (!this.handleUserErrorResponse(this.userCanContactMe(messageIce.source, (MessageDataIce)null))) {
         if (this.onReceiveBinaryData()) {
            throw new FusionException("Cannot receive anymore images.");
         } else {
            this.sessionList.putFileReceived(messageIce);
         }
      }
   }

   public int getOnlineContactsCount() {
      return this.contactList.getOnlineContactsCount();
   }

   public ContactDataIce acceptContactRequest(ContactDataIce contact, UserPrx contactProxy, int inviterContactListVersion, int inviteeContactListVersion) {
      if (this.purging) {
         return contact;
      } else {
         if (log.isDebugEnabled()) {
            log.debug("user [" + this.getUsername() + "] accepting contact request from [" + contact.fusionUsername + "]");
         }

         if (contactProxy != null) {
            contact = contactProxy.contactRequestWasAccepted(contact, this.chatUserData.getStatusMessage(), this.chatUserData.getDisplayPicture(), this.overallFusionPresence.value(), inviterContactListVersion);
         } else {
            contact.fusionPresence = PresenceType.OFFLINE.value();
         }

         ContactData newContact = new ContactData(contact);
         this.contactList.addContact(newContact, inviteeContactListVersion);
         int outstandingPendingContacts = this.chatUserData.contactRequestAccept(newContact);
         this.contactList.assignPresence(contact);
         this.sessionList.contactRequestAccepted(newContact, inviteeContactListVersion, outstandingPendingContacts);
         return contact;
      }
   }

   public void addPendingContact(String username) {
      if (!this.purging) {
         this.chatUserData.addPendingContact(username);
         if (!this.silencedNotifications) {
            this.checkOutstandingContactRequests();
         }

      }
   }

   public void removeContact(int contactId, int contactListVersion) {
      if (!this.purging) {
         ContactData contact = this.contactList.removeContact(contactId);
         String fusionUsername = contact.fusionUsername;
         if (StringUtils.hasLength(fusionUsername)) {
            this.contactList.removeContact(contact, contactListVersion);
            this.chatUserData.removeFromBroadcastList(fusionUsername);
         }

         this.notifySessionsOfRemovedContact(contactId, contactListVersion);
      }
   }

   public void contactUpdated(String usernameThatWasModified, boolean acceptedContactRequest) {
      this.chatUserData.contactUpdated(usernameThatWasModified, acceptedContactRequest);
      if (!acceptedContactRequest) {
         this.checkOutstandingContactRequests();
      }

   }

   public void contactUpdated(String usernameThatWasModified) throws FusionException {
      this.chatUserData.contactUpdated(usernameThatWasModified);
      this.contactChangedPresence(ImType.FUSION.value(), usernameThatWasModified, PresenceType.OFFLINE.value());
   }

   public void messageSettingsChanged(int setting) {
      if (!this.purging) {
         this.chatUserData.setMessageSetting(UserSettingData.MessageEnum.fromValue(setting));
      }
   }

   public void addContact(ContactDataIce contact, int contactListVersion) {
      if (!this.purging) {
         ContactData newContact = new ContactData(contact);
         this.contactList.addContact(newContact, contactListVersion);
         this.notifySessionsOfNewContact(newContact, contactListVersion, false);
      }
   }

   public void leavingGroupChat() {
      this.chatGroupChats.leaving();
   }

   public void enteringGroupChat(boolean isCreator) throws FusionException {
      this.chatGroupChats.entering(isCreator);
   }

   public void anonymousCallSettingsChanged(int setting) {
      if (!this.purging) {
         this.chatUserData.setAnonymousCallSetting(setting);
      }
   }

   public PresenceAndCapabilityIce contactUpdated(ContactDataIce contact, String oldusername, boolean acceptedContactRequest, boolean changedFusionContact, UserPrx newContactUserProxy, int contactListVersion) throws FusionException {
      if (changedFusionContact) {
         this.chatUserData.updateContact(oldusername, contact.fusionUsername, acceptedContactRequest);
         if (!acceptedContactRequest) {
            this.contactChangedPresence(ImType.FUSION.value(), oldusername, PresenceType.OFFLINE.value());
         }
      }

      ContactData oldContactData = this.contactList.removeContact(contact.id);
      ContactData contactData = new ContactData(contact);
      if (newContactUserProxy != null) {
         if (log.isDebugEnabled()) {
            log.debug("we received a newContactUserProxy [" + newContactUserProxy + "], using that to determine presence");
         }

         contactData.fusionPresence = PresenceType.fromValue(newContactUserProxy.getOverallFusionPresence(contactData.username));
      } else {
         if (log.isDebugEnabled()) {
            log.debug("we did NOT receive a newContactUserProxy, setting status to offline");
         }

         contactData.fusionPresence = PresenceType.OFFLINE;
      }

      this.contactList.copyPresenceForExistingIMContacts(oldContactData, contactData);
      this.contactList.addContact(contactData, contactListVersion);
      this.notifySessionsOfNewContact(contactData, contactListVersion, false);
      return contactData.getPresenceAndCapabilty();
   }

   public boolean onSendBinaryData() throws FusionException {
      ChatPrivacyController.verifyCanSendBinaryData(this.chatUserData.getNewUserData());
      return this.rateLimit.onSend();
   }

   public boolean onReceiveBinaryData() {
      return this.rateLimit.onReceive();
   }

   public void stopBroadcastingTo(String username) {
      this.chatUserData.removeFromBroadcastList(username);

      try {
         if (log.isDebugEnabled()) {
            log.debug("our [" + this.getUsername() + "] contact [" + username + "] was removed, setting presence for contact to " + PresenceType.OFFLINE.toString() + "]");
         }

         this.contactChangedPresence(ImType.FUSION.value(), username, PresenceType.OFFLINE.value());
      } catch (FusionException var3) {
      }

   }

   public void rejectContactRequest(String inviterUsername) {
      if (log.isDebugEnabled()) {
         log.debug("[" + inviterUsername + "] rejecting contact request from user [" + this.getUsername() + "], B side");
      }

      int outstandingPendingContacts = this.chatUserData.rejectContactRequest(inviterUsername);
      this.sessionList.contactRequestRejected(inviterUsername, outstandingPendingContacts);
   }

   public void addToContactAndBroadcastLists(ContactDataIce contact, int contactListVersion) {
      if (!this.purging) {
         ContactData newContact = new ContactData(contact);
         this.chatUserData.addToBroadcastList(newContact.fusionUsername);
         this.contactList.addContact(newContact, contactListVersion);
         this.notifySessionsOfNewContact(newContact, contactListVersion, true);
      }
   }

   public void contactChangedDetail(ContactDataIce contact, int contactListVersion) {
      if (!this.purging) {
         ContactData newContact = new ContactData(contact);
         this.contactList.changedDetail(newContact, contactListVersion);
         if (newContact.fusionUsername != null) {
            newContact.fusionPresence = this.contactList.findOverallFusionPresenceForContact(this.objectManager, newContact, this.username);
         }

         this.notifySessionsOfNewContact(newContact, contactListVersion, false);
      }
   }

   public void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone) throws FusionException {
      if (!this.handleUserErrorResponse(this.userCanContactMe(requestingUsername, (MessageDataIce)null))) {
         if (this.chatUserData.getAnonymousCallSetting() == UserSettingData.AnonymousCallEnum.DISABLED) {
            throw new FusionException("The user you selected does not want to be called by people who don't know their phone number");
         } else {
            boolean found = this.sessionList.putAnonymousCallNotifiaction(requestingUsername, requestingMobilePhone);
            if (!found) {
               throw new FusionException(this.getUsername() + " is using a version of client which does not support call by name");
            }
         }
      }
   }

   public void putEvent(UserEventIce event) {
      if (log.isDebugEnabled()) {
         log.debug("sending out event to user [" + this.getUsername() + "]");
      }

      this.sessionList.putEvent(event);
   }

   public void putServerQuestion(String message, String url) {
      if (!this.silencedNotifications) {
         this.sessionList.putServerQuestion(message, url);
      }
   }

   public void putAlertMessage(String message, String title, short timeout) {
      if (!this.silencedNotifications) {
         this.sessionList.putAlertMessage(message, title, timeout);
      }
   }

   public void privateChatNowAGroupChat(String groupChatID, String creator) {
      this.sessionList.putPrivateChatNowAGroupChat(groupChatID, creator);
   }

   public String[] getBroadcastList() {
      return this.chatUserData.getBroadcastList();
   }

   public String[] getBlockListFromUsernames(String[] usernames) {
      return this.chatUserData.getBlocklistFromUsernames(usernames);
   }

   public void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency) {
      if (!this.purging) {
         this.chatUserData.updateBalance(balance, fundedBalance, currency.code);
         this.sessionList.accountBalanceChanged(balance, fundedBalance, currency);
      }
   }

   public void userReputationChanged() {
      if (this.chatUserReputation == null) {
         log.error("Called userReputationChanged on a not yet loaded ChatUser, username=" + this.username);
      } else {
         try {
            this.chatUserReputation.reloadReputationData();
         } catch (Exception var2) {
            log.error("Unable to reload reputation data for user=" + this.username + " cause=" + var2, var2);
         }

      }
   }

   public void userDetailChanged(UserDataIce user) {
      if (!this.purging) {
         this.chatUserData.update(user);

         try {
            Credential[] otherIMCredentials = this.objectManager.getUserCredentials(this.chatUserData.getUserID(), PasswordType.OTHER_IM_TYPES);
            this.chatExternal.setIMCredentials(otherIMCredentials);
         } catch (FusionException var3) {
            log.warn("Unable to reload otherIMCredentials for " + user.username, var3);
         }

      }
   }

   public void contactGroupDetailChanged(ContactGroupDataIce contactGroup, int contactListVersion) {
      if (!this.purging) {
         ContactGroupData newContactGroup = new ContactGroupData(contactGroup);
         this.contactList.updateGroup(newContactGroup);
         this.contactList.setContactListVersion(contactListVersion);
         this.sessionList.notifySessionsOfNewContactGroup(newContactGroup, contactListVersion);
      }
   }

   public void contactGroupDeleted(int contactGroupID, int contactListVersion) {
      if (!this.purging) {
         this.contactList.removeGroup(contactGroupID);
         this.contactList.setContactListVersion(contactListVersion);
         this.sessionList.notifySessionsOfRemovedContactGroup(contactGroupID, contactListVersion);
      }
   }

   public UserDataIce getUserData() {
      return this.chatUserData.getUserDataIce();
   }

   public boolean supportsBinaryMessage() {
      return this.sessionList.supportsBinaryMessage();
   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol) throws FusionException {
      this.sessionList.putWebCallNotification(source, destination, gateway, gatewayName, protocol);
   }

   public SessionPrx[] getSessions() {
      return this.objectManager.findSessionsPrx(this.sessionList.getSessions());
   }

   public int getUnreadEmailCount() {
      return this.chatUserData.getUnreadEmailCount();
   }

   public void emailNotification(int unreadEmailCount) {
      this.chatUserData.setUnreadEmailCount(unreadEmailCount);
      this.sessionList.emailNotification(unreadEmailCount);
   }

   public String[] getEmoticonHotKeys() {
      return this.emoticonList.getEmoticonHotKeys();
   }

   public String[] getEmoticonAlternateKeys() {
      return this.emoticonList.getEmoticonAlternateKeys();
   }

   private void putSerializedPacket(byte[] packet) throws FusionException {
      this.sessionList.putSerializedPacket(packet);
   }

   public void themeChanged(String themeLocation) {
      this.sessionList.themeChanged(themeLocation);
   }

   public void emoticonPackPurchased(int emoticonPackId) {
      try {
         this.emoticonList.packPurchased(emoticonPackId);
         this.sessionList.emoticonsChanged(this.emoticonList.getEmoticonHotKeys(), this.emoticonList.getEmoticonAlternateKeys());
      } catch (Exception var3) {
         log.warn("Unable to reload emoticons for the user '" + this.username + "'. Exception: " + var3.getMessage());
      }

   }

   public ChatSession createSession(String sessionID, int presence, int deviceType, int connectionType, int imType, int port, int remotePort, String remoteAddress, String mobileDevice, String userAgent, short clientVersion, String language, ConnectionPrx connectionProxy) throws FusionException {
      if (!this.loginSemaphore.tryAcquire()) {
         log.warn("[" + this.getUsername() + "] exceeded maximum concurrent login");
         throw new FusionException("You cannot login at this time. Please try again");
      } else {
         ChatSession var17;
         try {
            ChatSession session = null;
            synchronized(this) {
               if (this.purging) {
                  throw new FusionException("The user is being purged");
               }

               int sessionCount = this.sessionList.verifyCanCreateSession(sessionID);
               ChatPrivacyController.verifyCanCreateSession(this.getUsername(), this.chatUserData.isChatRoomAdmin(), sessionCount);
               session = this.objectManager.createSession(this, sessionID, presence, deviceType, connectionType, imType, port, remotePort, remoteAddress, mobileDevice, userAgent, clientVersion, language, connectionProxy);
               this.sessionList.addSession(sessionID, session);
            }

            this.updateOverallFusionPresence();
            Set<String> userList = this.chatUserData.getPendingContacts();
            Set<String> sentUsers = session.checkOutstandingContactRequests(userList);
            this.contactList.updateOutstandingContactRequests(sentUsers);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.NOTIFICATIONS_COUNTER_ON_LOGIN)) {
               try {
                  this.objectManager.getIcePrxFinder().getUserNotificationServiceProxy().sendNotificationCounterToUser(this.chatUserData.getUserID());
               } catch (Exception var23) {
                  log.error("Failed to push pending notifications: ", var23);
               }
            }

            this.metrics.request();
            if (log.isDebugEnabled()) {
               log.debug("Added the Session object '" + sessionID + "' to the user '" + this.getUsername() + "'");
            }

            var17 = session;
         } finally {
            this.loginSemaphore.release();
         }

         return var17;
      }
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy) throws FusionException {
      return this.emoteCommandStates.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, (ChatSourceUser)this);
   }

   public final SessionPrx findSessionPrx(String sessionID) {
      return this.objectManager.findSessionPrx(sessionID);
   }

   public final SessionPrx getSessionPrx(String sessionID) {
      return this.objectManager.findSessionPrx(sessionID);
   }

   public int getUserID() {
      return this.chatUserData.getUserID();
   }

   public UserData.TypeEnum getUserType() {
      return this.chatUserData.getUserType();
   }

   public double getBalance() {
      return this.chatUserData.getBalance();
   }

   public String getPassword() {
      return this.chatUserData.getPassword();
   }

   public Integer getCountryID() {
      return this.chatUserData.getCountryID();
   }

   public void sessionSendOtherIMMessage(ChatSession source, MessageDataIce message) throws FusionException {
      this.chatExternal.verifyIsLoggedIn(MessageType.fromValue(message.type));
      if (message.messageDestinations.length != 1) {
         FusionException fe = new FusionException("The " + MessageType.fromValue(message.type).toString() + " message must have a single destination");
         log.warn(fe.message);
         throw fe;
      } else {
         boolean isNewMimeMessage = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED) && !StringUtil.iceIsBlank(message.mimeType);
         FusionException fe;
         if (!isNewMimeMessage && message.contentType != MessageData.ContentTypeEnum.TEXT.value()) {
            try {
               MessageData messageData = new MessageData(message);
               if (message.contentType == MessageData.ContentTypeEnum.EXISTING_FILE.value()) {
                  MIS misBean = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                  FileData fileData = misBean.getFile(messageData.messageText);
                  if (fileData == null) {
                     throw new Exception("Invalid file ID " + messageData.messageText);
                  }

                  messageData.binaryData = new byte[fileData.size];
               } else {
                  message.messageText = this.storeImage(messageData.source, messageData.binaryData);
               }
            } catch (Exception var7) {
               fe = new FusionException("Unable to send the picture. Reason: " + var7.getMessage());
               log.warn(fe.message);
               throw fe;
            }
         }

         MessageDestinationData.TypeEnum messageDestinationType = MessageDestinationData.TypeEnum.fromValue(message.messageDestinations[0].type);
         if (messageDestinationType != MessageDestinationData.TypeEnum.INDIVIDUAL && messageDestinationType != MessageDestinationData.TypeEnum.GROUP) {
            fe = new FusionException("The destination type " + MessageDestinationData.TypeEnum.fromValue(message.messageDestinations[0].type).toString() + " for " + MessageType.fromValue(message.type).toString() + " messages is not supported");
            log.warn(fe.message);
            throw fe;
         } else {
            MessageData messageData = new MessageData(message);

            try {
               if (!isNewMimeMessage && message.contentType != MessageData.ContentTypeEnum.TEXT.value()) {
                  messageData.messageText = messageData.source + " has sent you a picture. You can view the picture at " + SystemProperty.get("ImageServerURL") + messageData.messageText;
                  messageData.contentType = MessageData.ContentTypeEnum.TEXT;
               }

               this.chatExternal.sendMessage(messageData);
            } catch (Exception var8) {
               if (log.isDebugEnabled()) {
                  log.debug("Unable to send " + messageData.type.toString() + " message from the Fusion user '" + message.source + "' to the destination '" + message.messageDestinations[0].destination + "'. Exception: " + var8.getMessage());
               }

               throw new FusionException("Unable to send message. " + var8.getMessage());
            }

            this.chatExternal.logMessage(message, messageData.type, this.chatUserData.getCountryID());
            this.chatExternal.triggerReward(this.chatUserData.getUserData(), messageData.type, ThirdPartyInstantMessageTrigger.EventTypeEnum.MESSAGE_SENT);
            if (!isNewMimeMessage && message.contentType != MessageData.ContentTypeEnum.TEXT.value() && source.isV3Device()) {
               source.putFileReceived(message);
            }

         }
      }
   }

   public boolean validatePutMessage(MessageDataIce message) {
      if (!MessageData.hasDestinations(message)) {
         return true;
      } else {
         if (!MessageData.isFusionMessage(message) || !MessageData.isMessageToAChatRoom(message) && !MessageData.isMessageToAGroupChat(message)) {
            Integer sourceContactID = this.contactList.findSourceContactID(MessageType.fromValue(message.type), message.source);
            if (sourceContactID != null) {
               message.sourceContactID = sourceContactID;
            }
         } else if (!MessageData.isSystemMessage(message) && this.contactList.userCanContactMe(message.source, message, this.silencedNotifications).error) {
            return false;
         }

         return true;
      }
   }

   public ArrayList<MessageDataIce> sessionValidatePutMessages(MessageDataIce[] messages) {
      ArrayList<MessageDataIce> messageList = new ArrayList();
      MessageDataIce[] arr$ = messages;
      int len$ = messages.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MessageDataIce message = arr$[i$];
         if (MessageData.hasDestinations(message)) {
            if (!MessageData.isMessageToAChatRoom(message) && !MessageData.isMessageToAGroupChat(message)) {
               Integer sourceContactID = null;
               sourceContactID = this.contactList.findSourceContactID(MessageType.fromValue(message.type), message.source);
               if (sourceContactID != null) {
                  message.sourceContactID = sourceContactID;
               }
            } else if (this.contactList.userCanContactMe(message.source, message, this.silencedNotifications).error) {
               continue;
            }
         }

         messageList.add(message);
      }

      return messageList;
   }

   public MessageDestinationData.TypeEnum sessionSendFusionMessage(ChatSession source, MessageDataIce message) throws FusionException {
      if (ChatUser.ChatData.isVerifiedUserRequired(message.contentType)) {
         this.onSendBinaryData();
      }

      if (message.messageDestinations.length != 1) {
         FusionException fe = new FusionException("The Fusion message must have a single destination");
         log.warn(fe.message);
         throw fe;
      } else {
         String displayPicture = this.chatUserData.getAvatar();
         if (displayPicture != null) {
            message.sourceDisplayPicture = displayPicture;
         }

         message.emoticonKeys = (String[])this.emoticonList.getEmoticonKeysInMessage(message.messageText).toArray(new String[0]);
         MessageDestinationData.TypeEnum messageDestinationType = MessageDestinationData.TypeEnum.fromValue(message.messageDestinations[0].type);
         return messageDestinationType;
      }
   }

   public void prepareForPurge() {
   }

   public void applyMessageColor(MessageDataIce message) {
      this.chatUserData.applyMessageColor(message);
   }

   public void rewardTriggerPrivateMessage() {
      try {
         UserData userData = this.chatUserData.getNewUserData();
         PrivateMessageSentTrigger trigger = new PrivateMessageSentTrigger(userData);
         trigger.quantityDelta = 1;
         trigger.amountDelta = 0.0D;
         RewardCentre.getInstance().sendTrigger(trigger);
      } catch (Exception var3) {
         log.warn("Unable to notify reward system. Exception: " + var3.toString());
      }

   }

   private SessionCachePrx getSessionCachePrx() {
      return this.objectManager.getSessionCachePrx();
   }

   private UserNotificationServicePrx getUserNotificationServiceProxy() throws Exception {
      return this.objectManager.getIcePrxFinder().getUserNotificationServiceProxy();
   }

   public ChatUserState getState() {
      ChatUserState state = new ChatUserState();
      state.username = this.username;
      state.sessions = this.sessionList.getState();
      return state;
   }

   public void setCurrentChatListGroupChatSubset(ChatList newCCL) {
      ChatList currentCCL = (ChatList)this.currentChatListGroupChatSubset.get();
      if (currentCCL == null || newCCL.getVersion() > currentCCL.getVersion()) {
         this.currentChatListGroupChatSubset.compareAndSet(currentCCL, newCCL);
      }

   }

   public void putMessageStatusEvent(MessageStatusEvent mse) {
      this.sessionList.putMessageStatusEvent(mse);
   }

   private class ChatUserMetrics {
      RequestCounter requestCounter;

      ChatUserMetrics(RequestCounter requestCounter) {
         this.requestCounter = requestCounter;
      }

      public void request() {
         this.requestCounter.add();
      }
   }

   private class ChatUserGroupChats {
      private AtomicInteger numGroupChatsInProgress = new AtomicInteger(0);
      private int maxConcurrentGroupChatsPerUser;

      public ChatUserGroupChats(ChatObjectManagerUser objectManager) {
         this.maxConcurrentGroupChatsPerUser = objectManager.getProperties().getPropertyAsIntWithDefault("MaxConcurrentGroupChatsPerUser", 5);
      }

      public void leaving() {
         this.numGroupChatsInProgress.decrementAndGet();
      }

      public void entering(boolean isCreator) throws FusionException {
         if (this.numGroupChatsInProgress.incrementAndGet() > this.maxConcurrentGroupChatsPerUser) {
            this.numGroupChatsInProgress.decrementAndGet();
            if (isCreator) {
               throw new FusionException("You have already joined the maximum number of group chats allowed this session");
            } else {
               throw new FusionException(ChatUser.this.username + " has already joined the maximum number of group chats allowed this session");
            }
         }
      }
   }

   private static class ChatData {
      public static final List<Integer> CONTENT_TYPES_REQUIRING_VERIFIED_USER;
      public static boolean logMessagesToFile;

      public static boolean isVerifiedUserRequired(int contentType) {
         return CONTENT_TYPES_REQUIRING_VERIFIED_USER.contains(contentType);
      }

      static {
         CONTENT_TYPES_REQUIRING_VERIFIED_USER = Arrays.asList(MessageData.ContentTypeEnum.AUDIO.value(), MessageData.ContentTypeEnum.EXISTING_FILE.value(), MessageData.ContentTypeEnum.IMAGE.value(), MessageData.ContentTypeEnum.VIDEO.value());
      }
   }
}
