package com.projectgoth.fusion.objectcache;

import Ice.Current;
import com.projectgoth.fusion.chatsync.ChatList;
import com.projectgoth.fusion.chatsync.CurrentChatList;
import com.projectgoth.fusion.chatsync.MessageStatusEvent;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.ice.IceAmdInvoker;
import com.projectgoth.fusion.slice.AMD_User_createSession;
import com.projectgoth.fusion.slice.AMD_User_otherIMLogin;
import com.projectgoth.fusion.slice.AMD_User_putMessage;
import com.projectgoth.fusion.slice.ChatListIce;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.ContactGroupDataIce;
import com.projectgoth.fusion.slice.ContactList;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.CurrencyDataIce;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.MessageStatusEventIce;
import com.projectgoth.fusion.slice.PresenceAndCapabilityIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserErrorResponse;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice._UserDisp;

public class UserRpcI extends _UserDisp {
   private ChatUser user;
   private ChatObjectManagerUser objectManager;

   public UserRpcI(ChatObjectManagerUser objectManager, ChatUser user) {
      this.objectManager = objectManager;
      this.user = user;
   }

   public void createSession_async(final AMD_User_createSession cb, final String sessionID, final int presence, final int deviceType, final int connectionType, final int imType, final int port, final int remotePort, final String remoteAddress, final String mobileDevice, final String userAgent, final short clientVersion, final String language, final ConnectionPrx connectionProxy, Current __current) throws FusionException {
      IceAmdInvoker ivk = new ObjectCacheIceAmdInvoker() {
         SessionPrx sessionPrx;

         public void payload() throws Exception {
            ChatSession session = UserRpcI.this.user.createSession(sessionID, presence, deviceType, connectionType, imType, port, remotePort, remoteAddress, mobileDevice, userAgent, clientVersion, language, connectionProxy);
            this.sessionPrx = UserRpcI.this.objectManager.onSessionCreated(session);
         }

         public void ice_response() {
            cb.ice_response(this.sessionPrx);
         }

         public void ice_exception(Exception e) {
            cb.ice_exception(e);
         }

         public String getLogContext() {
            return "User.createSession, sessionID=" + sessionID;
         }
      };
      ivk.invoke();
   }

   public SessionPrx[] getSessions(Current __current) {
      return this.user.getSessions();
   }

   public void disconnect(String reason, Current __current) {
      this.user.disconnect(reason);
   }

   public void disconnectAndSuspend(String reason, String reference, Current __current) {
      this.user.disconnectAndSuspend(reason, reference);
   }

   public void disconnectFlooder(String reason, Current __current) {
      this.user.disconnectFlooder(reason);
   }

   public int getOverallFusionPresence(String requestingUsername, Current __current) {
      return this.user.getOverallFusionPresence(requestingUsername);
   }

   public void putMessage_async(final AMD_User_putMessage cb, final MessageDataIce message, Current __current) throws FusionException {
      IceAmdInvoker ivk = new ObjectCacheIceAmdInvoker() {
         public void payload() throws Exception {
            UserRpcI.this.user.putMessage(message);
         }

         public void ice_response() {
            cb.ice_response();
         }

         public void ice_exception(Exception e) {
            cb.ice_exception(e);
         }

         public String getLogContext() {
            return "User.putMessage, message.source=" + message.source;
         }
      };
      ivk.invoke();
   }

   public boolean privateChattedWith(String username, Current __current) {
      return this.user.hasPrivateChattedWith(username);
   }

   public UserErrorResponse userCanContactMe(String username, MessageDataIce message, Current __current) {
      return this.user.userCanContactMe(username, message);
   }

   public void contactChangedPresence(int imType, String source, int presence, Current __current) throws FusionException {
      this.user.contactChangedPresence(imType, source, presence);
   }

   public void contactChangedDisplayPicture(String source, String displayPicture, long timeStamp, Current __current) throws FusionException {
      this.user.contactChangedDisplayPicture(source, displayPicture, timeStamp);
   }

   public void contactChangedStatusMessage(String source, String statusMessage, long timeStamp, Current __current) throws FusionException {
      this.user.contactChangedStatusMessage(source, statusMessage, timeStamp);
   }

