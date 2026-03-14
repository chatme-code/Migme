package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface UserPrx extends ObjectPrx {
   SessionPrx createSession(String var1, int var2, int var3, int var4, int var5, int var6, int var7, String var8, String var9, String var10, short var11, String var12, ConnectionPrx var13) throws FusionException;

   SessionPrx createSession(String var1, int var2, int var3, int var4, int var5, int var6, int var7, String var8, String var9, String var10, short var11, String var12, ConnectionPrx var13, Map<String, String> var14) throws FusionException;

   void putMessage(MessageDataIce var1) throws FusionException;

   void putMessage(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void contactChangedPresenceOneWay(int var1, String var2, int var3);

   void contactChangedPresenceOneWay(int var1, String var2, int var3, Map<String, String> var4);

   void contactChangedDisplayPictureOneWay(String var1, String var2, long var3);

   void contactChangedDisplayPictureOneWay(String var1, String var2, long var3, Map<String, String> var5);

   void contactChangedStatusMessageOneWay(String var1, String var2, long var3);

   void contactChangedStatusMessageOneWay(String var1, String var2, long var3, Map<String, String> var5);

   int getOverallFusionPresence(String var1);

   int getOverallFusionPresence(String var1, Map<String, String> var2);

   int getContactListVersion();

   int getContactListVersion(Map<String, String> var1);

   ContactList getContactList();

   ContactList getContactList(Map<String, String> var1);

   ContactDataIce[] getContacts();

   ContactDataIce[] getContacts(Map<String, String> var1);

   ContactDataIce[] getOtherIMContacts();

   ContactDataIce[] getOtherIMContacts(Map<String, String> var1);

   Credential[] getOtherIMCredentials();

   Credential[] getOtherIMCredentials(Map<String, String> var1);

   String[] getOtherIMConferenceParticipants(int var1, String var2);

   String[] getOtherIMConferenceParticipants(int var1, String var2, Map<String, String> var3);

   UserDataIce getUserData();

   UserDataIce getUserData(Map<String, String> var1);

   boolean isOnContactList(String var1);

   boolean isOnContactList(String var1, Map<String, String> var2);

   boolean isOnBlockList(String var1);

   boolean isOnBlockList(String var1, Map<String, String> var2);

   void otherIMLogin(int var1, int var2, boolean var3) throws FusionException;

   void otherIMLogin(int var1, int var2, boolean var3, Map<String, String> var4) throws FusionException;

   void otherIMLogout(int var1);

   void otherIMLogout(int var1, Map<String, String> var2);

   void otherIMSendMessage(int var1, String var2, String var3) throws FusionException;

   void otherIMSendMessage(int var1, String var2, String var3, Map<String, String> var4) throws FusionException;

   String otherIMInviteToConference(int var1, String var2, String var3) throws FusionException;

   String otherIMInviteToConference(int var1, String var2, String var3, Map<String, String> var4) throws FusionException;

   void otherIMLeaveConference(int var1, String var2);

   void otherIMLeaveConference(int var1, String var2, Map<String, String> var3);

   void otherIMAddContact(int var1, String var2) throws FusionException;

   void otherIMAddContact(int var1, String var2, Map<String, String> var3) throws FusionException;

   void otherIMRemoveContact(int var1) throws FusionException;

   void otherIMRemoveContact(int var1, Map<String, String> var2) throws FusionException;

   void otherIMRemoved(int var1);

   void otherIMRemoved(int var1, Map<String, String> var2);

   String[] getBlockList();

   String[] getBlockList(Map<String, String> var1);

   String[] getBlockListFromUsernames(String[] var1);

   String[] getBlockListFromUsernames(String[] var1, Map<String, String> var2);

   String[] getBroadcastList();

   String[] getBroadcastList(Map<String, String> var1);

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5);

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5, Map<String, String> var6);

   void privateChatNowAGroupChat(String var1, String var2) throws FusionException;

   void privateChatNowAGroupChat(String var1, String var2, Map<String, String> var3) throws FusionException;

   void putEvent(UserEventIce var1) throws FusionException;

   void putEvent(UserEventIce var1, Map<String, String> var2) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3, Map<String, String> var4) throws FusionException;

   void putServerQuestion(String var1, String var2) throws FusionException;

   void putServerQuestion(String var1, String var2, Map<String, String> var3) throws FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5) throws FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5, Map<String, String> var6) throws FusionException;

   void putAnonymousCallNotification(String var1, String var2) throws FusionException;

   void putAnonymousCallNotification(String var1, String var2, Map<String, String> var3) throws FusionException;

   void putFileReceived(MessageDataIce var1) throws FusionException;

   void putFileReceived(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void contactDetailChanged(ContactDataIce var1, int var2);

   void contactDetailChanged(ContactDataIce var1, int var2, Map<String, String> var3);

   void contactGroupDetailChanged(ContactGroupDataIce var1, int var2);

   void contactGroupDetailChanged(ContactGroupDataIce var1, int var2, Map<String, String> var3);

   void contactGroupDeleted(int var1, int var2);

   void contactGroupDeleted(int var1, int var2, Map<String, String> var3);

   void userDetailChanged(UserDataIce var1);

   void userDetailChanged(UserDataIce var1, Map<String, String> var2);

   void userReputationChanged();

   void userReputationChanged(Map<String, String> var1);

   void userDisplayPictureChanged(String var1, long var2);

   void userDisplayPictureChanged(String var1, long var2, Map<String, String> var4);

   void userStatusMessageChanged(String var1, long var2);

   void userStatusMessageChanged(String var1, long var2, Map<String, String> var4);

   void messageSettingChanged(int var1);

   void messageSettingChanged(int var1, Map<String, String> var2);

   void anonymousCallSettingChanged(int var1);

   void anonymousCallSettingChanged(int var1, Map<String, String> var2);

   int[] getConnectedOtherIMs();

   int[] getConnectedOtherIMs(Map<String, String> var1);

   boolean supportsBinaryMessage();

   boolean supportsBinaryMessage(Map<String, String> var1);

   int getUnreadEmailCount();

   int getUnreadEmailCount(Map<String, String> var1);

   void emailNotification(int var1);

   void emailNotification(int var1, Map<String, String> var2);

   String[] getEmoticonHotKeys();

   String[] getEmoticonHotKeys(Map<String, String> var1);

   String[] getEmoticonAlternateKeys();

   String[] getEmoticonAlternateKeys(Map<String, String> var1);

   void emoticonPackActivated(int var1);

   void emoticonPackActivated(int var1, Map<String, String> var2);

   void themeChanged(String var1) throws FusionException;

   void themeChanged(String var1, Map<String, String> var2) throws FusionException;

   void addContact(ContactDataIce var1, int var2);

   void addContact(ContactDataIce var1, int var2, Map<String, String> var3);

   void addToContactAndBroadcastLists(ContactDataIce var1, int var2);

   void addToContactAndBroadcastLists(ContactDataIce var1, int var2, Map<String, String> var3);

   void addPendingContact(String var1);

   void addPendingContact(String var1, Map<String, String> var2);

   ContactDataIce acceptContactRequest(ContactDataIce var1, UserPrx var2, int var3, int var4);

   ContactDataIce acceptContactRequest(ContactDataIce var1, UserPrx var2, int var3, int var4, Map<String, String> var5);

   ContactDataIce contactRequestWasAccepted(ContactDataIce var1, String var2, String var3, int var4, int var5);

   ContactDataIce contactRequestWasAccepted(ContactDataIce var1, String var2, String var3, int var4, int var5, Map<String, String> var6);

   void blockUser(String var1, int var2);

   void blockUser(String var1, int var2, Map<String, String> var3);

   void unblockUser(String var1);

   void unblockUser(String var1, Map<String, String> var2);

   void contactRequestWasRejected(String var1, int var2);

   void contactRequestWasRejected(String var1, int var2, Map<String, String> var3);

   void rejectContactRequest(String var1);

   void rejectContactRequest(String var1, Map<String, String> var2);

   void stopBroadcastingTo(String var1);

   void stopBroadcastingTo(String var1, Map<String, String> var2);

   void removeContact(int var1, int var2);

   void removeContact(int var1, int var2, Map<String, String> var3);

   PresenceAndCapabilityIce contactUpdated(ContactDataIce var1, String var2, boolean var3, boolean var4, UserPrx var5, int var6) throws FusionException;

   PresenceAndCapabilityIce contactUpdated(ContactDataIce var1, String var2, boolean var3, boolean var4, UserPrx var5, int var6, Map<String, String> var7) throws FusionException;

   void oldUserContactUpdated(String var1) throws FusionException;

   void oldUserContactUpdated(String var1, Map<String, String> var2) throws FusionException;

   void newUserContactUpdated(String var1, boolean var2);

   void newUserContactUpdated(String var1, boolean var2, Map<String, String> var3);

   void notifySessionsOfNewContact(ContactDataIce var1, int var2, boolean var3);

   void notifySessionsOfNewContact(ContactDataIce var1, int var2, boolean var3, Map<String, String> var4);

   SessionPrx[] getSessions();

   SessionPrx[] getSessions(Map<String, String> var1);

   void disconnect(String var1);

   void disconnect(String var1, Map<String, String> var2);

   void disconnectFlooder(String var1);

   void disconnectFlooder(String var1, Map<String, String> var2);

   boolean privateChattedWith(String var1);

   boolean privateChattedWith(String var1, Map<String, String> var2);

   UserErrorResponse userCanContactMe(String var1, MessageDataIce var2);

   UserErrorResponse userCanContactMe(String var1, MessageDataIce var2, Map<String, String> var3);

   void enteringGroupChat(boolean var1) throws FusionException;

   void enteringGroupChat(boolean var1, Map<String, String> var2) throws FusionException;

   void leavingGroupChat();

   void leavingGroupChat(Map<String, String> var1);

   void pushNotification(Message var1) throws FusionException;

   void pushNotification(Message var1, Map<String, String> var2) throws FusionException;

   int getOnlineContactsCount();

   int getOnlineContactsCount(Map<String, String> var1);

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3) throws FusionException;

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3, Map<String, String> var4) throws FusionException;

   void addToCurrentChatroomList(String var1) throws FusionException;

   void addToCurrentChatroomList(String var1, Map<String, String> var2) throws FusionException;

   void removeFromCurrentChatroomList(String var1);

   void removeFromCurrentChatroomList(String var1, Map<String, String> var2);

   String[] getCurrentChatrooms();

   String[] getCurrentChatrooms(Map<String, String> var1);

   int getReputationDataLevel();

   int getReputationDataLevel(Map<String, String> var1);

   void notifyUserLeftGroupChat(String var1, String var2);

   void notifyUserLeftGroupChat(String var1, String var2, Map<String, String> var3);

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3);

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3, Map<String, String> var4);

   void setCurrentChatListGroupChatSubset(ChatListIce var1);

   void setCurrentChatListGroupChatSubset(ChatListIce var1, Map<String, String> var2);

   void putMessageStatusEvent(MessageStatusEventIce var1) throws FusionException;

   void putMessageStatusEvent(MessageStatusEventIce var1, Map<String, String> var2) throws FusionException;

   SessionPrx findSession(String var1) throws FusionException;

   SessionPrx findSession(String var1, Map<String, String> var2) throws FusionException;
}
