package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _UserTie extends _UserDisp implements TieBase {
   private _UserOperations _ice_delegate;

   public _UserTie() {
   }

   public _UserTie(_UserOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_UserOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _UserTie) ? false : this._ice_delegate.equals(((_UserTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public ContactDataIce acceptContactRequest(ContactDataIce contact, UserPrx contactProxy, int inviterContactListVersion, int inviteeContactListVersion, Current __current) {
      return this._ice_delegate.acceptContactRequest(contact, contactProxy, inviterContactListVersion, inviteeContactListVersion, __current);
   }

   public void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency, Current __current) {
      this._ice_delegate.accountBalanceChanged(balance, fundedBalance, currency, __current);
   }

   public void addContact(ContactDataIce contact, int contactListVersion, Current __current) {
      this._ice_delegate.addContact(contact, contactListVersion, __current);
   }

   public void addPendingContact(String username, Current __current) {
      this._ice_delegate.addPendingContact(username, __current);
   }

   public void addToContactAndBroadcastLists(ContactDataIce contact, int contactListVersion, Current __current) {
      this._ice_delegate.addToContactAndBroadcastLists(contact, contactListVersion, __current);
   }

   public void addToCurrentChatroomList(String chatroom, Current __current) throws FusionException {
      this._ice_delegate.addToCurrentChatroomList(chatroom, __current);
   }

   public void anonymousCallSettingChanged(int setting, Current __current) {
      this._ice_delegate.anonymousCallSettingChanged(setting, __current);
   }

   public void blockUser(String username, int contactListVersion, Current __current) {
      this._ice_delegate.blockUser(username, contactListVersion, __current);
   }

   public void contactChangedDisplayPictureOneWay(String source, String displayPicture, long timeStamp, Current __current) {
      this._ice_delegate.contactChangedDisplayPictureOneWay(source, displayPicture, timeStamp, __current);
   }

   public void contactChangedPresenceOneWay(int imType, String source, int presence, Current __current) {
      this._ice_delegate.contactChangedPresenceOneWay(imType, source, presence, __current);
   }

   public void contactChangedStatusMessageOneWay(String source, String statusMessage, long timeStamp, Current __current) {
      this._ice_delegate.contactChangedStatusMessageOneWay(source, statusMessage, timeStamp, __current);
   }

   public void contactDetailChanged(ContactDataIce contact, int contactListVersion, Current __current) {
      this._ice_delegate.contactDetailChanged(contact, contactListVersion, __current);
   }

   public void contactGroupDeleted(int contactGroupID, int contactListVersion, Current __current) {
      this._ice_delegate.contactGroupDeleted(contactGroupID, contactListVersion, __current);
   }

   public void contactGroupDetailChanged(ContactGroupDataIce contactGroup, int contactListVersion, Current __current) {
      this._ice_delegate.contactGroupDetailChanged(contactGroup, contactListVersion, __current);
   }

   public ContactDataIce contactRequestWasAccepted(ContactDataIce contact, String statusMessage, String displayPicture, int overallFusionPresence, int contactListVersion, Current __current) {
      return this._ice_delegate.contactRequestWasAccepted(contact, statusMessage, displayPicture, overallFusionPresence, contactListVersion, __current);
   }

   public void contactRequestWasRejected(String contactRequestUsername, int contactListVersion, Current __current) {
      this._ice_delegate.contactRequestWasRejected(contactRequestUsername, contactListVersion, __current);
   }

   public PresenceAndCapabilityIce contactUpdated(ContactDataIce contact, String oldusername, boolean acceptedContactRequest, boolean changedFusionContact, UserPrx newContactUserProxy, int contactListVersion, Current __current) throws FusionException {
      return this._ice_delegate.contactUpdated(contact, oldusername, acceptedContactRequest, changedFusionContact, newContactUserProxy, contactListVersion, __current);
   }

   public void createSession_async(AMD_User_createSession __cb, String sessionID, int presence, int deviceType, int connectionType, int imType, int port, int remotePort, String IP, String mobileDevice, String userAgent, short clientVersion, String language, ConnectionPrx connectionProxy, Current __current) throws FusionException {
      this._ice_delegate.createSession_async(__cb, sessionID, presence, deviceType, connectionType, imType, port, remotePort, IP, mobileDevice, userAgent, clientVersion, language, connectionProxy, __current);
   }

   public void disconnect(String reason, Current __current) {
      this._ice_delegate.disconnect(reason, __current);
   }

   public void disconnectFlooder(String reason, Current __current) {
      this._ice_delegate.disconnectFlooder(reason, __current);
   }

   public void emailNotification(int unreadEmailCount, Current __current) {
      this._ice_delegate.emailNotification(unreadEmailCount, __current);
   }

   public void emoticonPackActivated(int emoticonPackId, Current __current) {
      this._ice_delegate.emoticonPackActivated(emoticonPackId, __current);
   }

   public void enteringGroupChat(boolean isCreator, Current __current) throws FusionException {
      this._ice_delegate.enteringGroupChat(isCreator, __current);
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Current __current) throws FusionException {
      return this._ice_delegate.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __current);
   }

   public SessionPrx findSession(String sid, Current __current) throws FusionException {
      return this._ice_delegate.findSession(sid, __current);
   }

   public String[] getBlockList(Current __current) {
      return this._ice_delegate.getBlockList(__current);
   }

   public String[] getBlockListFromUsernames(String[] usernames, Current __current) {
      return this._ice_delegate.getBlockListFromUsernames(usernames, __current);
   }

   public String[] getBroadcastList(Current __current) {
      return this._ice_delegate.getBroadcastList(__current);
   }

   public int[] getConnectedOtherIMs(Current __current) {
      return this._ice_delegate.getConnectedOtherIMs(__current);
   }

   public ContactList getContactList(Current __current) {
      return this._ice_delegate.getContactList(__current);
   }

   public int getContactListVersion(Current __current) {
      return this._ice_delegate.getContactListVersion(__current);
   }

   public ContactDataIce[] getContacts(Current __current) {
      return this._ice_delegate.getContacts(__current);
   }

   public String[] getCurrentChatrooms(Current __current) {
      return this._ice_delegate.getCurrentChatrooms(__current);
   }

   public String[] getEmoticonAlternateKeys(Current __current) {
      return this._ice_delegate.getEmoticonAlternateKeys(__current);
   }

   public String[] getEmoticonHotKeys(Current __current) {
      return this._ice_delegate.getEmoticonHotKeys(__current);
   }

   public int getOnlineContactsCount(Current __current) {
      return this._ice_delegate.getOnlineContactsCount(__current);
   }

   public String[] getOtherIMConferenceParticipants(int imType, String otherIMConferenceID, Current __current) {
      return this._ice_delegate.getOtherIMConferenceParticipants(imType, otherIMConferenceID, __current);
   }

   public ContactDataIce[] getOtherIMContacts(Current __current) {
      return this._ice_delegate.getOtherIMContacts(__current);
   }

   public Credential[] getOtherIMCredentials(Current __current) {
      return this._ice_delegate.getOtherIMCredentials(__current);
   }

   public int getOverallFusionPresence(String requestingUsername, Current __current) {
      return this._ice_delegate.getOverallFusionPresence(requestingUsername, __current);
   }

   public int getReputationDataLevel(Current __current) {
      return this._ice_delegate.getReputationDataLevel(__current);
   }

   public SessionPrx[] getSessions(Current __current) {
      return this._ice_delegate.getSessions(__current);
   }

   public int getUnreadEmailCount(Current __current) {
      return this._ice_delegate.getUnreadEmailCount(__current);
   }

   public UserDataIce getUserData(Current __current) {
      return this._ice_delegate.getUserData(__current);
   }

   public boolean isOnBlockList(String contactUsername, Current __current) {
      return this._ice_delegate.isOnBlockList(contactUsername, __current);
   }

   public boolean isOnContactList(String contactUsername, Current __current) {
      return this._ice_delegate.isOnContactList(contactUsername, __current);
   }

   public void leavingGroupChat(Current __current) {
      this._ice_delegate.leavingGroupChat(__current);
   }

   public void messageSettingChanged(int setting, Current __current) {
      this._ice_delegate.messageSettingChanged(setting, __current);
   }

   public void newUserContactUpdated(String usernameThatWasModified, boolean acceptedContactRequest, Current __current) {
      this._ice_delegate.newUserContactUpdated(usernameThatWasModified, acceptedContactRequest, __current);
   }

   public void notifySessionsOfNewContact(ContactDataIce newContact, int contactListVersion, boolean guaranteedIsNew, Current __current) {
      this._ice_delegate.notifySessionsOfNewContact(newContact, contactListVersion, guaranteedIsNew, __current);
   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted, Current __current) {
      this._ice_delegate.notifyUserJoinedGroupChat(groupChatId, username, isMuted, __current);
   }

   public void notifyUserLeftGroupChat(String groupChatId, String username, Current __current) {
      this._ice_delegate.notifyUserLeftGroupChat(groupChatId, username, __current);
   }

   public void oldUserContactUpdated(String usernameThatWasModified, Current __current) throws FusionException {
      this._ice_delegate.oldUserContactUpdated(usernameThatWasModified, __current);
   }

   public void otherIMAddContact(int imType, String otherIMUsername, Current __current) throws FusionException {
      this._ice_delegate.otherIMAddContact(imType, otherIMUsername, __current);
   }

   public String otherIMInviteToConference(int imType, String otherIMConferenceID, String otherIMUsername, Current __current) throws FusionException {
      return this._ice_delegate.otherIMInviteToConference(imType, otherIMConferenceID, otherIMUsername, __current);
   }

   public void otherIMLeaveConference(int imType, String otherIMConferenceID, Current __current) {
      this._ice_delegate.otherIMLeaveConference(imType, otherIMConferenceID, __current);
   }

   public void otherIMLogin_async(AMD_User_otherIMLogin __cb, int imType, int presence, boolean showOfflineContacts, Current __current) throws FusionException {
      this._ice_delegate.otherIMLogin_async(__cb, imType, presence, showOfflineContacts, __current);
   }

   public void otherIMLogout(int imType, Current __current) {
      this._ice_delegate.otherIMLogout(imType, __current);
   }

   public void otherIMRemoveContact(int contactId, Current __current) throws FusionException {
      this._ice_delegate.otherIMRemoveContact(contactId, __current);
   }

   public void otherIMRemoved(int imType, Current __current) {
      this._ice_delegate.otherIMRemoved(imType, __current);
   }

   public void otherIMSendMessage(int imType, String otherIMUsername, String message, Current __current) throws FusionException {
      this._ice_delegate.otherIMSendMessage(imType, otherIMUsername, message, __current);
   }

   public void privateChatNowAGroupChat(String groupChatID, String creator, Current __current) throws FusionException {
      this._ice_delegate.privateChatNowAGroupChat(groupChatID, creator, __current);
   }

   public boolean privateChattedWith(String username, Current __current) {
      return this._ice_delegate.privateChattedWith(username, __current);
   }

   public void pushNotification(Message msg, Current __current) throws FusionException {
      this._ice_delegate.pushNotification(msg, __current);
   }

   public void putAlertMessage(String message, String title, short timeout, Current __current) throws FusionException {
      this._ice_delegate.putAlertMessage(message, title, timeout, __current);
   }

   public void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone, Current __current) throws FusionException {
      this._ice_delegate.putAnonymousCallNotification(requestingUsername, requestingMobilePhone, __current);
   }

   public void putEvent(UserEventIce event, Current __current) throws FusionException {
      this._ice_delegate.putEvent(event, __current);
   }

   public void putFileReceived(MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.putFileReceived(message, __current);
   }

   public void putMessage_async(AMD_User_putMessage __cb, MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.putMessage_async(__cb, message, __current);
   }

   public void putMessageStatusEvent(MessageStatusEventIce mseIce, Current __current) throws FusionException {
      this._ice_delegate.putMessageStatusEvent(mseIce, __current);
   }

   public void putServerQuestion(String message, String url, Current __current) throws FusionException {
      this._ice_delegate.putServerQuestion(message, url, __current);
   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol, Current __current) throws FusionException {
      this._ice_delegate.putWebCallNotification(source, destination, gateway, gatewayName, protocol, __current);
   }

   public void rejectContactRequest(String inviterUsername, Current __current) {
      this._ice_delegate.rejectContactRequest(inviterUsername, __current);
   }

   public void removeContact(int contactid, int contactListVersion, Current __current) {
      this._ice_delegate.removeContact(contactid, contactListVersion, __current);
   }

   public void removeFromCurrentChatroomList(String chatroom, Current __current) {
      this._ice_delegate.removeFromCurrentChatroomList(chatroom, __current);
   }

   public void setCurrentChatListGroupChatSubset(ChatListIce ccl, Current __current) {
      this._ice_delegate.setCurrentChatListGroupChatSubset(ccl, __current);
   }

   public void stopBroadcastingTo(String username, Current __current) {
      this._ice_delegate.stopBroadcastingTo(username, __current);
   }

   public boolean supportsBinaryMessage(Current __current) {
      return this._ice_delegate.supportsBinaryMessage(__current);
   }

   public void themeChanged(String themeLocation, Current __current) throws FusionException {
      this._ice_delegate.themeChanged(themeLocation, __current);
   }

   public void unblockUser(String username, Current __current) {
      this._ice_delegate.unblockUser(username, __current);
   }

   public UserErrorResponse userCanContactMe(String username, MessageDataIce message, Current __current) {
      return this._ice_delegate.userCanContactMe(username, message, __current);
   }

   public void userDetailChanged(UserDataIce user, Current __current) {
      this._ice_delegate.userDetailChanged(user, __current);
   }

   public void userDisplayPictureChanged(String displayPicture, long timeStamp, Current __current) {
      this._ice_delegate.userDisplayPictureChanged(displayPicture, timeStamp, __current);
   }

   public void userReputationChanged(Current __current) {
      this._ice_delegate.userReputationChanged(__current);
   }

   public void userStatusMessageChanged(String statusMessage, long timeStamp, Current __current) {
      this._ice_delegate.userStatusMessageChanged(statusMessage, timeStamp, __current);
   }
}