   public void contactChangedPresenceOneWay(int imType, String source, int presence, Current __current) {
      try {
         this.user.contactChangedPresence(imType, source, presence);
      } catch (Exception var6) {
      }

   }

   public void contactChangedDisplayPictureOneWay(String source, String displayPicture, long timeStamp, Current __current) {
      try {
         this.user.contactChangedDisplayPicture(source, displayPicture, timeStamp);
      } catch (Exception var7) {
      }

   }

   public void contactChangedStatusMessageOneWay(String source, String statusMessage, long timeStamp, Current __current) {
      try {
         this.user.contactChangedStatusMessage(source, statusMessage, timeStamp);
      } catch (Exception var7) {
      }

   }

   public ContactList getContactList(Current __current) {
      return this.user.getContactList();
   }

   public ContactDataIce[] getContacts(Current __current) {
      return this.user.getContacts();
   }

   public ContactDataIce[] getOtherIMContacts(Current __current) {
      return this.user.getOtherIMContacts();
   }

   public String[] getOtherIMConferenceParticipants(int imType, String otherIMConferenceID, Current __current) {
      return this.user.getOtherIMConferenceParticipants(imType, otherIMConferenceID);
   }

   public boolean isOnContactList(String contactUsername, Current __current) {
      return this.user.isOnContactList(contactUsername);
   }

   public boolean isOnBlockList(String contactUsername, Current __current) {
      return this.user.isOnBlockList(contactUsername);
   }

   public void contactDetailChanged(ContactDataIce contact, int contactListVersion, Current __current) {
      this.user.contactChangedDetail(contact, contactListVersion);
   }

   public void contactGroupDeleted(int contactGroupID, int contactListVersion, Current __current) {
      this.user.contactGroupDeleted(contactGroupID, contactListVersion);
   }

   public void contactGroupDetailChanged(ContactGroupDataIce contactGroup, int contactListVersion, Current __current) {
      this.user.contactGroupDetailChanged(contactGroup, contactListVersion);
   }

   public void userDetailChanged(UserDataIce userData, Current __current) {
      this.user.userDetailChanged(userData);
   }

   public void userReputationChanged(Current __current) {
      this.user.userReputationChanged();
   }

   public void userDisplayPictureChanged(String displayPicture, long timeStamp, Current __current) {
      this.user.userDisplayPictureChanged(displayPicture, timeStamp);
   }

   public void userStatusMessageChanged(String statusMessage, long timeStamp, Current __current) {
      this.user.userStatusMessageChanged(statusMessage, timeStamp);
   }

