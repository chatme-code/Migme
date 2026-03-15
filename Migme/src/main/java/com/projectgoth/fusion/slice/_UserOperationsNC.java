package com.projectgoth.fusion.slice;

public interface _UserOperationsNC {
   void createSession_async(AMD_User_createSession var1, String var2, int var3, int var4, int var5, int var6, int var7, int var8, String var9, String var10, String var11, short var12, String var13, ConnectionPrx var14) throws FusionException;

   void putMessage_async(AMD_User_putMessage var1, MessageDataIce var2) throws FusionException;

   void contactChangedPresenceOneWay(int var1, String var2, int var3);

   void contactChangedDisplayPictureOneWay(String var1, String var2, long var3);

   void contactChangedStatusMessageOneWay(String var1, String var2, long var3);

   int getOverallFusionPresence(String var1);

   int getContactListVersion();

   ContactList getContactList();

   ContactDataIce[] getContacts();

   ContactDataIce[] getOtherIMContacts();

   Credential[] getOtherIMCredentials();

   String[] getOtherIMConferenceParticipants(int var1, String var2);

   UserDataIce getUserData();

   boolean isOnContactList(String var1);

   boolean isOnBlockList(String var1);

   void otherIMLogin_async(AMD_User_otherIMLogin var1, int var2, int var3, boolean var4) throws FusionException;

   void otherIMLogout(int var1);

   void otherIMSendMessage(int var1, String var2, String var3) throws FusionException;

   String otherIMInviteToConference(int var1, String var2, String var3) throws FusionException;

   void otherIMLeaveConference(int var1, String var2);

   void otherIMAddContact(int var1, String var2) throws FusionException;

   void otherIMRemoveContact(int var1) throws FusionException;

   void otherIMRemoved(int var1);

   String[] getBlockList();

   String[] getBlockListFromUsernames(String[] var1);

   String[] getBroadcastList();

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5);

   void privateChatNowAGroupChat(String var1, String var2) throws FusionException;

   void putEvent(UserEventIce var1) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3) throws FusionException;

   void putServerQuestion(String var1, String var2) throws FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5) throws FusionException;

   void putAnonymousCallNotification(String var1, String var2) throws FusionException;

   void putFileReceived(MessageDataIce var1) throws FusionException;

   void contactDetailChanged(ContactDataIce var1, int var2);

   void contactGroupDetailChanged(ContactGroupDataIce var1, int var2);

   void contactGroupDeleted(int var1, int var2);

   void userDetailChanged(UserDataIce var1);

   void userReputationChanged();

   void userDisplayPictureChanged(String var1, long var2);

   void userStatusMessageChanged(String var1, long var2);

   void messageSettingChanged(int var1);

   void anonymousCallSettingChanged(int var1);

   int[] getConnectedOtherIMs();

   boolean supportsBinaryMessage();

   int getUnreadEmailCount();

   void emailNotification(int var1);

   String[] getEmoticonHotKeys();

   String[] getEmoticonAlternateKeys();

   void emoticonPackActivated(int var1);

   void themeChanged(String var1) throws FusionException;

   void addContact(ContactDataIce var1, int var2);

   void addToContactAndBroadcastLists(ContactDataIce var1, int var2);

   void addPendingContact(String var1);

   ContactDataIce acceptContactRequest(ContactDataIce var1, UserPrx var2, int var3, int var4);

   ContactDataIce contactRequestWasAccepted(ContactDataIce var1, String var2, String var3, int var4, int var5);

   void blockUser(String var1, int var2);

   void unblockUser(String var1);

   void contactRequestWasRejected(String var1, int var2);

   void rejectContactRequest(String var1);

   void stopBroadcastingTo(String var1);

   void removeContact(int var1, int var2);

   PresenceAndCapabilityIce contactUpdated(ContactDataIce var1, String var2, boolean var3, boolean var4, UserPrx var5, int var6) throws FusionException;

   void oldUserContactUpdated(String var1) throws FusionException;

   void newUserContactUpdated(String var1, boolean var2);

   void notifySessionsOfNewContact(ContactDataIce var1, int var2, boolean var3);

   SessionPrx[] getSessions();

   void disconnect(String var1);

   void disconnectFlooder(String var1);

   boolean privateChattedWith(String var1);

   UserErrorResponse userCanContactMe(String var1, MessageDataIce var2);

   void enteringGroupChat(boolean var1) throws FusionException;

   void leavingGroupChat();

   void pushNotification(Message var1) throws FusionException;

   int getOnlineContactsCount();

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3) throws FusionException;

   void addToCurrentChatroomList(String var1) throws FusionException;

   void removeFromCurrentChatroomList(String var1);

   String[] getCurrentChatrooms();

   int getReputationDataLevel();

   void notifyUserLeftGroupChat(String var1, String var2);

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3);

   void setCurrentChatListGroupChatSubset(ChatListIce var1);

   void putMessageStatusEvent(MessageStatusEventIce var1) throws FusionException;

   SessionPrx findSession(String var1) throws FusionException;
}
