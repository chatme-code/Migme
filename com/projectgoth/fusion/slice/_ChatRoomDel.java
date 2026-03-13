package com.projectgoth.fusion.slice;

import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _ChatRoomDel extends _BotChannelDel {
   void addParticipantOld(UserPrx var1, UserDataIce var2, SessionPrx var3, String var4, String var5, String var6, String var7, Map<String, String> var8) throws LocalExceptionWrapper, FusionException;

   void addParticipant(UserPrx var1, UserDataIce var2, SessionPrx var3, String var4, String var5, String var6, String var7, short var8, int var9, Map<String, String> var10) throws LocalExceptionWrapper, FusionException;

   void removeParticipant(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void removeParticipantOneWay(String var1, boolean var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void addModerator(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void removeModerator(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void banUser(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void unbanUser(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void banGroupMembers(String[] var1, String var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void unbanGroupMember(String var1, String var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void banMultiIds(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void inviteUserToGroup(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void broadcastMessage(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void setMaximumSize(int var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void setDescription(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void updateDescription(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void setAllowKicking(boolean var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void setAdultOnly(boolean var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void changeOwner(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper;

   ChatRoomDataIce getRoomData(Map<String, String> var1) throws LocalExceptionWrapper;

   String[] getAllParticipants(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   String[] getAdministrators(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   int getNumParticipants(Map<String, String> var1) throws LocalExceptionWrapper;

   boolean isVisibleParticipant(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void listParticipants(String var1, int var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void banIndexes(int[] var1, String var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void kickIndexes(int[] var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void bumpUser(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void warnUser(String var1, String var2, String var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void voteToKickUser(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void clearUserKick(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void putMessage(MessageDataIce var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void putSystemMessage(String var1, String[] var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void putSystemMessageWithColour(String var1, String[] var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper;

   int getMaximumMessageLength(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void addGroupModerator(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void removeGroupModerator(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   String[] getGroupModerators(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void mute(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void unmute(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void unsilence(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void unsilenceUser(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void silence(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void silenceUser(String var1, String var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void setNumberOfFakeParticipants(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void convertIntoUserOwnedChatRoom(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   void convertIntoGroupChatRoom(int var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   boolean isLocked(Map<String, String> var1) throws LocalExceptionWrapper;

   void lock(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void unlock(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void announceOff(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void announceOn(String var1, String var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void adminAnnounce(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   Map<String, String> getTheme(Map<String, String> var1) throws LocalExceptionWrapper;

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void submitGiftAllTask(int var1, String var2, MessageDataIce var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void updateExtraData(ChatRoomDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void updateGroupModeratorStatus(String var1, boolean var2, Map<String, String> var3) throws LocalExceptionWrapper;
}
