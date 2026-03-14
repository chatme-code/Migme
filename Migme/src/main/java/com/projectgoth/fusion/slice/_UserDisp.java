package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectImpl;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.OutputStream;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import java.util.Arrays;

public abstract class _UserDisp extends ObjectImpl implements User {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::User"};
   private static final String[] __all = new String[]{"acceptContactRequest", "accountBalanceChanged", "addContact", "addPendingContact", "addToContactAndBroadcastLists", "addToCurrentChatroomList", "anonymousCallSettingChanged", "blockUser", "contactChangedDisplayPictureOneWay", "contactChangedPresenceOneWay", "contactChangedStatusMessageOneWay", "contactDetailChanged", "contactGroupDeleted", "contactGroupDetailChanged", "contactRequestWasAccepted", "contactRequestWasRejected", "contactUpdated", "createSession", "disconnect", "disconnectFlooder", "emailNotification", "emoticonPackActivated", "enteringGroupChat", "executeEmoteCommandWithState", "findSession", "getBlockList", "getBlockListFromUsernames", "getBroadcastList", "getConnectedOtherIMs", "getContactList", "getContactListVersion", "getContacts", "getCurrentChatrooms", "getEmoticonAlternateKeys", "getEmoticonHotKeys", "getOnlineContactsCount", "getOtherIMConferenceParticipants", "getOtherIMContacts", "getOtherIMCredentials", "getOverallFusionPresence", "getReputationDataLevel", "getSessions", "getUnreadEmailCount", "getUserData", "ice_id", "ice_ids", "ice_isA", "ice_ping", "isOnBlockList", "isOnContactList", "leavingGroupChat", "messageSettingChanged", "newUserContactUpdated", "notifySessionsOfNewContact", "notifyUserJoinedGroupChat", "notifyUserLeftGroupChat", "oldUserContactUpdated", "otherIMAddContact", "otherIMInviteToConference", "otherIMLeaveConference", "otherIMLogin", "otherIMLogout", "otherIMRemoveContact", "otherIMRemoved", "otherIMSendMessage", "privateChatNowAGroupChat", "privateChattedWith", "pushNotification", "putAlertMessage", "putAnonymousCallNotification", "putEvent", "putFileReceived", "putMessage", "putMessageStatusEvent", "putServerQuestion", "putWebCallNotification", "rejectContactRequest", "removeContact", "removeFromCurrentChatroomList", "setCurrentChatListGroupChatSubset", "stopBroadcastingTo", "supportsBinaryMessage", "themeChanged", "unblockUser", "userCanContactMe", "userDetailChanged", "userDisplayPictureChanged", "userReputationChanged", "userStatusMessageChanged"};

