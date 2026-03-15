package com.projectgoth.fusion.slice;

import java.util.Map;

public interface _ChatRoomOperationsNC extends _BotChannelOperationsNC {
   void addParticipantOld(UserPrx var1, UserDataIce var2, SessionPrx var3, String var4, String var5, String var6, String var7) throws FusionException;

   void addParticipant(UserPrx var1, UserDataIce var2, SessionPrx var3, String var4, String var5, String var6, String var7, short var8, int var9) throws FusionException;

   void removeParticipant(String var1) throws FusionException;

   void removeParticipantOneWay(String var1, boolean var2);

   void addModerator(String var1);

   void removeModerator(String var1);

   void banUser(String var1);

   void unbanUser(String var1);

   void banGroupMembers(String[] var1, String var2, int var3) throws FusionException;

   void unbanGroupMember(String var1, String var2, int var3) throws FusionException;

   void banMultiIds(String var1) throws FusionException;

   void inviteUserToGroup(String var1, String var2) throws FusionException;

   void broadcastMessage(String var1, String var2) throws FusionException;

   void setMaximumSize(int var1);

   void setDescription(String var1);

   void updateDescription(String var1, String var2) throws FusionException;

   void setAllowKicking(boolean var1);

   void setAdultOnly(boolean var1);

   void changeOwner(String var1, String var2);

   ChatRoomDataIce getRoomData();

   String[] getAllParticipants(String var1);

   String[] getAdministrators(String var1);

   int getNumParticipants();

   boolean isVisibleParticipant(String var1) throws FusionException;

   void listParticipants(String var1, int var2, int var3) throws FusionException;

   void banIndexes(int[] var1, String var2, int var3) throws FusionException;

   void kickIndexes(int[] var1, String var2) throws FusionException;

   void bumpUser(String var1, String var2) throws FusionException;

   void warnUser(String var1, String var2, String var3) throws FusionException;

   void voteToKickUser(String var1, String var2) throws FusionException;

   void clearUserKick(String var1, String var2) throws FusionException;

   void putMessage(MessageDataIce var1, String var2) throws FusionException;

   void putSystemMessage(String var1, String[] var2);

   void putSystemMessageWithColour(String var1, String[] var2, int var3);

   int getMaximumMessageLength(String var1);

   void addGroupModerator(String var1, String var2) throws FusionException;

   void removeGroupModerator(String var1, String var2) throws FusionException;

   String[] getGroupModerators(String var1) throws FusionException;

   void mute(String var1, String var2) throws FusionException;

   void unmute(String var1, String var2) throws FusionException;

   void unsilence(String var1) throws FusionException;

   void unsilenceUser(String var1, String var2) throws FusionException;

   void silence(String var1, int var2) throws FusionException;

   void silenceUser(String var1, String var2, int var3) throws FusionException;

   void setNumberOfFakeParticipants(String var1, int var2);

   void convertIntoUserOwnedChatRoom() throws FusionException;

   void convertIntoGroupChatRoom(int var1, String var2) throws FusionException;

   boolean isLocked();

   void lock(String var1) throws FusionException;

   void unlock(String var1) throws FusionException;

   void announceOff(String var1) throws FusionException;

   void announceOn(String var1, String var2, int var3) throws FusionException;

   void adminAnnounce(String var1, int var2) throws FusionException;

   Map<String, String> getTheme();

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3) throws FusionException;

   void submitGiftAllTask(int var1, String var2, MessageDataIce var3) throws FusionException;

   void updateExtraData(ChatRoomDataIce var1);

   void updateGroupModeratorStatus(String var1, boolean var2);
}