   public void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency, Current __current) {
      this.user.accountBalanceChanged(balance, fundedBalance, currency);
   }

   public UserDataIce getUserData(Current __current) {
      return this.user.getUserData();
   }

   public void otherIMLogin_async(final AMD_User_otherIMLogin cb, final int imType, final int presence, final boolean showOfflineContacts, Current __current) throws FusionException {
      IceAmdInvoker ivk = new ObjectCacheIceAmdInvoker() {
         public void payload() throws Exception {
            UserRpcI.this.user.otherIMLogin(imType, presence, showOfflineContacts);
         }

         public void ice_response() {
            cb.ice_response();
         }

         public void ice_exception(Exception e) {
            cb.ice_exception(e);
         }

         public String getLogContext() {
            return "User.otherIMLogin, imType=" + imType;
         }
      };
      ivk.invoke();
   }

   public void otherIMLogout(int imType, Current __current) {
      this.user.otherIMLogout(imType);
   }

   public void otherIMRemoved(int imType, Current __current) {
      this.user.otherIMRemoved(imType);
   }

   public String[] getBlockList(Current __current) {
      return this.user.getBlockList();
   }

   public String[] getBlockListFromUsernames(String[] usernames, Current __current) {
      return this.user.getBlockListFromUsernames(usernames);
   }

   public String[] getBroadcastList(Current __current) {
      return this.user.getBroadcastList();
   }

   public void otherIMSendMessage(int imType, String otherIMUsername, String message, Current __current) throws FusionException {
      this.user.otherIMSendMessage(imType, otherIMUsername, message);
   }

   public String otherIMInviteToConference(int imType, String otherIMConferenceID, String otherIMUsername, Current __current) throws FusionException {
      return this.user.otherIMInviteToConference(imType, otherIMConferenceID, otherIMUsername);
   }

   public void otherIMLeaveConference(int imType, String otherIMConferenceID, Current __current) {
      this.user.otherIMLeaveConference(imType, otherIMConferenceID);
   }

   public void otherIMAddContact(int imType, String otherIMUsername, Current __current) throws FusionException {
      this.user.otherIMAddContact(imType, otherIMUsername);
   }

   public void otherIMRemoveContact(int contactId, Current __current) throws FusionException {
      this.user.otherIMRemoveContact(contactId);
   }

   public void privateChatNowAGroupChat(String groupChatID, String creator, Current __current) throws FusionException {
      this.user.privateChatNowAGroupChat(groupChatID, creator);
   }

   public void putEvent(UserEventIce event, Current __current) throws FusionException {
      this.user.putEvent(event);
   }

   public void putAlertMessage(String message, String title, short timeout, Current __current) throws FusionException {
      this.user.putAlertMessage(message, title, timeout);
   }

   public void putServerQuestion(String message, String url, Current __current) throws FusionException {
      this.user.putServerQuestion(message, url);
   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol, Current __current) throws FusionException {
      this.user.putWebCallNotification(source, destination, gateway, gatewayName, protocol);
   }

   public void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone, Current __current) throws FusionException {
      this.user.putAnonymousCallNotification(requestingUsername, requestingMobilePhone);
   }

   public void putFileReceived(MessageDataIce messageIce, Current __current) throws FusionException {
      this.user.putFileReceived(messageIce);
   }

   public int[] getConnectedOtherIMs(Current __current) {
      return this.user.getConnectedOtherIMs();
   }

   public boolean supportsBinaryMessage(Current __current) {
      return this.user.supportsBinaryMessage();
   }

   public int getUnreadEmailCount(Current __current) {
      return this.user.getUnreadEmailCount();
   }

   public void emailNotification(int unreadEmailCount, Current __current) {
      this.user.emailNotification(unreadEmailCount);
   }

   public String[] getEmoticonHotKeys(Current __current) {
      return this.user.getEmoticonHotKeys();
   }

   public String[] getEmoticonAlternateKeys(Current __current) {
      return this.user.getEmoticonAlternateKeys();
   }

   public void emoticonPackActivated(int emoticonPackId, Current __current) {
      this.user.emoticonPackPurchased(emoticonPackId);
   }

   public void emoticonPackPurchased(int emoticonPackId, Current __current) {
      this.user.emoticonPackPurchased(emoticonPackId);
   }

   public void themeChanged(String themeLocation, Current __current) throws FusionException {
      this.user.themeChanged(themeLocation);
   }

   public ContactDataIce acceptContactRequest(ContactDataIce contact, UserPrx contactProxy, int inviterContactListVersion, int inviteeContactListVersion, Current __current) {
      return this.user.acceptContactRequest(contact, contactProxy, inviterContactListVersion, inviteeContactListVersion);
   }

   public void notifySessionsOfNewContact(ContactDataIce newContact, int contactListVersion, boolean guaranteedIsNew, Current __current) {
      this.user.notifySessionsOfNewContact(new ContactData(newContact), contactListVersion, guaranteedIsNew);
   }

   public void addContact(ContactDataIce contact, int contactListVersion, Current __current) {
      this.user.addContact(contact, contactListVersion);
   }

   public void addToContactAndBroadcastLists(ContactDataIce contact, int contactListVersion, Current __current) {
      this.user.addToContactAndBroadcastLists(contact, contactListVersion);
   }

   public void addPendingContact(String username, Current __current) {
      this.user.addPendingContact(username);
   }

   public void blockUser(String blockUsername, int contactListVersion, Current __current) {
      this.user.blockUser(blockUsername, contactListVersion);
   }

   public void contactRequestWasRejected(String contactRequestUsername, int contactListVersion, Current __current) {
      this.user.contactRequestWasRejected(contactRequestUsername, contactListVersion);
   }

   public void rejectContactRequest(String inviterUsername, Current __current) {
      this.user.rejectContactRequest(inviterUsername);
   }

   public ContactDataIce contactRequestWasAccepted(ContactDataIce contact, String statusMessage, String displayPicture, int overallFusionPresence, int contactListVersion, Current __current) {
      return this.user.contactRequestWasAccepted(contact, statusMessage, displayPicture, overallFusionPresence, contactListVersion);
   }

   public void removeContact(int contactId, int contactListVersion, Current __current) {
      this.user.removeContact(contactId, contactListVersion);
   }

   public void stopBroadcastingTo(String username, Current __current) {
      this.user.stopBroadcastingTo(username);
   }

   public void unblockUser(String username, Current __current) {
      this.user.unblockUser(username);
   }

   public PresenceAndCapabilityIce contactUpdated(ContactDataIce contact, String oldusername, boolean acceptedContactRequest, boolean changedFusionContact, UserPrx newContactUserProxy, int contactListVersion, Current __current) throws FusionException {
      return this.user.contactUpdated(contact, oldusername, acceptedContactRequest, changedFusionContact, newContactUserProxy, contactListVersion);
   }

   public void newUserContactUpdated(String usernameThatWasModified, boolean acceptedContactRequest, Current __current) {
      this.user.contactUpdated(usernameThatWasModified, acceptedContactRequest);
   }

   public void oldUserContactUpdated(String usernameThatWasModified, Current __current) throws FusionException {
      this.user.contactUpdated(usernameThatWasModified);
   }

   public void anonymousCallSettingChanged(int setting, Current __current) {
      this.user.anonymousCallSettingsChanged(setting);
   }

   public void messageSettingChanged(int setting, Current __current) {
      this.user.messageSettingsChanged(setting);
   }

   public Credential[] getOtherIMCredentials(Current __current) {
      return this.user.getOtherIMCredentials();
   }

   public int getContactListVersion(Current __current) {
      return this.user.getContactListVersion();
   }

   public void addToCurrentChatroomList(String chatroom, Current __current) throws FusionException {
      this.user.addToCurrentChatroomList(chatroom);
   }

   public void removeFromCurrentChatroomList(String chatroom, Current __current) {
      this.user.removeFromCurrentChatroomList(chatroom);
   }

   public String[] getCurrentChatrooms(Current __current) {
      return this.user.getCurrentChatrooms();
   }

   public int getReputationDataLevel(Current __current) {
      return this.user.getReputationDataLevel();
   }

   public void pushNotification(Message message, Current __current) throws FusionException {
      this.user.pushNotification(message);
   }

   public int getOnlineContactsCount(Current __current) {
      return this.user.getOnlineContactsCount();
   }

   public void enteringGroupChat(boolean isCreator, Current __current) throws FusionException {
      this.user.enteringGroupChat(isCreator);
   }

   public void leavingGroupChat(Current __current) {
      this.user.leavingGroupChat();
   }

   public void notifyUserLeftGroupChat(String groupChatId, String username, Current __current) {
      this.user.notifyUserLeftGroupChat(groupChatId, username);
   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted, Current __current) {
      this.user.notifyUserJoinedGroupChat(groupChatId, username, isMuted);
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Current __current) throws FusionException {
      return this.user.executeEmoteCommandWithState(emoteCommand, message, sessionProxy);
   }

   public void purgeExpiredSessions() {
      this.user.purgeExpiredSessions();
   }

   public void prepareForPurge() {
      this.user.dispose();
   }

   public void setCurrentChatListGroupChatSubset(ChatListIce cclSubsetIce, Current __current) {
      ChatList cclSubset = new CurrentChatList(cclSubsetIce);
      this.user.setCurrentChatListGroupChatSubset(cclSubset);
   }

   public void putMessageStatusEvent(MessageStatusEventIce mseIce, Current __current) throws FusionException {
      MessageStatusEvent mse = new MessageStatusEvent(mseIce);
      this.user.putMessageStatusEvent(mse);
   }

   public SessionPrx findSession(String sessionID, Current __current) throws FusionException {
      return this.user.findSessionPrx(sessionID);
   }
}
