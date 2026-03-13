package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _UserOperations {
   void createSession_async(AMD_User_createSession var1, String var2, int var3, int var4, int var5, int var6, int var7, int var8, String var9, String var10, String var11, short var12, String var13, ConnectionPrx var14, Current var15) throws FusionException;

   void putMessage_async(AMD_User_putMessage var1, MessageDataIce var2, Current var3) throws FusionException;

   void contactChangedPresenceOneWay(int var1, String var2, int var3, Current var4);

   void contactChangedDisplayPictureOneWay(String var1, String var2, long var3, Current var5);

   void contactChangedStatusMessageOneWay(String var1, String var2, long var3, Current var5);

   int getOverallFusionPresence(String var1, Current var2);

   int getContactListVersion(Current var1);

   ContactList getContactList(Current var1);

   ContactDataIce[] getContacts(Current var1);

   ContactDataIce[] getOtherIMContacts(Current var1);

   Credential[] getOtherIMCredentials(Current var1);

   String[] getOtherIMConferenceParticipants(int var1, String var2, Current var3);

   UserDataIce getUserData(Current var1);

   boolean isOnContactList(String var1, Current var2);

   boolean isOnBlockList(String var1, Current var2);

   void otherIMLogin_async(AMD_User_otherIMLogin var1, int var2, int var3, boolean var4, Current var5) throws FusionException;

   void otherIMLogout(int var1, Current var2);

   void otherIMSendMessage(int var1, String var2, String var3, Current var4) throws FusionException;

   String otherIMInviteToConference(int var1, String var2, String var3, Current var4) throws FusionException;

   void otherIMLeaveConference(int var1, String var2, Current var3);

   void otherIMAddContact(int var1, String var2, Current var3) throws FusionException;

   void otherIMRemoveContact(int var1, Current var2) throws FusionException;

   void otherIMRemoved(int var1, Current var2);

   String[] getBlockList(Current var1);

   String[] getBlockListFromUsernames(String[] var1, Current var2);

   String[] getBroadcastList(Current var1);

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5, Current var6);

   void privateChatNowAGroupChat(String var1, String var2, Current var3) throws FusionException;

   void putEvent(UserEventIce var1, Current var2) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3, Current var4) throws FusionException;

   void putServerQuestion(String var1, String var2, Current var3) throws FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5, Current var6) throws FusionException;

   void putAnonymousCallNotification(String var1, String var2, Current var3) throws FusionException;

   void putFileReceived(MessageDataIce var1, Current var2) throws FusionException;

   void contactDetailChanged(ContactDataIce var1, int var2, Current var3);

   void contactGroupDetailChanged(ContactGroupDataIce var1, int var2, Current var3);

   void contactGroupDeleted(int var1, int var2, Current var3);

   void userDetailChanged(UserDataIce var1, Current var2);

   void userReputationChanged(Current var1);

   void userDisplayPictureChanged(String var1, long var2, Current var4);

   void userStatusMessageChanged(String var1, long var2, Current var4);

   void messageSettingChanged(int var1, Current var2);

   void anonymousCallSettingChanged(int var1, Current var2);

   int[] getConnectedOtherIMs(Current var1);

   boolean supportsBinaryMessage(Current var1);

   int getUnreadEmailCount(Current var1);

   void emailNotification(int var1, Current var2);

   String[] getEmoticonHotKeys(Current var1);

   String[] getEmoticonAlternateKeys(Current var1);

   void emoticonPackActivated(int var1, Current var2);

   void themeChanged(String var1, Current var2) throws FusionException;

   void addContact(ContactDataIce var1, int var2, Current var3);

   void addToContactAndBroadcastLists(ContactDataIce var1, int var2, Current var3);

   void addPendingContact(String var1, Current var2);

   ContactDataIce acceptContactRequest(ContactDataIce var1, UserPrx var2, int var3, int var4, Current var5);

   ContactDataIce contactRequestWasAccepted(ContactDataIce var1, String var2, String var3, int var4, int var5, Current var6);

   void blockUser(String var1, int var2, Current var3);

   void unblockUser(String var1, Current var2);

   void contactRequestWasRejected(String var1, int var2, Current var3);

   void rejectContactRequest(String var1, Current var2);

   void stopBroadcastingTo(String var1, Current var2);

   void removeContact(int var1, int var2, Current var3);

   PresenceAndCapabilityIce contactUpdated(ContactDataIce var1, String var2, boolean var3, boolean var4, UserPrx var5, int var6, Current var7) throws FusionException;

   void oldUserContactUpdated(String var1, Current var2) throws FusionException;

   void newUserContactUpdated(String var1, boolean var2, Current var3);

   void notifySessionsOfNewContact(ContactDataIce var1, int var2, boolean var3, Current var4);

   SessionPrx[] getSessions(Current var1);

   void disconnect(String var1, Current var2);

   void disconnectFlooder(String var1, Current var2);

   boolean privateChattedWith(String var1, Current var2);

   UserErrorResponse userCanContactMe(String var1, MessageDataIce var2, Current var3);

   void enteringGroupChat(boolean var1, Current var2) throws FusionException;

   void leavingGroupChat(Current var1);

   void pushNotification(Message var1, Current var2) throws FusionException;

   int getOnlineContactsCount(Current var1);

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3, Current var4) throws FusionException;

   void addToCurrentChatroomList(String var1, Current var2) throws FusionException;

   void removeFromCurrentChatroomList(String var1, Current var2);

   String[] getCurrentChatrooms(Current var1);

   int getReputationDataLevel(Current var1);

   void notifyUserLeftGroupChat(String var1, String var2, Current var3);

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3, Current var4);

   void setCurrentChatListGroupChatSubset(ChatListIce var1, Current var2);

   void putMessageStatusEvent(MessageStatusEventIce var1, Current var2) throws FusionException;

   SessionPrx findSession(String var1, Current var2) throws FusionException;
}
