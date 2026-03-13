package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _UserDel extends _ObjectDel {
   SessionPrx createSession(String var1, int var2, int var3, int var4, int var5, int var6, int var7, String var8, String var9, String var10, short var11, String var12, ConnectionPrx var13, Map<String, String> var14) throws LocalExceptionWrapper, FusionException;

   void putMessage(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void contactChangedPresenceOneWay(int var1, String var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper;

   void contactChangedDisplayPictureOneWay(String var1, String var2, long var3, Map<String, String> var5) throws LocalExceptionWrapper;

   void contactChangedStatusMessageOneWay(String var1, String var2, long var3, Map<String, String> var5) throws LocalExceptionWrapper;

   int getOverallFusionPresence(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   int getContactListVersion(Map<String, String> var1) throws LocalExceptionWrapper;

   ContactList getContactList(Map<String, String> var1) throws LocalExceptionWrapper;

   ContactDataIce[] getContacts(Map<String, String> var1) throws LocalExceptionWrapper;

   ContactDataIce[] getOtherIMContacts(Map<String, String> var1) throws LocalExceptionWrapper;

   Credential[] getOtherIMCredentials(Map<String, String> var1) throws LocalExceptionWrapper;

   String[] getOtherIMConferenceParticipants(int var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper;

   UserDataIce getUserData(Map<String, String> var1) throws LocalExceptionWrapper;

   boolean isOnContactList(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   boolean isOnBlockList(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void otherIMLogin(int var1, int var2, boolean var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void otherIMLogout(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void otherIMSendMessage(int var1, String var2, String var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   String otherIMInviteToConference(int var1, String var2, String var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void otherIMLeaveConference(int var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void otherIMAddContact(int var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void otherIMRemoveContact(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void otherIMRemoved(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   String[] getBlockList(Map<String, String> var1) throws LocalExceptionWrapper;

   String[] getBlockListFromUsernames(String[] var1, Map<String, String> var2) throws LocalExceptionWrapper;

   String[] getBroadcastList(Map<String, String> var1) throws LocalExceptionWrapper;

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5, Map<String, String> var6) throws LocalExceptionWrapper;

   void privateChatNowAGroupChat(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void putEvent(UserEventIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putAlertMessage(String var1, String var2, short var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void putServerQuestion(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void putAnonymousCallNotification(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void putFileReceived(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void contactDetailChanged(ContactDataIce var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void contactGroupDetailChanged(ContactGroupDataIce var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void contactGroupDeleted(int var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void userDetailChanged(UserDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void userReputationChanged(Map<String, String> var1) throws LocalExceptionWrapper;

   void userDisplayPictureChanged(String var1, long var2, Map<String, String> var4) throws LocalExceptionWrapper;

   void userStatusMessageChanged(String var1, long var2, Map<String, String> var4) throws LocalExceptionWrapper;

   void messageSettingChanged(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void anonymousCallSettingChanged(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   int[] getConnectedOtherIMs(Map<String, String> var1) throws LocalExceptionWrapper;

   boolean supportsBinaryMessage(Map<String, String> var1) throws LocalExceptionWrapper;

   int getUnreadEmailCount(Map<String, String> var1) throws LocalExceptionWrapper;

   void emailNotification(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   String[] getEmoticonHotKeys(Map<String, String> var1) throws LocalExceptionWrapper;

   String[] getEmoticonAlternateKeys(Map<String, String> var1) throws LocalExceptionWrapper;

   void emoticonPackActivated(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void themeChanged(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void addContact(ContactDataIce var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void addToContactAndBroadcastLists(ContactDataIce var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void addPendingContact(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   ContactDataIce acceptContactRequest(ContactDataIce var1, UserPrx var2, int var3, int var4, Map<String, String> var5) throws LocalExceptionWrapper;

   ContactDataIce contactRequestWasAccepted(ContactDataIce var1, String var2, String var3, int var4, int var5, Map<String, String> var6) throws LocalExceptionWrapper;

   void blockUser(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void unblockUser(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void contactRequestWasRejected(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void rejectContactRequest(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void stopBroadcastingTo(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void removeContact(int var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   PresenceAndCapabilityIce contactUpdated(ContactDataIce var1, String var2, boolean var3, boolean var4, UserPrx var5, int var6, Map<String, String> var7) throws LocalExceptionWrapper, FusionException;

   void oldUserContactUpdated(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void newUserContactUpdated(String var1, boolean var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void notifySessionsOfNewContact(ContactDataIce var1, int var2, boolean var3, Map<String, String> var4) throws LocalExceptionWrapper;

   SessionPrx[] getSessions(Map<String, String> var1) throws LocalExceptionWrapper;

   void disconnect(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void disconnectFlooder(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   boolean privateChattedWith(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   UserErrorResponse userCanContactMe(String var1, MessageDataIce var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void enteringGroupChat(boolean var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void leavingGroupChat(Map<String, String> var1) throws LocalExceptionWrapper;

   void pushNotification(Message var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   int getOnlineContactsCount(Map<String, String> var1) throws LocalExceptionWrapper;

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void addToCurrentChatroomList(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void removeFromCurrentChatroomList(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   String[] getCurrentChatrooms(Map<String, String> var1) throws LocalExceptionWrapper;

   int getReputationDataLevel(Map<String, String> var1) throws LocalExceptionWrapper;

   void notifyUserLeftGroupChat(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3, Map<String, String> var4) throws LocalExceptionWrapper;

   void setCurrentChatListGroupChatSubset(ChatListIce var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void putMessageStatusEvent(MessageStatusEventIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   SessionPrx findSession(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;
}