   protected void ice_copyStateFrom(Object __obj) throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
   }

   public final ContactDataIce acceptContactRequest(ContactDataIce contact, UserPrx contactProxy, int inviterContactListVersion, int inviteeContactListVersion) {
      return this.acceptContactRequest(contact, contactProxy, inviterContactListVersion, inviteeContactListVersion, (Current)null);
   }

   public final void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency) {
      this.accountBalanceChanged(balance, fundedBalance, currency, (Current)null);
   }

   public final void addContact(ContactDataIce contact, int contactListVersion) {
      this.addContact(contact, contactListVersion, (Current)null);
   }

   public final void addPendingContact(String username) {
      this.addPendingContact(username, (Current)null);
   }

   public final void addToContactAndBroadcastLists(ContactDataIce contact, int contactListVersion) {
      this.addToContactAndBroadcastLists(contact, contactListVersion, (Current)null);
   }

   public final void addToCurrentChatroomList(String chatroom) throws FusionException {
      this.addToCurrentChatroomList(chatroom, (Current)null);
   }

   public final void anonymousCallSettingChanged(int setting) {
      this.anonymousCallSettingChanged(setting, (Current)null);
   }

   public final void blockUser(String username, int contactListVersion) {
      this.blockUser(username, contactListVersion, (Current)null);
   }

   public final void contactChangedDisplayPictureOneWay(String source, String displayPicture, long timeStamp) {
      this.contactChangedDisplayPictureOneWay(source, displayPicture, timeStamp, (Current)null);
   }

   public final void contactChangedPresenceOneWay(int imType, String source, int presence) {
      this.contactChangedPresenceOneWay(imType, source, presence, (Current)null);
   }

   public final void contactChangedStatusMessageOneWay(String source, String statusMessage, long timeStamp) {
      this.contactChangedStatusMessageOneWay(source, statusMessage, timeStamp, (Current)null);
   }

   public final void contactDetailChanged(ContactDataIce contact, int contactListVersion) {
      this.contactDetailChanged(contact, contactListVersion, (Current)null);
   }

   public final void contactGroupDeleted(int contactGroupID, int contactListVersion) {
      this.contactGroupDeleted(contactGroupID, contactListVersion, (Current)null);
   }

   public final void contactGroupDetailChanged(ContactGroupDataIce contactGroup, int contactListVersion) {
      this.contactGroupDetailChanged(contactGroup, contactListVersion, (Current)null);
   }

   public final ContactDataIce contactRequestWasAccepted(ContactDataIce contact, String statusMessage, String displayPicture, int overallFusionPresence, int contactListVersion) {
      return this.contactRequestWasAccepted(contact, statusMessage, displayPicture, overallFusionPresence, contactListVersion, (Current)null);
   }

   public final void contactRequestWasRejected(String contactRequestUsername, int contactListVersion) {
      this.contactRequestWasRejected(contactRequestUsername, contactListVersion, (Current)null);
   }

   public final PresenceAndCapabilityIce contactUpdated(ContactDataIce contact, String oldusername, boolean acceptedContactRequest, boolean changedFusionContact, UserPrx newContactUserProxy, int contactListVersion) throws FusionException {
      return this.contactUpdated(contact, oldusername, acceptedContactRequest, changedFusionContact, newContactUserProxy, contactListVersion, (Current)null);
   }

   public final void createSession_async(AMD_User_createSession __cb, String sessionID, int presence, int deviceType, int connectionType, int imType, int port, int remotePort, String IP, String mobileDevice, String userAgent, short clientVersion, String language, ConnectionPrx connectionProxy) throws FusionException {
      this.createSession_async(__cb, sessionID, presence, deviceType, connectionType, imType, port, remotePort, IP, mobileDevice, userAgent, clientVersion, language, connectionProxy, (Current)null);
   }

   public final void disconnect(String reason) {
      this.disconnect(reason, (Current)null);
   }

   public final void disconnectFlooder(String reason) {
      this.disconnectFlooder(reason, (Current)null);
   }

   public final void emailNotification(int unreadEmailCount) {
      this.emailNotification(unreadEmailCount, (Current)null);
   }

   public final void emoticonPackActivated(int emoticonPackId) {
      this.emoticonPackActivated(emoticonPackId, (Current)null);
   }

   public final void enteringGroupChat(boolean isCreator) throws FusionException {
      this.enteringGroupChat(isCreator, (Current)null);
   }

   public final int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy) throws FusionException {
      return this.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, (Current)null);
   }

   public final SessionPrx findSession(String sid) throws FusionException {
      return this.findSession(sid, (Current)null);
   }

   public final String[] getBlockList() {
      return this.getBlockList((Current)null);
   }

   public final String[] getBlockListFromUsernames(String[] usernames) {
      return this.getBlockListFromUsernames(usernames, (Current)null);
   }

   public final String[] getBroadcastList() {
      return this.getBroadcastList((Current)null);
   }

   public final int[] getConnectedOtherIMs() {
      return this.getConnectedOtherIMs((Current)null);
   }

   public final ContactList getContactList() {
      return this.getContactList((Current)null);
   }

   public final int getContactListVersion() {
      return this.getContactListVersion((Current)null);
   }

   public final ContactDataIce[] getContacts() {
      return this.getContacts((Current)null);
   }

   public final String[] getCurrentChatrooms() {
      return this.getCurrentChatrooms((Current)null);
   }

   public final String[] getEmoticonAlternateKeys() {
      return this.getEmoticonAlternateKeys((Current)null);
   }

   public final String[] getEmoticonHotKeys() {
      return this.getEmoticonHotKeys((Current)null);
   }

   public final int getOnlineContactsCount() {
      return this.getOnlineContactsCount((Current)null);
   }

   public final String[] getOtherIMConferenceParticipants(int imType, String otherIMConferenceID) {
      return this.getOtherIMConferenceParticipants(imType, otherIMConferenceID, (Current)null);
   }

   public final ContactDataIce[] getOtherIMContacts() {
      return this.getOtherIMContacts((Current)null);
   }

   public final Credential[] getOtherIMCredentials() {
      return this.getOtherIMCredentials((Current)null);
   }

   public final int getOverallFusionPresence(String requestingUsername) {
      return this.getOverallFusionPresence(requestingUsername, (Current)null);
   }

   public final int getReputationDataLevel() {
      return this.getReputationDataLevel((Current)null);
   }

   public final SessionPrx[] getSessions() {
      return this.getSessions((Current)null);
   }

   public final int getUnreadEmailCount() {
      return this.getUnreadEmailCount((Current)null);
   }

   public final UserDataIce getUserData() {
      return this.getUserData((Current)null);
   }

   public final boolean isOnBlockList(String contactUsername) {
      return this.isOnBlockList(contactUsername, (Current)null);
   }

   public final boolean isOnContactList(String contactUsername) {
      return this.isOnContactList(contactUsername, (Current)null);
   }

   public final void leavingGroupChat() {
      this.leavingGroupChat((Current)null);
   }

   public final void messageSettingChanged(int setting) {
      this.messageSettingChanged(setting, (Current)null);
   }

   public final void newUserContactUpdated(String usernameThatWasModified, boolean acceptedContactRequest) {
      this.newUserContactUpdated(usernameThatWasModified, acceptedContactRequest, (Current)null);
   }

   public final void notifySessionsOfNewContact(ContactDataIce newContact, int contactListVersion, boolean guaranteedIsNew) {
      this.notifySessionsOfNewContact(newContact, contactListVersion, guaranteedIsNew, (Current)null);
   }

   public final void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted) {
      this.notifyUserJoinedGroupChat(groupChatId, username, isMuted, (Current)null);
   }

   public final void notifyUserLeftGroupChat(String groupChatId, String username) {
      this.notifyUserLeftGroupChat(groupChatId, username, (Current)null);
   }

   public final void oldUserContactUpdated(String usernameThatWasModified) throws FusionException {
      this.oldUserContactUpdated(usernameThatWasModified, (Current)null);
   }

   public final void otherIMAddContact(int imType, String otherIMUsername) throws FusionException {
      this.otherIMAddContact(imType, otherIMUsername, (Current)null);
   }

   public final String otherIMInviteToConference(int imType, String otherIMConferenceID, String otherIMUsername) throws FusionException {
      return this.otherIMInviteToConference(imType, otherIMConferenceID, otherIMUsername, (Current)null);
   }

   public final void otherIMLeaveConference(int imType, String otherIMConferenceID) {
      this.otherIMLeaveConference(imType, otherIMConferenceID, (Current)null);
   }

   public final void otherIMLogin_async(AMD_User_otherIMLogin __cb, int imType, int presence, boolean showOfflineContacts) throws FusionException {
      this.otherIMLogin_async(__cb, imType, presence, showOfflineContacts, (Current)null);
   }

   public final void otherIMLogout(int imType) {
      this.otherIMLogout(imType, (Current)null);
   }

   public final void otherIMRemoveContact(int contactId) throws FusionException {
      this.otherIMRemoveContact(contactId, (Current)null);
   }

   public final void otherIMRemoved(int imType) {
      this.otherIMRemoved(imType, (Current)null);
   }

   public final void otherIMSendMessage(int imType, String otherIMUsername, String message) throws FusionException {
      this.otherIMSendMessage(imType, otherIMUsername, message, (Current)null);
   }

   public final void privateChatNowAGroupChat(String groupChatID, String creator) throws FusionException {
      this.privateChatNowAGroupChat(groupChatID, creator, (Current)null);
   }

   public final boolean privateChattedWith(String username) {
      return this.privateChattedWith(username, (Current)null);
   }

   public final void pushNotification(Message msg) throws FusionException {
      this.pushNotification(msg, (Current)null);
   }

   public final void putAlertMessage(String message, String title, short timeout) throws FusionException {
      this.putAlertMessage(message, title, timeout, (Current)null);
   }

   public final void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone) throws FusionException {
      this.putAnonymousCallNotification(requestingUsername, requestingMobilePhone, (Current)null);
   }

   public final void putEvent(UserEventIce event) throws FusionException {
      this.putEvent(event, (Current)null);
   }

   public final void putFileReceived(MessageDataIce message) throws FusionException {
      this.putFileReceived(message, (Current)null);
   }

   public final void putMessage_async(AMD_User_putMessage __cb, MessageDataIce message) throws FusionException {
      this.putMessage_async(__cb, message, (Current)null);
   }

   public final void putMessageStatusEvent(MessageStatusEventIce mseIce) throws FusionException {
      this.putMessageStatusEvent(mseIce, (Current)null);
   }

   public final void putServerQuestion(String message, String url) throws FusionException {
      this.putServerQuestion(message, url, (Current)null);
   }

   public final void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol) throws FusionException {
      this.putWebCallNotification(source, destination, gateway, gatewayName, protocol, (Current)null);
   }

   public final void rejectContactRequest(String inviterUsername) {
      this.rejectContactRequest(inviterUsername, (Current)null);
   }

   public final void removeContact(int contactid, int contactListVersion) {
      this.removeContact(contactid, contactListVersion, (Current)null);
   }

   public final void removeFromCurrentChatroomList(String chatroom) {
      this.removeFromCurrentChatroomList(chatroom, (Current)null);
   }

   public final void setCurrentChatListGroupChatSubset(ChatListIce ccl) {
      this.setCurrentChatListGroupChatSubset(ccl, (Current)null);
   }

   public final void stopBroadcastingTo(String username) {
      this.stopBroadcastingTo(username, (Current)null);
   }

   public final boolean supportsBinaryMessage() {
      return this.supportsBinaryMessage((Current)null);
   }

   public final void themeChanged(String themeLocation) throws FusionException {
      this.themeChanged(themeLocation, (Current)null);
   }

   public final void unblockUser(String username) {
      this.unblockUser(username, (Current)null);
   }

   public final UserErrorResponse userCanContactMe(String username, MessageDataIce message) {
      return this.userCanContactMe(username, message, (Current)null);
   }

   public final void userDetailChanged(UserDataIce user) {
      this.userDetailChanged(user, (Current)null);
   }

   public final void userDisplayPictureChanged(String displayPicture, long timeStamp) {
      this.userDisplayPictureChanged(displayPicture, timeStamp, (Current)null);
   }

   public final void userReputationChanged() {
      this.userReputationChanged((Current)null);
   }

   public final void userStatusMessageChanged(String statusMessage, long timeStamp) {
      this.userStatusMessageChanged(statusMessage, timeStamp, (Current)null);
   }

   public static DispatchStatus ___createSession(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String sessionID = __is.readString();
      int presence = __is.readInt();
      int deviceType = __is.readInt();
      int connectionType = __is.readInt();
      int imType = __is.readInt();
      int port = __is.readInt();
      int remotePort = __is.readInt();
      String IP = __is.readString();
      String mobileDevice = __is.readString();
      String userAgent = __is.readString();
      short clientVersion = __is.readShort();
      String language = __is.readString();
      ConnectionPrx connectionProxy = ConnectionPrxHelper.__read(__is);
      __is.endReadEncaps();
      _AMD_User_createSession __cb = new _AMD_User_createSession(__inS);

      try {
         __obj.createSession_async(__cb, sessionID, presence, deviceType, connectionType, imType, port, remotePort, IP, mobileDevice, userAgent, clientVersion, language, connectionProxy, __current);
      } catch (Exception var19) {
         __cb.ice_exception(var19);
      }

      return DispatchStatus.DispatchAsync;
   }

   public static DispatchStatus ___putMessage(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      _AMD_User_putMessage __cb = new _AMD_User_putMessage(__inS);

      try {
         __obj.putMessage_async(__cb, message, __current);
      } catch (Exception var7) {
         __cb.ice_exception(var7);
      }

      return DispatchStatus.DispatchAsync;
   }

   public static DispatchStatus ___contactChangedPresenceOneWay(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      String source = __is.readString();
      int presence = __is.readInt();
      __is.endReadEncaps();
      __obj.contactChangedPresenceOneWay(imType, source, presence, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactChangedDisplayPictureOneWay(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String source = __is.readString();
      String displayPicture = __is.readString();
      long timeStamp = __is.readLong();
      __is.endReadEncaps();
      __obj.contactChangedDisplayPictureOneWay(source, displayPicture, timeStamp, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactChangedStatusMessageOneWay(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String source = __is.readString();
      String statusMessage = __is.readString();
      long timeStamp = __is.readLong();
      __is.endReadEncaps();
      __obj.contactChangedStatusMessageOneWay(source, statusMessage, timeStamp, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getOverallFusionPresence(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String requestingUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getOverallFusionPresence(requestingUsername, __current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getContactListVersion(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getContactListVersion(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getContactList(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      ContactList __ret = __obj.getContactList(__current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getContacts(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      ContactDataIce[] __ret = __obj.getContacts(__current);
      ContactDataIceArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getOtherIMContacts(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      ContactDataIce[] __ret = __obj.getOtherIMContacts(__current);
      ContactDataIceArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getOtherIMCredentials(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      Credential[] __ret = __obj.getOtherIMCredentials(__current);
      CredentialArrayHelper.write(__os, __ret);
      __os.writePendingObjects();
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getOtherIMConferenceParticipants(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      String otherIMConferenceID = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getOtherIMConferenceParticipants(imType, otherIMConferenceID, __current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getUserData(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      UserDataIce __ret = __obj.getUserData(__current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___isOnContactList(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String contactUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      boolean __ret = __obj.isOnContactList(contactUsername, __current);
      __os.writeBool(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___isOnBlockList(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String contactUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      boolean __ret = __obj.isOnBlockList(contactUsername, __current);
      __os.writeBool(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___otherIMLogin(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      int presence = __is.readInt();
      boolean showOfflineContacts = __is.readBool();
      __is.endReadEncaps();
      _AMD_User_otherIMLogin __cb = new _AMD_User_otherIMLogin(__inS);

      try {
         __obj.otherIMLogin_async(__cb, imType, presence, showOfflineContacts, __current);
      } catch (Exception var9) {
         __cb.ice_exception(var9);
      }

      return DispatchStatus.DispatchAsync;
   }

   public static DispatchStatus ___otherIMLogout(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      __is.endReadEncaps();
      __obj.otherIMLogout(imType, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___otherIMSendMessage(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      String otherIMUsername = __is.readString();
      String message = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.otherIMSendMessage(imType, otherIMUsername, message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___otherIMInviteToConference(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      String otherIMConferenceID = __is.readString();
      String otherIMUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         String __ret = __obj.otherIMInviteToConference(imType, otherIMConferenceID, otherIMUsername, __current);
         __os.writeString(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___otherIMLeaveConference(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      String otherIMConferenceID = __is.readString();
      __is.endReadEncaps();
      __obj.otherIMLeaveConference(imType, otherIMConferenceID, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___otherIMAddContact(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      String otherIMUsername = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.otherIMAddContact(imType, otherIMUsername, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___otherIMRemoveContact(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int contactId = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.otherIMRemoveContact(contactId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___otherIMRemoved(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      __is.endReadEncaps();
      __obj.otherIMRemoved(imType, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getBlockList(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getBlockList(__current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getBlockListFromUsernames(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String[] usernames = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getBlockListFromUsernames(usernames, __current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getBroadcastList(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getBroadcastList(__current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___accountBalanceChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      double balance = __is.readDouble();
      double fundedBalance = __is.readDouble();
      CurrencyDataIce currency = new CurrencyDataIce();
      currency.__read(__is);
      __is.endReadEncaps();
      __obj.accountBalanceChanged(balance, fundedBalance, currency, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___privateChatNowAGroupChat(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String groupChatID = __is.readString();
      String creator = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.privateChatNowAGroupChat(groupChatID, creator, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putEvent(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      UserEventIceHolder event = new UserEventIceHolder();
      __is.readObject(event.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putEvent(event.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putAlertMessage(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String message = __is.readString();
      String title = __is.readString();
      short timeout = __is.readShort();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putAlertMessage(message, title, timeout, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putServerQuestion(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String message = __is.readString();
      String url = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putServerQuestion(message, url, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putWebCallNotification(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String source = __is.readString();
      String destination = __is.readString();
      int gateway = __is.readInt();
      String gatewayName = __is.readString();
      int protocol = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putWebCallNotification(source, destination, gateway, gatewayName, protocol, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putAnonymousCallNotification(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String requestingUsername = __is.readString();
      String requestingMobilePhone = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putAnonymousCallNotification(requestingUsername, requestingMobilePhone, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putFileReceived(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putFileReceived(message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___contactDetailChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce contact = new ContactDataIce();
      contact.__read(__is);
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      __obj.contactDetailChanged(contact, contactListVersion, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactGroupDetailChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactGroupDataIce contactGroup = new ContactGroupDataIce();
      contactGroup.__read(__is);
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      __obj.contactGroupDetailChanged(contactGroup, contactListVersion, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactGroupDeleted(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int contactGroupID = __is.readInt();
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      __obj.contactGroupDeleted(contactGroupID, contactListVersion, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___userDetailChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      UserDataIce user = new UserDataIce();
      user.__read(__is);
      __is.endReadEncaps();
      __obj.userDetailChanged(user, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___userReputationChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      __obj.userReputationChanged(__current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___userDisplayPictureChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String displayPicture = __is.readString();
      long timeStamp = __is.readLong();
      __is.endReadEncaps();
      __obj.userDisplayPictureChanged(displayPicture, timeStamp, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___userStatusMessageChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String statusMessage = __is.readString();
      long timeStamp = __is.readLong();
      __is.endReadEncaps();
      __obj.userStatusMessageChanged(statusMessage, timeStamp, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___messageSettingChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int setting = __is.readInt();
      __is.endReadEncaps();
      __obj.messageSettingChanged(setting, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___anonymousCallSettingChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int setting = __is.readInt();
      __is.endReadEncaps();
      __obj.anonymousCallSettingChanged(setting, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getConnectedOtherIMs(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int[] __ret = __obj.getConnectedOtherIMs(__current);
      IntArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___supportsBinaryMessage(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      boolean __ret = __obj.supportsBinaryMessage(__current);
      __os.writeBool(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getUnreadEmailCount(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getUnreadEmailCount(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___emailNotification(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int unreadEmailCount = __is.readInt();
      __is.endReadEncaps();
      __obj.emailNotification(unreadEmailCount, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getEmoticonHotKeys(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getEmoticonHotKeys(__current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getEmoticonAlternateKeys(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getEmoticonAlternateKeys(__current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___emoticonPackActivated(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int emoticonPackId = __is.readInt();
      __is.endReadEncaps();
      __obj.emoticonPackActivated(emoticonPackId, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___themeChanged(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String themeLocation = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.themeChanged(themeLocation, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___addContact(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce contact = new ContactDataIce();
      contact.__read(__is);
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      __obj.addContact(contact, contactListVersion, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___addToContactAndBroadcastLists(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce contact = new ContactDataIce();
      contact.__read(__is);
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      __obj.addToContactAndBroadcastLists(contact, contactListVersion, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___addPendingContact(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      __obj.addPendingContact(username, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___acceptContactRequest(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce contact = new ContactDataIce();
      contact.__read(__is);
      UserPrx contactProxy = UserPrxHelper.__read(__is);
      int inviterContactListVersion = __is.readInt();
      int inviteeContactListVersion = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      ContactDataIce __ret = __obj.acceptContactRequest(contact, contactProxy, inviterContactListVersion, inviteeContactListVersion, __current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactRequestWasAccepted(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce contact = new ContactDataIce();
      contact.__read(__is);
      String statusMessage = __is.readString();
      String displayPicture = __is.readString();
      int overallFusionPresence = __is.readInt();
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      ContactDataIce __ret = __obj.contactRequestWasAccepted(contact, statusMessage, displayPicture, overallFusionPresence, contactListVersion, __current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___blockUser(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      __obj.blockUser(username, contactListVersion, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___unblockUser(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      __obj.unblockUser(username, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactRequestWasRejected(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String contactRequestUsername = __is.readString();
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      __obj.contactRequestWasRejected(contactRequestUsername, contactListVersion, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___rejectContactRequest(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String inviterUsername = __is.readString();
      __is.endReadEncaps();
      __obj.rejectContactRequest(inviterUsername, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___stopBroadcastingTo(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      __obj.stopBroadcastingTo(username, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___removeContact(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int contactid = __is.readInt();
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      __obj.removeContact(contactid, contactListVersion, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactUpdated(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce contact = new ContactDataIce();
      contact.__read(__is);
      String oldusername = __is.readString();
      boolean acceptedContactRequest = __is.readBool();
      boolean changedFusionContact = __is.readBool();
      UserPrx newContactUserProxy = UserPrxHelper.__read(__is);
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         PresenceAndCapabilityIce __ret = __obj.contactUpdated(contact, oldusername, acceptedContactRequest, changedFusionContact, newContactUserProxy, contactListVersion, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var12) {
         __os.writeUserException(var12);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___oldUserContactUpdated(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String usernameThatWasModified = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.oldUserContactUpdated(usernameThatWasModified, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___newUserContactUpdated(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String usernameThatWasModified = __is.readString();
      boolean acceptedContactRequest = __is.readBool();
      __is.endReadEncaps();
      __obj.newUserContactUpdated(usernameThatWasModified, acceptedContactRequest, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___notifySessionsOfNewContact(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce newContact = new ContactDataIce();
      newContact.__read(__is);
      int contactListVersion = __is.readInt();
      boolean guaranteedIsNew = __is.readBool();
      __is.endReadEncaps();
      __obj.notifySessionsOfNewContact(newContact, contactListVersion, guaranteedIsNew, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getSessions(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      SessionPrx[] __ret = __obj.getSessions(__current);
      SessionProxyArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___disconnect(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String reason = __is.readString();
      __is.endReadEncaps();
      __obj.disconnect(reason, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___disconnectFlooder(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String reason = __is.readString();
      __is.endReadEncaps();
      __obj.disconnectFlooder(reason, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___privateChattedWith(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      boolean __ret = __obj.privateChattedWith(username, __current);
      __os.writeBool(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___userCanContactMe(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      UserErrorResponse __ret = __obj.userCanContactMe(username, message, __current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___enteringGroupChat(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      boolean isCreator = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.enteringGroupChat(isCreator, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___leavingGroupChat(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      __obj.leavingGroupChat(__current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___pushNotification(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageHolder msg = new MessageHolder();
      __is.readObject(msg.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.pushNotification(msg.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getOnlineContactsCount(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getOnlineContactsCount(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___executeEmoteCommandWithState(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String emoteCommand = __is.readString();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      SessionPrx sessionProxy = SessionPrxHelper.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         int __ret = __obj.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, __current);
         __os.writeInt(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___addToCurrentChatroomList(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String chatroom = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.addToCurrentChatroomList(chatroom, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___removeFromCurrentChatroomList(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String chatroom = __is.readString();
      __is.endReadEncaps();
      __obj.removeFromCurrentChatroomList(chatroom, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getCurrentChatrooms(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String[] __ret = __obj.getCurrentChatrooms(__current);
      StringArrayHelper.write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getReputationDataLevel(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getReputationDataLevel(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___notifyUserLeftGroupChat(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String groupChatId = __is.readString();
      String username = __is.readString();
      __is.endReadEncaps();
      __obj.notifyUserLeftGroupChat(groupChatId, username, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___notifyUserJoinedGroupChat(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String groupChatId = __is.readString();
      String username = __is.readString();
      boolean isMuted = __is.readBool();
      __is.endReadEncaps();
      __obj.notifyUserJoinedGroupChat(groupChatId, username, isMuted, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___setCurrentChatListGroupChatSubset(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ChatListIce ccl = new ChatListIce();
      ccl.__read(__is);
      __is.endReadEncaps();
      __obj.setCurrentChatListGroupChatSubset(ccl, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___putMessageStatusEvent(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageStatusEventIce mseIce = new MessageStatusEventIce();
      mseIce.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putMessageStatusEvent(mseIce, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___findSession(User __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String sid = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         SessionPrx __ret = __obj.findSession(sid, __current);
         SessionPrxHelper.__write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___acceptContactRequest(this, in, __current);
         case 1:
            return ___accountBalanceChanged(this, in, __current);
         case 2:
            return ___addContact(this, in, __current);
         case 3:
            return ___addPendingContact(this, in, __current);
         case 4:
            return ___addToContactAndBroadcastLists(this, in, __current);
         case 5:
            return ___addToCurrentChatroomList(this, in, __current);
         case 6:
            return ___anonymousCallSettingChanged(this, in, __current);
         case 7:
            return ___blockUser(this, in, __current);
         case 8:
            return ___contactChangedDisplayPictureOneWay(this, in, __current);
         case 9:
            return ___contactChangedPresenceOneWay(this, in, __current);
         case 10:
            return ___contactChangedStatusMessageOneWay(this, in, __current);
         case 11:
            return ___contactDetailChanged(this, in, __current);
         case 12:
            return ___contactGroupDeleted(this, in, __current);
         case 13:
            return ___contactGroupDetailChanged(this, in, __current);
         case 14:
            return ___contactRequestWasAccepted(this, in, __current);
         case 15:
            return ___contactRequestWasRejected(this, in, __current);
         case 16:
            return ___contactUpdated(this, in, __current);
         case 17:
            return ___createSession(this, in, __current);
         case 18:
            return ___disconnect(this, in, __current);
         case 19:
            return ___disconnectFlooder(this, in, __current);
         case 20:
            return ___emailNotification(this, in, __current);
         case 21:
            return ___emoticonPackActivated(this, in, __current);
         case 22:
            return ___enteringGroupChat(this, in, __current);
         case 23:
            return ___executeEmoteCommandWithState(this, in, __current);
         case 24:
            return ___findSession(this, in, __current);
         case 25:
            return ___getBlockList(this, in, __current);
         case 26:
            return ___getBlockListFromUsernames(this, in, __current);
         case 27:
            return ___getBroadcastList(this, in, __current);
         case 28:
            return ___getConnectedOtherIMs(this, in, __current);
         case 29:
            return ___getContactList(this, in, __current);
         case 30:
            return ___getContactListVersion(this, in, __current);
         case 31:
            return ___getContacts(this, in, __current);
         case 32:
            return ___getCurrentChatrooms(this, in, __current);
         case 33:
            return ___getEmoticonAlternateKeys(this, in, __current);
         case 34:
            return ___getEmoticonHotKeys(this, in, __current);
         case 35:
            return ___getOnlineContactsCount(this, in, __current);
         case 36:
            return ___getOtherIMConferenceParticipants(this, in, __current);
         case 37:
            return ___getOtherIMContacts(this, in, __current);
         case 38:
            return ___getOtherIMCredentials(this, in, __current);
         case 39:
            return ___getOverallFusionPresence(this, in, __current);
         case 40:
            return ___getReputationDataLevel(this, in, __current);
         case 41:
            return ___getSessions(this, in, __current);
         case 42:
            return ___getUnreadEmailCount(this, in, __current);
         case 43:
            return ___getUserData(this, in, __current);
         case 44:
            return ___ice_id(this, in, __current);
         case 45:
            return ___ice_ids(this, in, __current);
         case 46:
            return ___ice_isA(this, in, __current);
         case 47:
            return ___ice_ping(this, in, __current);
         case 48:
            return ___isOnBlockList(this, in, __current);
         case 49:
            return ___isOnContactList(this, in, __current);
         case 50:
            return ___leavingGroupChat(this, in, __current);
         case 51:
            return ___messageSettingChanged(this, in, __current);
         case 52:
            return ___newUserContactUpdated(this, in, __current);
         case 53:
            return ___notifySessionsOfNewContact(this, in, __current);
         case 54:
            return ___notifyUserJoinedGroupChat(this, in, __current);
         case 55:
            return ___notifyUserLeftGroupChat(this, in, __current);
         case 56:
            return ___oldUserContactUpdated(this, in, __current);
         case 57:
            return ___otherIMAddContact(this, in, __current);
         case 58:
            return ___otherIMInviteToConference(this, in, __current);
         case 59:
            return ___otherIMLeaveConference(this, in, __current);
         case 60:
            return ___otherIMLogin(this, in, __current);
         case 61:
            return ___otherIMLogout(this, in, __current);
         case 62:
            return ___otherIMRemoveContact(this, in, __current);
         case 63:
            return ___otherIMRemoved(this, in, __current);
         case 64:
            return ___otherIMSendMessage(this, in, __current);
         case 65:
            return ___privateChatNowAGroupChat(this, in, __current);
         case 66:
            return ___privateChattedWith(this, in, __current);
         case 67:
            return ___pushNotification(this, in, __current);
         case 68:
            return ___putAlertMessage(this, in, __current);
         case 69:
            return ___putAnonymousCallNotification(this, in, __current);
         case 70:
            return ___putEvent(this, in, __current);
         case 71:
            return ___putFileReceived(this, in, __current);
         case 72:
            return ___putMessage(this, in, __current);
         case 73:
            return ___putMessageStatusEvent(this, in, __current);
         case 74:
            return ___putServerQuestion(this, in, __current);
         case 75:
            return ___putWebCallNotification(this, in, __current);
         case 76:
            return ___rejectContactRequest(this, in, __current);
         case 77:
            return ___removeContact(this, in, __current);
         case 78:
            return ___removeFromCurrentChatroomList(this, in, __current);
         case 79:
            return ___setCurrentChatListGroupChatSubset(this, in, __current);
         case 80:
            return ___stopBroadcastingTo(this, in, __current);
         case 81:
            return ___supportsBinaryMessage(this, in, __current);
         case 82:
            return ___themeChanged(this, in, __current);
         case 83:
            return ___unblockUser(this, in, __current);
         case 84:
            return ___userCanContactMe(this, in, __current);
         case 85:
            return ___userDetailChanged(this, in, __current);
         case 86:
            return ___userDisplayPictureChanged(this, in, __current);
         case 87:
            return ___userReputationChanged(this, in, __current);
         case 88:
            return ___userStatusMessageChanged(this, in, __current);
         default:
            assert false;

            throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
         }
      }
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::User was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::User was not generated with stream support";
      throw ex;
   }
}
